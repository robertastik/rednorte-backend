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
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.EspecialidadMedica;
import com.example.demo.repository.EspecialidadMedicaRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/especialidades")
@RequiredArgsConstructor
public class EspecialidadMedicaController {
    private final EspecialidadMedicaRepository especialidadRepository;

    @GetMapping
    public ResponseEntity<List<EspecialidadMedica>> getAll() {
        return ResponseEntity.ok(especialidadRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EspecialidadMedica> getById(@PathVariable Long id) {
        return especialidadRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<EspecialidadMedica> create(@RequestBody EspecialidadMedica especialidad) {
        if (especialidadRepository.existsByCodigo(especialidad.getCodigo())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(especialidadRepository.save(especialidad));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EspecialidadMedica> update(@PathVariable Long id,
                                                    @RequestBody EspecialidadMedica updated) {
        return especialidadRepository.findById(id)
            .map(especialidad -> {
                especialidad.setNombre(updated.getNombre());
                especialidad.setCodigo(updated.getCodigo());
                especialidad.setDiasEsperaPromedio(updated.getDiasEsperaPromedio());
                return ResponseEntity.ok(especialidadRepository.save(especialidad));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!especialidadRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        especialidadRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
