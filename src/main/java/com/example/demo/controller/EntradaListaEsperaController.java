package com.example.demo.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
import com.example.demo.model.EntradaListaEsperaProtos;
import com.example.demo.model.enums.EstadoListaEspera;
import com.example.demo.repository.DoctorRepository;
import com.example.demo.repository.EntradaListaEsperaRepository;
import com.example.demo.repository.EspecialidadMedicaRepository;
import com.example.demo.repository.PacienteRepository;
import com.example.demo.util.ProtobufMapper;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/lista-espera")
@RequiredArgsConstructor
public class EntradaListaEsperaController {
    @Autowired
    private EntradaListaEsperaRepository entradaListaEsperaRepository;
    
    @Autowired
    private PacienteRepository pacienteRepository;
    
    @Autowired
    private EspecialidadMedicaRepository especialidadRepository;
    
    @Autowired
    private DoctorRepository doctorRepository;

    @GetMapping
    public ResponseEntity<EntradaListaEsperaProtos.ListaEntradas> getAll() {
        List<EntradaListaEspera> entradas = entradaListaEsperaRepository.findAll();
        List<EntradaListaEsperaProtos.EntradaListaEspera> protos = entradas.stream()
                .map(ProtobufMapper::toProto)
                .collect(Collectors.toList());
                
        EntradaListaEsperaProtos.ListaEntradas response = EntradaListaEsperaProtos.ListaEntradas.newBuilder()
                .addAllEntradas(protos)
                .build();
                
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntradaListaEsperaProtos.EntradaListaEspera> getById(@PathVariable Long id) {
        return entradaListaEsperaRepository.findById(id)
            .map(ProtobufMapper::toProto)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<EntradaListaEsperaProtos.ListaEntradas> getByPaciente(@PathVariable Long pacienteId) {
        List<EntradaListaEspera> entradas = entradaListaEsperaRepository.findByPacienteId(pacienteId);
        List<EntradaListaEsperaProtos.EntradaListaEspera> protos = entradas.stream()
                .map(ProtobufMapper::toProto)
                .collect(Collectors.toList());
                
        EntradaListaEsperaProtos.ListaEntradas response = EntradaListaEsperaProtos.ListaEntradas.newBuilder()
                .addAllEntradas(protos)
                .build();
                
        return ResponseEntity.ok(response);
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<EntradaListaEsperaProtos.ListaEntradas> getByEstado(@PathVariable EstadoListaEspera estado) {
        List<EntradaListaEspera> entradas = entradaListaEsperaRepository.findByEstado(estado);
        List<EntradaListaEsperaProtos.EntradaListaEspera> protos = entradas.stream()
                .map(ProtobufMapper::toProto)
                .collect(Collectors.toList());
                
        EntradaListaEsperaProtos.ListaEntradas response = EntradaListaEsperaProtos.ListaEntradas.newBuilder()
                .addAllEntradas(protos)
                .build();
                
        return ResponseEntity.ok(response);
    }

    @GetMapping("/especialidad/{especialidadId}")
    public ResponseEntity<EntradaListaEsperaProtos.ListaEntradas> getByEspecialidadOrderedByPriority(
            @PathVariable Long especialidadId) {
        List<EntradaListaEspera> entradas = entradaListaEsperaRepository
                .findByEstadoAndEspecialidadIdOrderByPuntajePrioridadDescFechaRegistroAsc(
                    EstadoListaEspera.PENDIENTE, especialidadId
                );
                
        List<EntradaListaEsperaProtos.EntradaListaEspera> protos = entradas.stream()
                .map(ProtobufMapper::toProto)
                .collect(Collectors.toList());
                
        EntradaListaEsperaProtos.ListaEntradas response = EntradaListaEsperaProtos.ListaEntradas.newBuilder()
                .addAllEntradas(protos)
                .build();
                
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<EntradaListaEsperaProtos.EntradaListaEspera> create(@RequestBody EntradaListaEsperaProtos.EntradaListaEspera proto) {
        boolean alreadyExists = entradaListaEsperaRepository
            .existsByPacienteIdAndEspecialidadIdAndEstadoIn(
                proto.getPacienteId(),
                proto.getEspecialidadId(),
                List.of(EstadoListaEspera.PENDIENTE, EstadoListaEspera.ASIGNADO)
            );

        if (alreadyExists) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        EntradaListaEspera entrada = ProtobufMapper.toEntity(proto);
        entrada.setFechaRegistro(LocalDateTime.now().toString());
        entrada.setEstado(EstadoListaEspera.PENDIENTE);
        
        if (proto.getPacienteId() != 0) {
            pacienteRepository.findById(proto.getPacienteId()).ifPresent(entrada::setPaciente);
        }
        if (proto.getEspecialidadId() != 0) {
            especialidadRepository.findById(proto.getEspecialidadId()).ifPresent(entrada::setEspecialidad);
        }
        if (proto.getDoctorId() != 0) {
            doctorRepository.findById(proto.getDoctorId()).ifPresent(entrada::setDoctorAsignado);
        }

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ProtobufMapper.toProto(entradaListaEsperaRepository.save(entrada)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntradaListaEsperaProtos.EntradaListaEspera> update(@PathVariable Long id,
                                                    @RequestBody EntradaListaEsperaProtos.EntradaListaEspera updatedProto) {
        return entradaListaEsperaRepository.findById(id)
            .map(entrada -> {
                if (updatedProto.getEstado() != null && updatedProto.getEstado() != EntradaListaEsperaProtos.EstadoListaEspera.UNRECOGNIZED) {
                    entrada.setEstado(EstadoListaEspera.valueOf(updatedProto.getEstado().name()));
                }
                if (updatedProto.getDoctorId() != 0) {
                    doctorRepository.findById(updatedProto.getDoctorId()).ifPresent(entrada::setDoctorAsignado);
                }
                entrada.setFechaEstimadaAtencion(updatedProto.getFechaEstimadaAtencion());
                entrada.setPuntajePrioridad(updatedProto.getPuntajePrioridad());
                entrada.setNotasClinicas(updatedProto.getNotasClinicas());
                return ResponseEntity.ok(ProtobufMapper.toProto(entradaListaEsperaRepository.save(entrada)));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    // Al cancelar una entrada
    @PutMapping("/{id}/cancel")
    public ResponseEntity<EntradaListaEsperaProtos.EntradaListaEspera> cancel(@PathVariable Long id) {
        return entradaListaEsperaRepository.findById(id)
            .map(entry -> {
                kafkaTemplate.send("waiting-list-events", "CANCELACION:" + id);
                return ResponseEntity.ok(ProtobufMapper.toProto(entry));
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
