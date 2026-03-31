package co.edu.udea.bancodigital.models.enums;

import lombok.Getter;

@Getter
public enum Rol {
	CLIENTE("Cliente"),
	ADMINISTRADOR("Administrador");

	private final String nombre;

	Rol(String nombre) {
		this.nombre = nombre;
	}
}
