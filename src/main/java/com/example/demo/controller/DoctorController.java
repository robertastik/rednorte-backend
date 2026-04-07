package com.example.demo.controller;

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

import com.example.demo.model.Doctor;
import com.example.demo.model.DoctorProtos;
import com.example.demo.repository.DoctorRepository;
import com.example.demo.repository.EspecialidadMedicaRepository;
import com.example.demo.util.ProtobufMapper;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/doctores")
@RequiredArgsConstructor
public class DoctorController {
    private final DoctorRepository doctorRepository;
    
    @Autowired
    private EspecialidadMedicaRepository especialidadRepository;

    @GetMapping
    public ResponseEntity<DoctorProtos.DoctorList> getAll() {
        List<Doctor> doctores = doctorRepository.findAll();
        List<DoctorProtos.Doctor> protos = doctores.stream()
                .map(ProtobufMapper::toProto)
                .collect(Collectors.toList());
                
        DoctorProtos.DoctorList response = DoctorProtos.DoctorList.newBuilder()
                .addAllDoctors(protos)
                .build();
                
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorProtos.Doctor> getById(@PathVariable Long id) {
        return doctorRepository.findById(id)
            .map(ProtobufMapper::toProto)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/available")
    public ResponseEntity<DoctorProtos.DoctorList> getAvailable() {
        List<Doctor> doctores = doctorRepository.findByDisponibleTrue();
        List<DoctorProtos.Doctor> protos = doctores.stream()
                .map(ProtobufMapper::toProto)
                .collect(Collectors.toList());
                
        DoctorProtos.DoctorList response = DoctorProtos.DoctorList.newBuilder()
                .addAllDoctors(protos)
                .build();
                
        return ResponseEntity.ok(response);
    }

    @GetMapping("/especialidad/{especialidadId}/available")
    public ResponseEntity<DoctorProtos.DoctorList> getAvailableByEspecialty(@PathVariable Long especialidadId) {
        List<Doctor> doctores = doctorRepository.findByEspecialidadIdAndDisponibleTrue(especialidadId);
        List<DoctorProtos.Doctor> protos = doctores.stream()
                .map(ProtobufMapper::toProto)
                .collect(Collectors.toList());
                
        DoctorProtos.DoctorList response = DoctorProtos.DoctorList.newBuilder()
                .addAllDoctors(protos)
                .build();
                
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<DoctorProtos.Doctor> create(@RequestBody DoctorProtos.Doctor proto) {
        if (doctorRepository.existsByNumeroLicencia(proto.getNumeroLicencia())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        
        Doctor doctor = ProtobufMapper.toEntity(proto);
        
        // Asignar especialidad manejada manualmente si se recibe el ID en el proto nested
        if (proto.hasEspecialidad() && proto.getEspecialidad().getId() != 0) {
            especialidadRepository.findById(proto.getEspecialidad().getId()).ifPresent(doctor::setEspecialidad);
        }
        
        Doctor saved = doctorRepository.save(doctor);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ProtobufMapper.toProto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DoctorProtos.Doctor> update(@PathVariable Long id,
                                         @RequestBody DoctorProtos.Doctor updatedProto) {
        return doctorRepository.findById(id)
            .map(doctor -> {
                doctor.setNombreCompleto(updatedProto.getNombreCompleto());
                doctor.setDisponible(updatedProto.getDisponible());
                
                if (updatedProto.hasEspecialidad() && updatedProto.getEspecialidad().getId() != 0) {
                    especialidadRepository.findById(updatedProto.getEspecialidad().getId()).ifPresent(doctor::setEspecialidad);
                }
                
                Doctor saved = doctorRepository.save(doctor);
                kafkaTemplate.send("doctor-events",
                    id + ":" + updatedProto.getDisponible());
                return ResponseEntity.ok(ProtobufMapper.toProto(saved));
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
