package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "paciente")
public class Paciente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rut", nullable = false, unique = true)
    private String rut;

    @Column(name = "nombre_completo", nullable = false)
    private String nombreCompleto;

    @Column(name = "fecha_nacimiento", nullable = false)
    private String fechaNacimiento;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(length = 15)
    private String telefono;

    @Column(name = "sistema_salud")
    private String sistemaSalud; // Fonasa o Isapre
}
