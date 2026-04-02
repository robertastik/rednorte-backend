package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Doctor;
import com.example.demo.model.EspecialidadMedica;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByNumeroLicencia(String numeroLicencia);

    List<Doctor> findByDisponibleTrue();

    List<Doctor> findByEspecialidad(EspecialidadMedica especialidad);

    List<Doctor> findByEspecialidadAndDisponibleTrue(EspecialidadMedica especialidad);

    List<Doctor> findByEspecialidadIdAndDisponibleTrue(Long especialidadId);

    Long countByEspecialidadIdAndDisponibleTrue(Long especialidadId);

    boolean existsByNumeroLicencia(String numeroLicencia);
}
