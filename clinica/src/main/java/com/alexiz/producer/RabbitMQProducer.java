package com.alexiz.producer;


import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.alexiz.event.CitaCreadaSpringEvent;
import com.alexiz.model.CitaEvent;

@Component
public class RabbitMQProducer {

    private final RabbitTemplate rabbitTemplate;
    private final MessageConverter messageConverter;

    public RabbitMQProducer(RabbitTemplate rabbitTemplate, MessageConverter messageConverter) {
        this.rabbitTemplate = rabbitTemplate;
        this.messageConverter = messageConverter;
    }

    @EventListener
    public void handleCitaCreadaEvent(CitaCreadaSpringEvent event) {
        CitaEvent cita = event.getCitaEvent();
        System.out.println("RabbitMQProducer capturó el evento de Spring. Procesando envío a los 4 Exchanges...");

        // 1. DIRECT EXCHANGE (Urgencias)
        if ("urgencia".equalsIgnoreCase(cita.getTipoAtencion())) {
            rabbitTemplate.convertAndSend("clinica.direct", "urgencia", cita);
        }

        // 2. TOPIC EXCHANGE (Especialidades: cita.cardiologia, cita.pediatria)
        String topicRoutingKey = "cita." + cita.getEspecialidad().toLowerCase();
        rabbitTemplate.convertAndSend("clinica.topic", topicRoutingKey, cita);

        // 3. FANOUT EXCHANGE (Auditoría Global)
        rabbitTemplate.convertAndSend("clinica.fanout", "", cita);

        // 4. HEADER EXCHANGE (Seguros Premium / VIP)
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setHeader("x-tipo-seguro", cita.getTipoSeguro());
        messageProperties.setHeader("x-prioridad", cita.getPrioridad());
        
        Message message = messageConverter.toMessage(cita, messageProperties);
        rabbitTemplate.send("clinica.headers", "", message);
    }
}