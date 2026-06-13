package com.alexiz.event;

import org.springframework.context.ApplicationEvent;

import com.alexiz.model.CitaEvent;

public class CitaCreadaSpringEvent extends ApplicationEvent {
	private final CitaEvent citaEvent;

	public CitaCreadaSpringEvent(Object source, CitaEvent citaEvent) {
		super(source);
		this.citaEvent = citaEvent;
	}

	public CitaEvent getCitaEvent() {
		return citaEvent;
	}
}