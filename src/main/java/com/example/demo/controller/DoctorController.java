package com.example.demo.controller;

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

import com.example.demo.model.Doctor;
import com.example.demo.repository.DoctorRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/doctores")
@RequiredArgsConstructor
public class DoctorController {
    private final DoctorRepository doctorRepository;

    @GetMapping
    public ResponseEntity<List<Doctor>> getAll() {
        return ResponseEntity.ok(doctorRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Doctor> getById(@PathVariable Long id) {
        return doctorRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/available")
    public ResponseEntity<List<Doctor>> getAvailable() {
        return ResponseEntity.ok(doctorRepository.findByDisponibleTrue());
    }

    @GetMapping("/especialidad/{especialidadId}/available")
    public ResponseEntity<List<Doctor>> getAvailableByEspecialty(@PathVariable Long especialidadId) {
        return ResponseEntity.ok(
            doctorRepository.findByEspecialidadIdAndDisponibleTrue(especialidadId)
        );
    }

    @PostMapping
    public ResponseEntity<Doctor> create(@RequestBody Doctor doctor) {
        if (doctorRepository.existsByNumeroLicencia(doctor.getNumeroLicencia())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(doctorRepository.save(doctor));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Doctor> update(@PathVariable Long id,
                                         @RequestBody Doctor updated) {
        return doctorRepository.findById(id)
            .map(doctor -> {
                doctor.setNombreCompleto(updated.getNombreCompleto());
                doctor.setDisponible(updated.getDisponible());
                doctor.setEspecialidad(updated.getEspecialidad());
                Doctor saved = doctorRepository.save(doctor);
                kafkaTemplate.send("doctor-events",
                    id + ":" + updated.getDisponible());
                return ResponseEntity.ok(saved);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!doctorRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        doctorRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
