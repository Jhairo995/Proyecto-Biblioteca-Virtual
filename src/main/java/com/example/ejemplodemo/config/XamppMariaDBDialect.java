package com.example.ejemplodemo.config;

import org.hibernate.dialect.DatabaseVersion;
import org.hibernate.dialect.MariaDBDialect;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;

import java.sql.DatabaseMetaData;
import java.util.Map;

/**
 * Dialecto MariaDB compatible con XAMPP (MariaDB 10.4).
 * Hibernate 7 genera "ALTER TABLE IF EXISTS ..." (soportado desde MariaDB 10.5),
 * lo que impide crear las llaves foráneas en XAMPP. Este dialecto omite esos "IF EXISTS"
 * y funciona igual con versiones nuevas de MariaDB (Docker / Kubernetes).
 *
 * Además corrige la versión detectada: con el driver de MySQL, MariaDB se anuncia como
 * "5.5.5-10.4.32-MariaDB" y Hibernate la tomaba como 5.5.5 (versión antigua sin soporte).
 */
public class XamppMariaDBDialect extends MariaDBDialect {

    // Versión de MariaDB que trae XAMPP; es la mínima que este dialecto está pensado para soportar
    private static final DatabaseVersion VERSION_MINIMA_XAMPP = DatabaseVersion.make(10, 4);

    public XamppMariaDBDialect() {
        super();
    }

    public XamppMariaDBDialect(DatabaseVersion version) {
        super(version);
    }

    public XamppMariaDBDialect(DialectResolutionInfo info) {
        super(new VersionMariaDBCorregida(info));
    }

    @Override
    protected DatabaseVersion getMinimumSupportedVersion() {
        return VERSION_MINIMA_XAMPP;
    }

    @Override
    public boolean supportsIfExistsAfterAlterTable() {
        return false;
    }

    @Override
    public boolean supportsIfExistsBeforeConstraintName() {
        return false;
    }

    @Override
    public boolean supportsIfExistsBeforeIndexName() {
        return false;
    }

    /**
     * Envuelve la información de la BD quitando el prefijo "5.5.5-" que MariaDB antepone
     * a su versión real por compatibilidad con clientes MySQL antiguos.
     */
    static final class VersionMariaDBCorregida implements DialectResolutionInfo {

        private static final String PREFIJO_COMPATIBILIDAD = "5.5.5-";

        private final DialectResolutionInfo original;
        private final String version;
        private final int mayor;
        private final int menor;
        private final int micro;

        VersionMariaDBCorregida(DialectResolutionInfo original) {
            this.original = original;
            String reportada = original.getDatabaseVersion();
            if (reportada == null || !reportada.startsWith(PREFIJO_COMPATIBILIDAD)) {
                this.version = reportada;
                this.mayor = original.getDatabaseMajorVersion();
                this.menor = original.getDatabaseMinorVersion();
                this.micro = original.getDatabaseMicroVersion();
                return;
            }
            this.version = reportada.substring(PREFIJO_COMPATIBILIDAD.length());
            int[] partes = partesDeVersion(this.version);
            this.mayor = partes[0];
            this.menor = partes[1];
            this.micro = partes[2];
        }

        // "10.4.32-MariaDB" -> {10, 4, 32}; las partes que no se puedan leer quedan en 0
        private static int[] partesDeVersion(String version) {
            String[] componentes = version.split("[.-]");
            int[] partes = new int[3];
            for (int i = 0; i < partes.length && i < componentes.length; i++) {
                try {
                    partes[i] = Integer.parseInt(componentes[i]);
                } catch (NumberFormatException e) {
                    break;
                }
            }
            return partes;
        }

        @Override
        public String getDatabaseVersion() {
            return version;
        }

        @Override
        public int getDatabaseMajorVersion() {
            return mayor;
        }

        @Override
        public int getDatabaseMinorVersion() {
            return menor;
        }

        @Override
        public int getDatabaseMicroVersion() {
            return micro;
        }

        @Override
        public String getDatabaseName() {
            return original.getDatabaseName();
        }

        @Override
        public String getDriverName() {
            return original.getDriverName();
        }

        @Override
        public int getDriverMajorVersion() {
            return original.getDriverMajorVersion();
        }

        @Override
        public int getDriverMinorVersion() {
            return original.getDriverMinorVersion();
        }

        @Override
        public String getSQLKeywords() {
            return original.getSQLKeywords();
        }

        @Override
        public DatabaseMetaData getDatabaseMetadata() {
            return original.getDatabaseMetadata();
        }

        @Override
        public Map<String, Object> getConfigurationValues() {
            return original.getConfigurationValues();
        }
    }
}
