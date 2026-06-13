package com.alexiz.service;

import org.springframework.stereotype.Service;

import com.alexiz.model.CitaEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

@Service
public class AwsLambdaService {

	public void invocarNotificacionSms(CitaEvent cita) {
		// 1. Configuramos el cliente de AWS (Region donde esté tu Lambda)
		LambdaClient awsLambda = LambdaClient.builder().region(Region.US_EAST_1).build();

		try {
			// 2. Convertimos el objeto cita a JSON para el Payload
			ObjectMapper mapper = new ObjectMapper();
			String jsonPayload = mapper.writeValueAsString(cita);

			// 3. Creamos la petición de invocación
			// Nombre de tu función en AWS
			InvokeRequest request = InvokeRequest.builder().functionName("notificarCitaMedicaLambda")
					.payload(SdkBytes.fromUtf8String(jsonPayload)).build();

			// 4. Invocamos la Lambda
			System.out.println("[AWS LAMBDA] Invocando función para envío de SMS...");
			InvokeResponse response = awsLambda.invoke(request);

			System.out.println("[AWS SUCCESS] Respuesta de Lambda: " + response.payload().asUtf8String());

		} catch (Exception e) {
			System.err.println("[AWS ERROR] Falló la comunicación con la Lambda: " + e.getMessage());
		}
	}

	public void invocarNotificacionCorreo(CitaEvent cita) {
		LambdaClient awsLambda = LambdaClient.builder().region(Region.US_EAST_1).build();

		try {
			ObjectMapper mapper = new ObjectMapper();
			String jsonPayload = mapper.writeValueAsString(cita);

			InvokeRequest request = InvokeRequest.builder().functionName("enviarCorreoClinica") // El nombre de la nueva
																								// lambda
					.payload(SdkBytes.fromUtf8String(jsonPayload)).build();

			InvokeResponse response = awsLambda.invoke(request);
			System.out.println("[AWS SES SUCCESS] Respuesta: " + response.payload().asUtf8String());

		} catch (Exception e) {
			System.err.println("[AWS ERROR] Falló el envío de correo: " + e.getMessage());
		}
	}
}