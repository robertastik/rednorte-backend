package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.model.EntradaListaEspera;
import com.example.demo.model.enums.EstadoListaEspera;

@Repository
public interface EntradaListaEsperaRepository extends JpaRepository<EntradaListaEspera, Long> {
    List<EntradaListaEspera> findByPacienteId(Long pacienteId);

    List<EntradaListaEspera> findByEstado(EstadoListaEspera estado);

    List<EntradaListaEspera> findByEspecialidadId(Long especialidadId);
    
    List<EntradaListaEspera> findByPacienteIdAndEstado(Long pacienteId, EstadoListaEspera estado);

    List<EntradaListaEspera> findByEstadoAndEspecialidadIdOrderByPuntajePrioridadDescFechaRegistroAsc(
        EstadoListaEspera estado, Long especialidadId
    );

    List<EntradaListaEspera> findByDoctorAsignadoIsNullAndEstado(EstadoListaEspera estado);

    List<EntradaListaEspera> findByEstadoAndFechaRegistroLessThanEqualOrderByFechaRegistroAsc(
        EstadoListaEspera estado, String limitDate
    );

    Long countByEspecialidadIdAndEstado(Long especialidadId, EstadoListaEspera estado);
    
    boolean existsByPacienteIdAndEspecialidadIdAndEstadoIn(
        Long pacienteId, Long especialidadId, List<EstadoListaEspera> estados
    );
}
