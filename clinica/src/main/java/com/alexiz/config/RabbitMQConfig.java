package com.alexiz.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.HeadersExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

	// 1. CONVERTIDOR JSON OBLIGATORIO (Criterios 4 y 5)
	@Bean
	public MessageConverter jsonMessageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	// --- EXCHANGES ---
	@Bean
	public DirectExchange directExchange() {
		return new DirectExchange("clinica.direct");
	}

	@Bean
	public TopicExchange topicExchange() {
		return new TopicExchange("clinica.topic");
	}

	@Bean
	public FanoutExchange fanoutExchange() {
		return new FanoutExchange("clinica.fanout");
	}

	@Bean
	public HeadersExchange headersExchange() {
		return new HeadersExchange("clinica.headers");
	}

	// --- QUEUES ---
	@Bean
	public Queue queueEmergencia() {
		return new Queue("queue.atencion.inmediata");
	}

//	@Bean
//	public Queue queueEspecialidades() {
//		return new Queue("queue.especialidades");
//	}

	@Bean
	public Queue queueAuditoria() {
		return new Queue("queue.auditoria");
	}

	@Bean
	public Queue queueSegurosPremium() {
		return new Queue("queue.seguros.premium");
	}

	// --- COMPONENTES PARA MANEJO DE ERRORES (Criterios 7 y 8) ---

	// 1. Intercambiador de Fallos (DLX)
	@Bean
	public DirectExchange deadLetterExchange() {
		return new DirectExchange("clinica.dlx");
	}

	// 2. Cola de Mensajes Muertos (DLQ)
	@Bean
	public Queue deadLetterQueue() {
		return new Queue("queue.dlq");
	}

	// 3. Cola del Parking Lot (PLQ)
	@Bean
	public Queue parkingLotQueue() {
		return new Queue("queue.parking.lot");
	}

	// --- BINDINGS (Enlaces) ---

	// Direct: Une si la routing key es "urgencia"
	@Bean
	public Binding bindingDirect(Queue queueEmergencia, DirectExchange directExchange) {
		return BindingBuilder.bind(queueEmergencia).to(directExchange).with("urgencia");
	}

	// Topic: Une si cumple el patrón "cita.*" (ej: cita.cardiologia,
	// cita.pediatria)
	@Bean
	public Binding bindingTopic(Queue queueEspecialidades, TopicExchange topicExchange) {
		return BindingBuilder.bind(queueEspecialidades).to(topicExchange).with("cita.*");
	}

	// Fanout: Une de forma global sin importar routing keys
	@Bean
	public Binding bindingFanout(Queue queueAuditoria, FanoutExchange fanoutExchange) {
		return BindingBuilder.bind(queueAuditoria).to(fanoutExchange);
	}

	// Headers: Une si los headers coinciden exactamente (Criterio 4.5)
	@Bean
	public Binding bindingHeaders(Queue queueSegurosPremium, HeadersExchange headersExchange) {
		Map<String, Object> headerValues = new HashMap<>();
		headerValues.put("x-tipo-seguro", "privado");
		headerValues.put("x-prioridad", "alta");

		// Evaluará que se cumplan TODOS los del mapa
		return BindingBuilder.bind(queueSegurosPremium).to(headersExchange).whereAll(headerValues).match();
		// Cierra la construcción del Binding
	}

	// 4. Enlace del DLX con la DLQ (Routing key: "fail")
	@Bean
	public Binding bindingDLX(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
		return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with("fail");
	}

	// 5. Enlace del DLX con la PLQ (Routing key: "park")
	@Bean
	public Binding bindingPLQ(Queue parkingLotQueue, DirectExchange deadLetterExchange) {
		return BindingBuilder.bind(parkingLotQueue).to(deadLetterExchange).with("park");
	}

	// --- MODIFICACIÓN DE LA COLA PRINCIPAL REQUERIDA (Criterio 8) ---
	// Reemplaza tu declaración anterior de 'queueEspecialidades' por esta:
	@Bean
	public Queue queueEspecialidades() {
		// Configura la routing key hacia la DLQ
		// Configura el DLX
		return QueueBuilder.durable("queue.especialidades").withArgument("x-dead-letter-exchange", "clinica.dlx")
				.withArgument("x-dead-letter-routing-key", "fail").build();
	}
}