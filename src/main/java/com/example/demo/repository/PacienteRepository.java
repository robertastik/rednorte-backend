package com.example.demo.repository;

import com.example.demo.model.Paciente;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    
    Optional<Paciente> findByRut(String rut);

    Optional<Paciente> findByEmail(String email);

    boolean existsByRut(String rut);

    boolean existsByEmail(String email);

    List<Paciente> findBySistemaSalud(String sistemaSalud);

    List<Paciente> findByNombreCompletoContainingIgnoreCase(String nombreCompleto);
}
