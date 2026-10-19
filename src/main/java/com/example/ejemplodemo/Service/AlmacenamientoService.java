package com.example.ejemplodemo.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;

// Guarda y recupera los archivos PDF de los libros en disco
@Service
public class AlmacenamientoService {

    private static final byte[] FIRMA_PDF = "%PDF".getBytes(StandardCharsets.US_ASCII);
    private static final String EXTENSION_PDF = ".pdf";

    private final Path directorio;

    public AlmacenamientoService(@Value("${app.uploads.dir}") String directorio) {
        this.directorio = Path.of(directorio, "libros").toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.directorio);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo crear el directorio de libros: " + this.directorio, e);
        }
    }

    public String guardarPdf(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("Selecciona el archivo PDF del libro.");
        }
        String nombreOriginal = archivo.getOriginalFilename();
        if (nombreOriginal == null || !nombreOriginal.toLowerCase(Locale.ROOT).endsWith(EXTENSION_PDF)) {
            throw new IllegalArgumentException("Solo se permiten archivos PDF.");
        }

        try {
            // Verifica la firma real del archivo, no solo su extensión
            try (InputStream entrada = archivo.getInputStream()) {
                if (!Arrays.equals(entrada.readNBytes(FIRMA_PDF.length), FIRMA_PDF)) {
                    throw new IllegalArgumentException("El archivo no es un PDF válido.");
                }
            }
            // Nombre aleatorio: evita colisiones y rutas manipuladas por el usuario
            String nombreGuardado = UUID.randomUUID() + EXTENSION_PDF;
            archivo.transferTo(directorio.resolve(nombreGuardado));
            return nombreGuardado;
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar el archivo del libro.", e);
        }
    }

    public Resource cargar(String nombreArchivo) {
        if (nombreArchivo == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El libro no tiene archivo.");
        }
        Path ruta = directorio.resolve(nombreArchivo).normalize();
        if (!ruta.startsWith(directorio) || !Files.isRegularFile(ruta)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Archivo no encontrado.");
        }
        return new FileSystemResource(ruta);
    }
}
