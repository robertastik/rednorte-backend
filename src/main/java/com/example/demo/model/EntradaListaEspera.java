package com.example.demo.model;

import com.example.demo.model.enums.EstadoListaEspera;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "entrada_lista_espera")
@NoArgsConstructor
@AllArgsConstructor
public class EntradaListaEspera {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Muchas entradas → un paciente
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    // Muchas entradas → una especialidad
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "especialidad_id", nullable = false)
    private EspecialidadMedica especialidad;

    // Muchas entradas → un doctor (nullable: puede no tener doctor aún)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private Doctor doctorAsignado;

    @Column(name = "fecha_registro", nullable = false)
    private String fechaRegistro;

    @Column(name = "fecha_estimada_atencion")
    private String fechaEstimadaAtencion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoListaEspera estado;

    @Column(name = "puntaje_prioridad")
    private Integer puntajePrioridad;

    @Column(name = "notas_clinicas", columnDefinition = "TEXT")
    private String notasClinicas;
}
