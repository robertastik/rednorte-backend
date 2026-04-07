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
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.EspecialidadMedica;
import com.example.demo.model.EspecialidadMedicaProtos;
import com.example.demo.repository.EspecialidadMedicaRepository;
import com.example.demo.util.ProtobufMapper;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/especialidades")
@RequiredArgsConstructor
public class EspecialidadMedicaController {
    @Autowired
    private EspecialidadMedicaRepository especialidadRepository;

    @GetMapping
    public ResponseEntity<EspecialidadMedicaProtos.EspecialidadMedicaList> getAll() {
        List<EspecialidadMedica> especialidades = especialidadRepository.findAll();
        List<EspecialidadMedicaProtos.EspecialidadMedica> protos = especialidades.stream()
                .map(ProtobufMapper::toProto)
                .collect(Collectors.toList());
                
        EspecialidadMedicaProtos.EspecialidadMedicaList response = EspecialidadMedicaProtos.EspecialidadMedicaList.newBuilder()
                .addAllEspecialidades(protos)
                .build();
                
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EspecialidadMedicaProtos.EspecialidadMedica> getById(@PathVariable Long id) {
        return especialidadRepository.findById(id)
            .map(ProtobufMapper::toProto)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<EspecialidadMedicaProtos.EspecialidadMedica> create(@RequestBody EspecialidadMedicaProtos.EspecialidadMedica proto) {
        if (especialidadRepository.existsByCodigo(proto.getCodigo())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        
        EspecialidadMedica especialidad = ProtobufMapper.toEntity(proto);
        EspecialidadMedica saved = especialidadRepository.save(especialidad);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ProtobufMapper.toProto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EspecialidadMedicaProtos.EspecialidadMedica> update(@PathVariable Long id,
                                                    @RequestBody EspecialidadMedicaProtos.EspecialidadMedica updatedProto) {
        return especialidadRepository.findById(id)
            .map(especialidad -> {
                especialidad.setNombre(updatedProto.getNombre());
                especialidad.setCodigo(updatedProto.getCodigo());
                especialidad.setDiasEsperaPromedio(updatedProto.getDiasEsperaPromedio());
                
                EspecialidadMedica saved = especialidadRepository.save(especialidad);
                return ResponseEntity.ok(ProtobufMapper.toProto(saved));
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
