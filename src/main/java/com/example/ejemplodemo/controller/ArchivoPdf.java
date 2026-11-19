package com.example.ejemplodemo.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

// Respuesta HTTP para mostrar un PDF dentro del navegador
public final class ArchivoPdf {

    private ArchivoPdf() {
    }

    public static ResponseEntity<Resource> enLinea(Resource archivo, Long idLibro) {
        ContentDisposition disposicion = ContentDisposition.inline()
                .filename("libro-" + idLibro + ".pdf")
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposicion.toString())
                .body(archivo);
    }
}
