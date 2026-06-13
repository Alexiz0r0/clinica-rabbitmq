package com.alexiz.config.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.alexiz.event.CitaCreadaSpringEvent;
import com.alexiz.model.CitaEvent;
import com.alexiz.repository.CitaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CitaService {

	private final ApplicationEventPublisher eventPublisher;
	private final CitaRepository citaRepository;

	public void registrarCita(CitaEvent cita) {
		// 1. Aquí iría la lógica de negocio (guardar en base de datos Oracle, etc.)

		log.info("Iniciando registro de cita [{}]", cita.getIdCita());

		citaRepository.insertar(cita);

		log.info("Cita registrada correctamente en Oracle [{}]", cita.getIdCita());

		// 2. Publicamos el evento de Spring de forma local (Desacoplamiento total)
		CitaCreadaSpringEvent springEvent = new CitaCreadaSpringEvent(this, cita);
		eventPublisher.publishEvent(springEvent);

		log.info("Evento publicado para la cita [{}]", cita.getIdCita());
	}
}