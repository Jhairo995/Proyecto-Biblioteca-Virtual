package com.example.ejemplodemo.e2e;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de interfaz con Selenium. Se ejecutan contra una aplicación YA levantada
 * (local, docker-compose o Kubernetes) y solo si se activa la propiedad:
 *
 *   ./mvnw test -Dtest=LoginSeleniumIT -Dselenium=true -Dbase.url=http://localhost:8081
 *
 * Por defecto se omiten para que el pipeline de CI no dependa de un navegador.
 */
@EnabledIfSystemProperty(named = "selenium", matches = "true")
class LoginSeleniumIT {

    private static final String BASE = System.getProperty("base.url", "http://localhost:8081");
    private WebDriver driver;

    @BeforeEach
    void abrirNavegador() {
        ChromeOptions opciones = new ChromeOptions();
        opciones.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage");
        driver = new ChromeDriver(opciones);
    }

    @AfterEach
    void cerrarNavegador() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @DisplayName("CP-UI-01: la página de login muestra el formulario con correo y contraseña")
    void loginMuestraFormulario() {
        driver.get(BASE + "/login");
        assertFalse(driver.findElements(By.name("correo")).isEmpty());
        assertFalse(driver.findElements(By.name("contrasena")).isEmpty());
    }

    @Test
    @DisplayName("CP-UI-02: credenciales incorrectas vuelven al login con error")
    void loginConCredencialesInvalidas() {
        driver.get(BASE + "/login");
        driver.findElement(By.cssSelector("#formLogin input[name='correo']")).sendKeys("no-existe@correo.com");
        driver.findElement(By.cssSelector("#formLogin input[name='contrasena']")).sendKeys("clave-incorrecta");
        driver.findElement(By.cssSelector("#formLogin button[type='submit']")).click();
        assertTrue(driver.getCurrentUrl().contains("error"));
    }

    @Test
    @DisplayName("CP-UI-03: el panel de administración exige iniciar sesión")
    void adminRequiereLogin() {
        driver.get(BASE + "/admin");
        assertTrue(driver.getCurrentUrl().contains("/login"));
    }

    @Test
    @DisplayName("CP-UI-04: el catálogo es público")
    void catalogoEsPublico() {
        driver.get(BASE + "/catalogo");
        assertFalse(driver.getCurrentUrl().contains("/login"));
    }
}
