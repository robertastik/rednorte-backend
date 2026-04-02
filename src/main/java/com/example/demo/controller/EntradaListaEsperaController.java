package com.example.demo.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.EntradaListaEspera;
import com.example.demo.model.enums.EstadoListaEspera;
import com.example.demo.repository.EntradaListaEsperaRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/lista-espera")
@RequiredArgsConstructor
public class EntradaListaEsperaController {
    @Autowired
    private EntradaListaEsperaRepository entradaListaEsperaRepository;

    @GetMapping
    public ResponseEntity<List<EntradaListaEspera>> getAll() {
        return ResponseEntity.ok(entradaListaEsperaRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntradaListaEspera> getById(@PathVariable Long id) {
        return entradaListaEsperaRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<List<EntradaListaEspera>> getByPaciente(@PathVariable Long pacienteId) {
        return ResponseEntity.ok(
            entradaListaEsperaRepository.findByPacienteId(pacienteId)
        );
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<EntradaListaEspera>> getByEstado(@PathVariable EstadoListaEspera estado) {
        return ResponseEntity.ok(
            entradaListaEsperaRepository.findByEstado(estado)
        );
    }

    @GetMapping("/especialidad/{especialidadId}")
    public ResponseEntity<List<EntradaListaEspera>> getByEspecialidadOrderedByPriority(
            @PathVariable Long especialidadId) {
        return ResponseEntity.ok(
            entradaListaEsperaRepository
                .findByEstadoAndEspecialidadIdOrderByPuntajePrioridadDescFechaRegistroAsc(
                    EstadoListaEspera.PENDIENTE, especialidadId
                )
        );
    }

    @PostMapping
    public ResponseEntity<EntradaListaEspera> create(@RequestBody EntradaListaEspera entrada) {
        boolean alreadyExists = entradaListaEsperaRepository
            .existsByPacienteIdAndEspecialidadIdAndEstadoIn(
                entrada.getPaciente().getId(),
                entrada.getEspecialidad().getId(),
                List.of(EstadoListaEspera.PENDIENTE, EstadoListaEspera.ASIGNADO)
            );

        if (alreadyExists) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        entrada.setFechaRegistro(LocalDateTime.now().toString());
        entrada.setEstado(EstadoListaEspera.PENDIENTE);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(entradaListaEsperaRepository.save(entrada));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntradaListaEspera> update(@PathVariable Long id,
                                                    @RequestBody EntradaListaEspera updated) {
        return entradaListaEsperaRepository.findById(id)
            .map(entrada -> {
                entrada.setEstado(updated.getEstado());
                entrada.setDoctorAsignado(updated.getDoctorAsignado());
                entrada.setFechaEstimadaAtencion(updated.getFechaEstimadaAtencion());
                entrada.setPuntajePrioridad(updated.getPuntajePrioridad());
                entrada.setNotasClinicas(updated.getNotasClinicas());
                return ResponseEntity.ok(entradaListaEsperaRepository.save(entrada));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    // Al cancelar una entrada
    @PutMapping("/{id}/cancel")
    public ResponseEntity<EntradaListaEspera> cancel(@PathVariable Long id) {
        return entradaListaEsperaRepository.findById(id)
            .map(entry -> {
                kafkaTemplate.send("waiting-list-events", "CANCELACION:" + id);
                return ResponseEntity.ok(entry);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!entradaListaEsperaRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        entradaListaEsperaRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
