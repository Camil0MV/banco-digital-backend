package co.edu.udea.bancodigital.stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import net.serenitybdd.rest.SerenityRest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;

import co.edu.udea.bancodigital.support.SessionContext;

public class LoginStepDefinition {

    private Response response;

    @Given("el usuario quiere iniciar sesion")
    public void elUsuarioQuiereIniciarSesion() {
        response = null;
    }

    @When("envia credenciales validas {string} y {string}")
    public void enviaCredencialesValidas(String correo, String contrasena) {

        String body = String.format("""
                {
                  "correo": "%s",
                  "contrasena": "%s"
                }
                """, correo, contrasena);

        response =
                SerenityRest.given()
                        .contentType(ContentType.JSON)
                        .body(body)
                .when()
                        .post("http://localhost:8080/api/v1/auth/login");
    }

    @When("envia credenciales invalidas {string} y {string}")
    public void enviaCredencialesInvalidas(String correo, String contrasena) {

        String body = String.format("""
                {
                  "correo": "%s",
                  "contrasena": "%s"
                }
                """, correo, contrasena);

        response =
                SerenityRest.given()
                        .contentType(ContentType.JSON)
                        .body(body)
                .when()
                        .post("http://localhost:8080/api/v1/auth/login");
    }

    @Then("el login debe responder con estado {int}")
    public void elLoginDebeResponderConEstado(int statusCode) {

        response.then().log().all();
        assertEquals(statusCode, response.getStatusCode());
    }

    @Then("el login debe retornar un token")
    public void elLoginDebeRetornarUnToken() {

        String token = response.jsonPath().getString("token");

        SessionContext.setToken(token);

        response.then()
                .body("token", notNullValue());
    }
}