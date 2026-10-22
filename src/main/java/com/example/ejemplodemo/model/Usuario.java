package com.example.ejemplodemo.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
// Las claves UNIQUE van a nivel de tabla porque con ddl-auto=update Hibernate también las agrega
// a tablas que ya existen; un @Column(unique = true) solo se aplica cuando crea la tabla desde cero
@Table(name = "usuarios", uniqueConstraints = {
        @UniqueConstraint(name = "uk_usuarios_correo", columnNames = "correo"),
        @UniqueConstraint(name = "uk_usuarios_dni", columnNames = "dni")
})
@Data
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String correo;

    @Column(nullable = false)
    private String contrasena; 

    // Se guardan como VARCHAR para poder agregar valores al enum sin alterar la columna
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private Rol rol = Rol.LECTOR;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private NivelCuenta nivelCuenta = NivelCuenta.GRATUITO;

    @Column(nullable = false, length = 8)
    private String dni;

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public String getDni() { return dni;}
}