package com.example.ejemplodemo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

// Prueba de humo: la aplicación arranca completa usando H2 (perfil "test")
@SpringBootTest
@ActiveProfiles("test")
class EjemplodemoApplicationTests {

    @Test
    void contextLoads() {
    }
}
