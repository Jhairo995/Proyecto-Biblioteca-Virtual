package com.example.ejemplodemo.Repository;

// Proyección para consultas "cuántos registros hay por id" (usuarios por rol, libros por categoría)
public interface ConteoPorId {
    Long getId();

    long getTotal();
}
