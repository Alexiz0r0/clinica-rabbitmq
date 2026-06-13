package com.alexiz.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.alexiz.model.CitaEvent;
import com.alexiz.service.AwsLambdaService;

@Component
public class RabbitMQConsumer {

	private final RabbitTemplate rabbitTemplate;
	private final AwsLambdaService awsLambdaService;

	public RabbitMQConsumer(RabbitTemplate rabbitTemplate, AwsLambdaService awsLambdaService) {
		this.rabbitTemplate = rabbitTemplate;
		this.awsLambdaService = awsLambdaService;
	}

	@RabbitListener(queues = "queue.atencion.inmediata")
	public void consumeDirect(CitaEvent cita) {
		System.out.println("[CONSUMER DIRECT] ¡Alerta de Emergencia recibida!: " + cita);
	}

//	@RabbitListener(queues = "queue.especialidades")
//	public void consumeTopic(CitaEvent cita) {
//		System.out
//				.println("[CONSUMER TOPIC] Cita asignada a la especialidad de " + cita.getEspecialidad() + ": " + cita);
//	}

	@RabbitListener(queues = "queue.auditoria")
	public void consumeFanout(CitaEvent cita) {
		System.out.println(
				"[CONSUMER FANOUT] Registro histórico de auditoría guardado para la cita: " + cita.getIdCita());
	}

	@RabbitListener(queues = "queue.seguros.premium")
	public void consumeHeaders(CitaEvent cita) {
		System.out.println("[CONSUMER HEADERS] Atención prioritaria VIP para seguro privado: " + cita.getPaciente());
	}

	// CONSUMIDOR PRINCIPAL DE ESPECIALIDADES
	@RabbitListener(queues = "queue.especialidades")
	public void consumeTopic(CitaEvent cita) {
		System.out.println("[CONSUMER] Procesando cita: " + cita.getIdCita());

		// Simulamos un error controlado para probar la tolerancia a fallos
		if ("CITA-ERRONEA".equalsIgnoreCase(cita.getIdCita())) {
			System.err.println(
					"[CONSUMER ERROR] Error crítico procesando la cita. Lanzando excepción para activar reintentos...");
			throw new RuntimeException("Simulación de caída del servicio de Historias Clínicas.");
		}

		awsLambdaService.invocarNotificacionCorreo(cita);

		System.out.println("[CONSUMER SUCCESS] Cita procesada correctamente.");
	}

	// CONSUMIDOR DE LA DEAD LETTER QUEUE (DLQ) - Criterio 8
	@RabbitListener(queues = "queue.dlq")
	public void consumeDLQ(CitaEvent cita) {
		System.out.println("[DLQ CONSUMER] Mensaje recibido en la DLQ. Agotó los 3 reintentos en la cola principal.");

		try {
			// Aquí se podría intentar una lógica de recuperación de última hora.
			System.out.println("[DLQ] Intentando reparación de emergencia para la cita: " + cita.getIdCita());

			// Si determinamos que el mensaje es completamente irrecuperable (ej. JSON mal
			// formado o ID corrupto):
			throw new Exception("Reparación fallida de forma definitiva.");

		} catch (Exception e) {
			System.err.println(
					"[DLQ -> PARKING LOT] Enviando mensaje de forma definitiva a la Parking Lot Queue (PLQ) para revisión manual.");
			// Publicamos directamente en el DLX con la routing key del Parking Lot ("park")
			rabbitTemplate.convertAndSend("clinica.dlx", "park", cita);
		}
	}

	// CONSUMIDOR DEL PARKING LOT (Sólo para evidenciar logs en el examen)
	@RabbitListener(queues = "queue.parking.lot")
	public void consumeParkingLot(CitaEvent cita) {
		System.err.println(
				"[PARKING LOT] ¡ALERTA! Mensaje retenido en el Parking Lot para auditoría del administrador: " + cita);
	}

}