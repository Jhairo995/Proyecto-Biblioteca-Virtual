package com.example.ejemplodemo.config;

import org.hibernate.dialect.DatabaseVersion;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class XamppMariaDBDialectTest {

    private static DialectResolutionInfo infoConVersion(String version, int mayor, int menor) {
        DialectResolutionInfo info = mock(DialectResolutionInfo.class);
        when(info.getDatabaseVersion()).thenReturn(version);
        when(info.getDatabaseMajorVersion()).thenReturn(mayor);
        when(info.getDatabaseMinorVersion()).thenReturn(menor);
        return info;
    }

    @Test
    @DisplayName("Quita el prefijo 5.5.5- que MariaDB envía al driver de MySQL")
    void quitaPrefijoDeCompatibilidad() {
        DialectResolutionInfo info = infoConVersion("5.5.5-10.4.32-MariaDB", 5, 5);

        var corregida = new XamppMariaDBDialect.VersionMariaDBCorregida(info);

        assertEquals("10.4.32-MariaDB", corregida.getDatabaseVersion());
        assertEquals(10, corregida.getDatabaseMajorVersion());
        assertEquals(4, corregida.getDatabaseMinorVersion());
        assertEquals(32, corregida.getDatabaseMicroVersion());
    }

    @Test
    @DisplayName("Respeta la versión cuando no trae el prefijo (MariaDB nuevo o driver MariaDB)")
    void respetaVersionSinPrefijo() {
        DialectResolutionInfo info = infoConVersion("11.4.2-MariaDB", 11, 4);

        var corregida = new XamppMariaDBDialect.VersionMariaDBCorregida(info);

        assertEquals("11.4.2-MariaDB", corregida.getDatabaseVersion());
        assertEquals(11, corregida.getDatabaseMajorVersion());
        assertEquals(4, corregida.getDatabaseMinorVersion());
    }

    @Test
    @DisplayName("El dialecto queda con la versión real de XAMPP (10.4), no 5.5.5")
    void dialectoUsaVersionReal() {
        DialectResolutionInfo info = infoConVersion("5.5.5-10.4.32-MariaDB", 5, 5);

        XamppMariaDBDialect dialecto = new XamppMariaDBDialect(info);

        assertTrue(dialecto.getVersion().isSameOrAfter(DatabaseVersion.make(10, 4)));
        assertFalse(dialecto.supportsIfExistsAfterAlterTable());
    }
}
