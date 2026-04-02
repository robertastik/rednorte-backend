package com.example.demo.controller;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.example.demo.model.enums.EstadoListaEspera;
import com.example.demo.repository.DoctorRepository;
import com.example.demo.repository.EntradaListaEsperaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KafkaController {
    private final EntradaListaEsperaRepository entradaListaEsperaRepository;
    private final DoctorRepository doctorRepository;

    @KafkaListener(topics = "waiting-list-events", groupId = "hospital-group")
    private void procesarEventoListaEspera(String mensaje) {
        String[] datos = mensaje.split(":");
        String tipoEvento = datos[0];
        Long entradaId = Long.parseLong(datos[1]);

        if ("CANCELACION".equals(tipoEvento)) {
            entradaListaEsperaRepository.findById(entradaId).ifPresent(entrada -> {
                entrada.setEstado(EstadoListaEspera.CANCELADO);
                entradaListaEsperaRepository.save(entrada);
            });
        }
    }

    @KafkaListener(topics = "doctor-events", groupId = "hospital-group")
    private void procesarEventoDoctor(String mensaje) {
        String[] datos = mensaje.split(":");
        Long doctorId = Long.parseLong(datos[0]);
        boolean disponible = Boolean.parseBoolean(datos[1]);

        doctorRepository.findById(doctorId).ifPresent(doctor -> {
            doctor.setDisponible(disponible);
            doctorRepository.save(doctor);
        });
    }
}
