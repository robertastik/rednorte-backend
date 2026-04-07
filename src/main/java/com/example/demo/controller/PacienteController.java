package com.example.demo.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Paciente;
import com.example.demo.model.PacienteProtos;
import com.example.demo.repository.PacienteRepository;
import com.example.demo.util.ProtobufMapper;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/pacientes")
@RequiredArgsConstructor
public class PacienteController {
    @Autowired
    private PacienteRepository pacienteRepository;

    @GetMapping
    public ResponseEntity<PacienteProtos.PacienteList> getAll() {
        List<Paciente> pacientes = pacienteRepository.findAll();
        List<PacienteProtos.Paciente> protos = pacientes.stream()
                .map(ProtobufMapper::toProto)
                .collect(Collectors.toList());
        
        PacienteProtos.PacienteList response = PacienteProtos.PacienteList.newBuilder()
                .addAllPacientes(protos)
                .build();
                
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PacienteProtos.Paciente> getById(@PathVariable Long id) {
        return pacienteRepository.findById(id)
            .map(ProtobufMapper::toProto)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/rut/{rut}")
    public ResponseEntity<PacienteProtos.Paciente> getByRut(@PathVariable String rut) {
        return pacienteRepository.findByRut(rut)
            .map(ProtobufMapper::toProto)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<PacienteProtos.PacienteList> searchByName(@RequestParam String nombre) {
        List<Paciente> pacientes = pacienteRepository.findByNombreCompletoContainingIgnoreCase(nombre);
        List<PacienteProtos.Paciente> protos = pacientes.stream()
                .map(ProtobufMapper::toProto)
                .collect(Collectors.toList());
                
        PacienteProtos.PacienteList response = PacienteProtos.PacienteList.newBuilder()
                .addAllPacientes(protos)
                .build();
                
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<PacienteProtos.Paciente> create(@RequestBody PacienteProtos.Paciente protoPaciente) {
        if (pacienteRepository.existsByRut(protoPaciente.getRut())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        if (pacienteRepository.existsByEmail(protoPaciente.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        
        Paciente paciente = ProtobufMapper.toEntity(protoPaciente);
        Paciente saved = pacienteRepository.save(paciente);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ProtobufMapper.toProto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PacienteProtos.Paciente> update(@PathVariable Long id,
                                          @RequestBody PacienteProtos.Paciente updatedProto) {
        return pacienteRepository.findById(id)
            .map(paciente -> {
                paciente.setNombreCompleto(updatedProto.getNombreCompleto());
                paciente.setEmail(updatedProto.getEmail());
                paciente.setTelefono(updatedProto.getTelefono());
                paciente.setSistemaSalud(updatedProto.getSistemaSalud());
                
                Paciente saved = pacienteRepository.save(paciente);
                return ResponseEntity.ok(ProtobufMapper.toProto(saved));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!pacienteRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        pacienteRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
