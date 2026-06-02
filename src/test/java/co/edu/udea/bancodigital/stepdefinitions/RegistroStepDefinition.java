package co.edu.udea.bancodigital.stepdefinitions;

import static net.serenitybdd.rest.SerenityRest.given;
import static net.serenitybdd.rest.SerenityRest.then;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class RegistroStepDefinition {

    @Given("el usuario quiere registrarse")
    public void elUsuarioQuiereRegistrarse() {
    }

    @When("envia un registro valido dinamico")
    public void enviaUnRegistroValidoDinamico() {

        String uniqueId = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8);

        String correo = "usuario" + uniqueId + "@gmail.com";

        String documento = String.valueOf(
                (long) (Math.random() * 900000000L) + 100000000L
        );

        Map<String, Object> body = new HashMap<>();

        body.put("idTipoDoc", 1);
        body.put("numeroDocumento", documento);
        body.put("nombre", "Pablo");
        body.put("primerApellido", "Perez");
        body.put("segundoApellido", "Gomez");
        body.put("direccion", "Calle 123");
        body.put("telefono", "3001234567");
        body.put("correo", correo);
        body.put("contrasena", "Abc123#@");

        given()
                .baseUri("http://localhost:8080")
                .contentType("application/json")
                .log().all()
                .body(body)
        .when()
                .post("/api/v1/usuarios/registro");
    }

    @When("envia un registro con {string} y {string}")
    public void enviaUnRegistroCon(String correo, String documento) {

        Map<String, Object> body = new HashMap<>();

        body.put("idTipoDoc", 1);
        body.put("numeroDocumento", documento);
        body.put("nombre", "Pablo");
        body.put("primerApellido", "Perez");
        body.put("segundoApellido", "Gomez");
        body.put("direccion", "Calle 123");
        body.put("telefono", "3001234567");
        body.put("correo", correo);
        body.put("contrasena", "Abc123#@");

        given()
                .baseUri("http://localhost:8080")
                .contentType("application/json")
                .log().all()
                .body(body)
        .when()
                .post("/api/v1/usuarios/registro");
    }

    @Then("el sistema debe responder con estado {int}")
    public void elSistemaDebeResponderConEstado(int status) {

        then().log().all();

        then().statusCode(status);
    }
}