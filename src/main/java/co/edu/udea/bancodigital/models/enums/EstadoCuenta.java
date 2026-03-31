package co.edu.udea.bancodigital.models.enums;

import lombok.Getter;

@Getter
public enum EstadoCuenta {
	ACTIVA("Activa"),
	INACTIVA("Inactiva"),
	BLOQUEADA("Bloqueada");

	private final String nombre;

	EstadoCuenta(String nombre) {
		this.nombre = nombre;
	}
}
