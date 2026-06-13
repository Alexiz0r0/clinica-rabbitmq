package com.alexiz.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alexiz.config.service.CitaService;
import com.alexiz.model.CitaEvent;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    private final CitaService citaService;

    public CitaController(CitaService citaService) {
        this.citaService = citaService;
    }

    @PostMapping
    public ResponseEntity<String> crearCita(@RequestBody CitaEvent cita) {
        citaService.registrarCita(cita);
        return ResponseEntity.ok("Proceso de simulación de cita iniciado.");
    }
}