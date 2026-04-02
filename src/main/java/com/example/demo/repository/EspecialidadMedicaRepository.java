package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.EspecialidadMedica;

public interface EspecialidadMedicaRepository extends JpaRepository<EspecialidadMedica, Long> {
    Optional<EspecialidadMedica> findByCodigo(String codigo);

    Optional<EspecialidadMedica> findByNombre(String nombre);

    List<EspecialidadMedica> findByDiasEsperaPromedioLessThanEqual(Integer dias);
    
    boolean existsByCodigo(String codigo);

    boolean existsByNombre(String nombre);
}