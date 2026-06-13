package com.alexiz.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CitaEvent implements Serializable {
    private String idCita;
    private String paciente;
    private String especialidad; // Ej: "cardiologia", "pediatria"
    private String tipoAtencion; // Ej: "urgencia", "control"
    private String tipoSeguro;   // Ej: "privado", "publico"
    private String prioridad;    // Ej: "alta", "media"
}