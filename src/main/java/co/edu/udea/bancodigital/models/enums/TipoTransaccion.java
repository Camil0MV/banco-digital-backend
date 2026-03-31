package co.edu.udea.bancodigital.models.enums;

import lombok.Getter;

@Getter
public enum TipoTransaccion {
	TRANSFERENCIA("Transferencia"),
	DEPOSITO("Depósito"),
	RETIRO("Retiro");

	private final String nombre;

	TipoTransaccion(String nombre) {
		this.nombre = nombre;
	}
}
