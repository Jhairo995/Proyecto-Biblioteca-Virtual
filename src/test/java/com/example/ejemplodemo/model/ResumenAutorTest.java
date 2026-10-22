package com.example.ejemplodemo.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class ResumenAutorTest {

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "Ana Pérez              | AP",
            "Esteban Escritor López | EE",
            "juan                   | J",
            "'  maría   josé  '     | MJ",
            "'   '                  | ?"
    })
    void inicialesTomaLaPrimeraLetraDeLasDosPrimerasPalabras(String nombre, String iniciales) {
        assertThat(new ResumenAutor(nombre, 0L, 0L).iniciales()).isEqualTo(iniciales);
    }
}
