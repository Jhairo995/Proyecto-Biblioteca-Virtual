package com.example.ejemplodemo;

import com.example.ejemplodemo.config.DatosIniciales;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Simula una base creada con la versión anterior (rol y categoría como texto) y verifica que se migre
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:migracion_legado;MODE=MySQL;DB_CLOSE_DELAY=-1")
@ActiveProfiles("test")
class MigracionEsquemaLegadoTest {

    private static final String INSERTAR_USUARIO = "INSERT INTO usuarios (nombre, correo, contrasena, nivel_cuenta, dni, activo, rol) "
            + "VALUES (?, ?, 'x', 'GRATUITO', ?, TRUE, ?)";
    private static final String INSERTAR_LIBRO = "INSERT INTO libros (titulo, nivel_acceso, estado, fecha_registro, categoria) "
            + "VALUES (?, 'GRATUITO', 'APROBADO', ?, ?)";

    @Autowired
    private DataSource dataSource;
    @Autowired
    private DatosIniciales datosIniciales;

    @Test
    @DisplayName("Las columnas de texto antiguas pasan a llaves foráneas y se eliminan")
    void migraColumnasAntiguas() {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("ALTER TABLE usuarios ADD COLUMN rol VARCHAR(20)");
        jdbc.execute("ALTER TABLE libros ADD COLUMN categoria VARCHAR(255)");
        jdbc.update(INSERTAR_USUARIO, "Escritora Antigua", "antigua@correo.com", "55556666", "ESCRITOR");
        jdbc.update(INSERTAR_USUARIO, "Rol Desconocido", "desconocido@correo.com", "55557777", "INVITADO");
        Timestamp ahora = Timestamp.valueOf(LocalDateTime.now());
        jdbc.update(INSERTAR_LIBRO, "Libro de Redes", ahora, "Redes de Computadoras");
        jdbc.update(INSERTAR_LIBRO, "Libro de Java", ahora, "  java ");

        datosIniciales.run();

        assertEquals("ESCRITOR", accesoDe(jdbc, "antigua@correo.com"));
        assertEquals("LECTOR", accesoDe(jdbc, "desconocido@correo.com"));
        assertEquals("Redes de Computadoras", categoriaDe(jdbc, "Libro de Redes"));
        assertEquals("Java", categoriaDe(jdbc, "Libro de Java"));
        assertEquals(0, contarColumnas(jdbc, "USUARIOS", "ROL"));
        assertEquals(0, contarColumnas(jdbc, "LIBROS", "CATEGORIA"));
    }

    private static String accesoDe(JdbcTemplate jdbc, String correo) {
        return jdbc.queryForObject("SELECT r.acceso FROM usuarios u JOIN roles r ON r.id = u.rol_id WHERE u.correo = ?",
                String.class, correo);
    }

    private static String categoriaDe(JdbcTemplate jdbc, String titulo) {
        return jdbc.queryForObject("SELECT c.nombre FROM libros l JOIN categorias c ON c.id = l.categoria_id WHERE l.titulo = ?",
                String.class, titulo);
    }

    private static int contarColumnas(JdbcTemplate jdbc, String tabla, String columna) {
        Integer total = jdbc.queryForObject("SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ? AND COLUMN_NAME = ?",
                Integer.class, tabla, columna);
        return total == null ? 0 : total;
    }
}
