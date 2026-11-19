package com.example.ejemplodemo.dto;

// Datos del registro público. Se usa un DTO (y no la entidad) para que el cliente no pueda fijar id, plan ni estado
public record RegistroUsuario(String nombre, String dni, String correo, String contrasena, String rol) {
}
