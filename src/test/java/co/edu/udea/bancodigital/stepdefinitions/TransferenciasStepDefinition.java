package co.edu.udea.bancodigital.stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import net.serenitybdd.rest.SerenityRest;
import java.util.HashMap;
import java.util.Map;

public class TransferenciasStepDefinition {

    private static String tokenUsuario = "";

    @Given("el usuario tiene fondos en su cuenta para transferir")
    public void elUsuarioTieneFondosEnSuCuentaParaTransferir() {
        Map<String, Object> loginBody = new HashMap<>();
        loginBody.put("correo", "usuario.prueba@bancodigital.com");
        loginBody.put("contrasena", "Password456!");

        tokenUsuario = SerenityRest.given()
                .baseUri("http://localhost:8080")
                .contentType("application/json")
                .body(loginBody)
        .when()
                .post("/api/v1/auth/login")
        .then()
                .statusCode(200)
                .extract()
                .path("token");
    }

    @When("realiza una transferencia desde {string} hacia {string} por valor de {int}")
    public void realizaUnaTransferenciaExitosa(String cOrigen, String cDestino, int monto) {
        enviarPeticionTransferencia(cOrigen, cDestino, monto);
    }

    @When("realiza una transferencia desde {string} hacia {string} por valor de {string}")
    public void realizaUnaTransferenciaInvalida(String cOrigen, String cDestino, String monto) {
        Object montoFinal;
        try {
            montoFinal = Integer.parseInt(monto);
        } catch (NumberFormatException e) {
            montoFinal = monto;
        }
        enviarPeticionTransferencia(cOrigen, cDestino, montoFinal);
    }

    private void enviarPeticionTransferencia(String origen, String destino, Object monto) {
        Map<String, Object> body = new HashMap<>();
        body.put("cuentaOrigen", origen);   
        body.put("cuentaDestino", destino); 
        body.put("monto", monto);

        SerenityRest.given()
                .baseUri("http://localhost:8080")
                .contentType("application/json")
                .header("Authorization", "Bearer " + tokenUsuario)
                .body(body)
        .when()
                .post("/api/v1/transferencias");
    }

    @Then("el sistema de transferencias debe responder con estado {int}")
    public void elSistemaDeTransferenciasDebeResponderConEstado(Integer estadoEsperado) {
        SerenityRest.then()
                .statusCode(estadoEsperado);
    }
}