package com.example.demo.util;

import com.example.demo.model.Doctor;
import com.example.demo.model.DoctorProtos;
import com.example.demo.model.EntradaListaEspera;
import com.example.demo.model.EntradaListaEsperaProtos;
import com.example.demo.model.EspecialidadMedica;
import com.example.demo.model.EspecialidadMedicaProtos;
import com.example.demo.model.Paciente;
import com.example.demo.model.PacienteProtos;

public class ProtobufMapper {

    // --- Paciente ---
    public static PacienteProtos.Paciente toProto(Paciente paciente) {
        if (paciente == null) return null;
        
        return PacienteProtos.Paciente.newBuilder()
            .setId(paciente.getId() != null ? paciente.getId() : 0)
            .setRut(paciente.getRut() != null ? paciente.getRut() : "")
            .setNombreCompleto(paciente.getNombreCompleto() != null ? paciente.getNombreCompleto() : "")
            .setFechaNacimiento(paciente.getFechaNacimiento() != null ? paciente.getFechaNacimiento() : "")
            .setEmail(paciente.getEmail() != null ? paciente.getEmail() : "")
            .setTelefono(paciente.getTelefono() != null ? paciente.getTelefono() : "")
            .setSistemaSalud(paciente.getSistemaSalud() != null ? paciente.getSistemaSalud() : "")
            .build();
    }

    public static Paciente toEntity(PacienteProtos.Paciente proto) {
        if (proto == null) return null;

        Paciente paciente = new Paciente();
        if (proto.getId() != 0) paciente.setId(proto.getId());
        paciente.setRut(proto.getRut());
        paciente.setNombreCompleto(proto.getNombreCompleto());
        paciente.setFechaNacimiento(proto.getFechaNacimiento());
        paciente.setEmail(proto.getEmail());
        paciente.setTelefono(proto.getTelefono());
        paciente.setSistemaSalud(proto.getSistemaSalud());
        return paciente;
    }

    // --- Especialidad Medica ---
    public static EspecialidadMedicaProtos.EspecialidadMedica toProto(EspecialidadMedica especialidad) {
        if (especialidad == null) return null;

        return EspecialidadMedicaProtos.EspecialidadMedica.newBuilder()
            .setId(especialidad.getId() != null ? especialidad.getId() : 0)
            .setNombre(especialidad.getNombre() != null ? especialidad.getNombre() : "")
            .setCodigo(especialidad.getCodigo() != null ? especialidad.getCodigo() : "")
            .setDiasEsperaPromedio(especialidad.getDiasEsperaPromedio() != null ? especialidad.getDiasEsperaPromedio() : 0)
            .build();
    }

    public static EspecialidadMedica toEntity(EspecialidadMedicaProtos.EspecialidadMedica proto) {
        if (proto == null) return null;

        EspecialidadMedica especialidad = new EspecialidadMedica();
        if (proto.getId() != 0) especialidad.setId(proto.getId());
        especialidad.setNombre(proto.getNombre());
        especialidad.setCodigo(proto.getCodigo());
        especialidad.setDiasEsperaPromedio(proto.getDiasEsperaPromedio());
        return especialidad;
    }

    // --- Doctor ---
    public static DoctorProtos.Doctor toProto(Doctor doctor) {
        if (doctor == null) return null;

        DoctorProtos.Doctor.Builder builder = DoctorProtos.Doctor.newBuilder()
            .setId(doctor.getId() != null ? doctor.getId() : 0)
            .setNombreCompleto(doctor.getNombreCompleto() != null ? doctor.getNombreCompleto() : "")
            .setNumeroLicencia(doctor.getNumeroLicencia() != null ? doctor.getNumeroLicencia() : "")
            .setDisponible(doctor.getDisponible() != null ? doctor.getDisponible() : false);
            
        if (doctor.getEspecialidad() != null) {
            builder.setEspecialidad(toProto(doctor.getEspecialidad()));
        }
        return builder.build();
    }

    // In most use cases, creating doctor entities only requires ID mapping for foreign keys or setting scalar fields. Eager entity loading does the heavy lifting for foreign entities.
    public static Doctor toEntity(DoctorProtos.Doctor proto) {
        if (proto == null) return null;

        Doctor doctor = new Doctor();
        if (proto.getId() != 0) doctor.setId(proto.getId());
        doctor.setNombreCompleto(proto.getNombreCompleto());
        doctor.setNumeroLicencia(proto.getNumeroLicencia());
        doctor.setDisponible(proto.getDisponible());
        
        // Handling nested especialidad entity mapping might be needed depending on your needs.
        // By default, assuming updating properties locally if missing will be handled by the controller via lookup or passing IDs.
        return doctor;
    }

    // --- Entrada Lista Espera ---
    public static EntradaListaEsperaProtos.EntradaListaEspera toProto(EntradaListaEspera entrada) {
        if (entrada == null) return null;

        EntradaListaEsperaProtos.EntradaListaEspera.Builder builder = EntradaListaEsperaProtos.EntradaListaEspera.newBuilder()
            .setId(entrada.getId() != null ? entrada.getId() : 0)
            .setFechaRegistro(entrada.getFechaRegistro() != null ? entrada.getFechaRegistro() : "")
            .setFechaEstimadaAtencion(entrada.getFechaEstimadaAtencion() != null ? entrada.getFechaEstimadaAtencion() : "")
            .setPuntajePrioridad(entrada.getPuntajePrioridad() != null ? entrada.getPuntajePrioridad() : 0)
            .setNotasClinicas(entrada.getNotasClinicas() != null ? entrada.getNotasClinicas() : "");

        if (entrada.getPaciente() != null) {
            builder.setPacienteId(entrada.getPaciente().getId() != null ? entrada.getPaciente().getId() : 0);
        }
        if (entrada.getEspecialidad() != null) {
            builder.setEspecialidadId(entrada.getEspecialidad().getId() != null ? entrada.getEspecialidad().getId() : 0);
        }
        if (entrada.getDoctorAsignado() != null) {
            builder.setDoctorId(entrada.getDoctorAsignado().getId() != null ? entrada.getDoctorAsignado().getId() : 0);
        }
        if (entrada.getEstado() != null) {
            builder.setEstado(EntradaListaEsperaProtos.EstadoListaEspera.valueOf(entrada.getEstado().name()));
        }
        return builder.build();
    }

    public static EntradaListaEspera toEntity(EntradaListaEsperaProtos.EntradaListaEspera proto) {
        if (proto == null) return null;

        EntradaListaEspera entrada = new EntradaListaEspera();
        if (proto.getId() != 0) entrada.setId(proto.getId());
        entrada.setFechaRegistro(proto.getFechaRegistro());
        entrada.setFechaEstimadaAtencion(proto.getFechaEstimadaAtencion());
        entrada.setPuntajePrioridad(proto.getPuntajePrioridad());
        entrada.setNotasClinicas(proto.getNotasClinicas());
        return entrada;
    }
}
