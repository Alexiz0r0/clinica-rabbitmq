package com.alexiz.repository;

import org.springframework.stereotype.Repository;

import com.alexiz.model.CitaEvent;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;

@Repository
public class CitaRepository {

	@PersistenceContext
	private EntityManager entityManager;

	public void insertar(CitaEvent cita) {
		StoredProcedureQuery query = entityManager.createStoredProcedureQuery("pkg_paciente.sp_registrar_cita");

		query.registerStoredProcedureParameter("p_id_cita", String.class, ParameterMode.IN);
		query.registerStoredProcedureParameter("p_paciente", String.class, ParameterMode.IN);
		query.registerStoredProcedureParameter("p_especialidad", String.class, ParameterMode.IN);
		query.registerStoredProcedureParameter("p_tipo_atencion", String.class, ParameterMode.IN);
		query.registerStoredProcedureParameter("p_tipo_seguro", String.class, ParameterMode.IN);
		query.registerStoredProcedureParameter("p_prioridad", String.class, ParameterMode.IN);

		query.setParameter("p_id_cita", cita.getIdCita());
		query.setParameter("p_paciente", cita.getPaciente());
		query.setParameter("p_especialidad", cita.getEspecialidad());
		query.setParameter("p_tipo_atencion", cita.getTipoAtencion());
		query.setParameter("p_tipo_seguro", cita.getTipoSeguro());
		query.setParameter("p_prioridad", cita.getPrioridad());

		query.execute();
	}

}
