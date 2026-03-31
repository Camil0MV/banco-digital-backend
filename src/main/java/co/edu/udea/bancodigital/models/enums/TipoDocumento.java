package co.edu.udea.bancodigital.models.enums;

import lombok.Getter;

@Getter
public enum TipoDocumento {
	CC("Cédula de ciudadanía"),
	CE("Cédula de extranjería"),
	PASAPORTE("Pasaporte"),
	TI("Tarjeta de identidad");

	private final String nombre;

	TipoDocumento(String nombre) {
		this.nombre = nombre;
	}
}
