package com.example.demo.controller;

import java.util.List;

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
import com.example.demo.repository.PacienteRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/pacientes")
@RequiredArgsConstructor
public class PacienteController {
    private final PacienteRepository pacienteRepository;

    @GetMapping
    public ResponseEntity<List<Paciente>> getAll() {
        return ResponseEntity.ok(pacienteRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Paciente> getById(@PathVariable Long id) {
        return pacienteRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/rut/{rut}")
    public ResponseEntity<Paciente> getByRut(@PathVariable String rut) {
        return pacienteRepository.findByRut(rut)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Paciente>> searchByName(@RequestParam String nombre) {
        return ResponseEntity.ok(
            pacienteRepository.findByNombreCompletoContainingIgnoreCase(nombre)
        );
    }

    @PostMapping
    public ResponseEntity<Paciente> create(@RequestBody Paciente paciente) {
        if (pacienteRepository.existsByRut(paciente.getRut())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        if (pacienteRepository.existsByEmail(paciente.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(pacienteRepository.save(paciente));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Paciente> update(@PathVariable Long id,
                                          @RequestBody Paciente updated) {
        return pacienteRepository.findById(id)
            .map(paciente -> {
                paciente.setNombreCompleto(updated.getNombreCompleto());
                paciente.setEmail(updated.getEmail());
                paciente.setTelefono(updated.getTelefono());
                paciente.setSistemaSalud(updated.getSistemaSalud());
                return ResponseEntity.ok(pacienteRepository.save(paciente));
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
