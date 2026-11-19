package com.example.ejemplodemo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Migra las bases creadas con versiones anteriores, donde el rol del usuario y la categoría del libro
 * eran columnas de texto ("usuarios.rol" y "libros.categoria"). Copia esos valores a las nuevas llaves
 * foráneas ("rol_id" y "categoria_id") y luego elimina las columnas antiguas, que ya no usa la aplicación
 * y que, por ser NOT NULL, impedirían insertar registros nuevos.
 * En una base nueva no hace nada.
 */
@Component
public class MigracionEsquemaLegado {

    private static final Logger log = LoggerFactory.getLogger(MigracionEsquemaLegado.class);

    private final DataSource dataSource;
    private final JdbcTemplate jdbc;

    public MigracionEsquemaLegado(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbc = new JdbcTemplate(dataSource);
    }

    public void migrarRolesDeUsuarios(Long idRolPorDefecto) {
        if (!existeColumna("usuarios", "rol")) {
            return;
        }
        int migrados = jdbc.update("UPDATE usuarios SET rol_id = (SELECT r.id FROM roles r "
                + "WHERE r.sistema = TRUE AND r.acceso = usuarios.rol) WHERE rol_id IS NULL");
        int sinCoincidencia = jdbc.update("UPDATE usuarios SET rol_id = ? WHERE rol_id IS NULL", idRolPorDefecto);
        jdbc.execute("ALTER TABLE usuarios DROP COLUMN rol");
        log.info("Migración: {} usuario(s) pasaron al nuevo esquema de roles ({} sin rol reconocido quedaron como lector).",
                migrados, sinCoincidencia);
    }

    /**
     * @param crearCategoria      crea la categoría con ese nombre si todavía no existe
     * @param categoriaPorDefecto id de la categoría para los libros que no coincidan con ninguna
     */
    public void migrarCategoriasDeLibros(Consumer<String> crearCategoria, Supplier<Long> categoriaPorDefecto) {
        if (!existeColumna("libros", "categoria")) {
            return;
        }
        List<String> nombres = jdbc.queryForList("SELECT DISTINCT TRIM(categoria) FROM libros "
                + "WHERE categoria_id IS NULL AND categoria IS NOT NULL AND TRIM(categoria) <> ''", String.class);
        nombres.forEach(crearCategoria);

        int migrados = jdbc.update("UPDATE libros SET categoria_id = (SELECT c.id FROM categorias c "
                + "WHERE LOWER(c.nombre) = LOWER(TRIM(libros.categoria))) WHERE categoria_id IS NULL");
        Integer pendientes = jdbc.queryForObject("SELECT COUNT(*) FROM libros WHERE categoria_id IS NULL", Integer.class);
        int sinCoincidencia = pendientes == null || pendientes == 0 ? 0
                : jdbc.update("UPDATE libros SET categoria_id = ? WHERE categoria_id IS NULL", categoriaPorDefecto.get());
        jdbc.execute("ALTER TABLE libros DROP COLUMN categoria");
        log.info("Migración: {} libro(s) pasaron al nuevo esquema de categorías ({} sin categoría reconocida).",
                migrados, sinCoincidencia);
    }

    private boolean existeColumna(String tabla, String columna) {
        try (Connection conexion = dataSource.getConnection()) {
            DatabaseMetaData metadatos = conexion.getMetaData();
            try (ResultSet columnas = metadatos.getColumns(conexion.getCatalog(), conexion.getSchema(),
                    comoLosGuarda(metadatos, tabla), comoLosGuarda(metadatos, columna))) {
                return columnas.next();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo revisar el esquema de la base de datos.", e);
        }
    }

    // Cada motor guarda los nombres sin comillas en mayúsculas, minúsculas o tal cual (H2 usa mayúsculas)
    private static String comoLosGuarda(DatabaseMetaData metadatos, String identificador) throws SQLException {
        if (metadatos.storesUpperCaseIdentifiers()) {
            return identificador.toUpperCase(Locale.ROOT);
        }
        if (metadatos.storesLowerCaseIdentifiers()) {
            return identificador.toLowerCase(Locale.ROOT);
        }
        return identificador;
    }
}
