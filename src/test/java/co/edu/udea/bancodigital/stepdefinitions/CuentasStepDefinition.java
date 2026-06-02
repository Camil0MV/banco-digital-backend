package co.edu.udea.bancodigital.stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import net.serenitybdd.core.Serenity;
import org.junit.Assert;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class CuentasStepDefinition {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(java.time.Duration.ofSeconds(10)) 
            .build();

    private final String baseUrl = "http://localhost:8080";
    @Given("el usuario no se encuentra autenticado en el sistema")
    public void elUsuarioNoSeEncuentraAutenticado() {
        Serenity.setSessionVariable("TOKEN_USUARIO").to("TOKEN_INVALIDO_O_VACIO");
        System.out.println("Simulando petición sin autenticación válida.");
    }

    @Given("el usuario se autentica con el correo {string} y la contrasena {string}")
    public void elUsuarioSeAutentica(String correo, String contrasena) {
        String loginBody = String.format("{\"correo\":\"%s\",\"contrasena\":\"%s\"}", correo, contrasena);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/v1/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(loginBody))
                .build();

        int maxReintentos = 5;
        int intentoActual = 0;
        HttpResponse<String> response = null;

        while (intentoActual < maxReintentos) {
            try {
                intentoActual++;
                response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                break;
                
            } catch (java.net.ConnectException e) {
                System.out.println("El servidor no responde en el puerto 8080. Intento " + intentoActual + " de " + maxReintentos + "...");
                if (intentoActual == maxReintentos) {
                    throw new RuntimeException("El servidor definitivamente está caído o inaccesible tras " + maxReintentos + " intentos.", e);
                }
                try {
                    // Pausa de 3 segundos antes del siguiente intento
                    Thread.sleep(3000); 
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            } catch (Exception e) {
                throw new RuntimeException("Error inesperado en Autenticación Nativa: " + e.getMessage(), e);
            }
        }

        if (response != null && response.statusCode() == 200) {
            String body = response.body();
            int tokenIndex = body.indexOf("\"token\":\"");
            if (tokenIndex != -1) {
                int start = tokenIndex + 9;
                int end = body.indexOf("\"", start);
                String token = body.substring(start, end);
                Serenity.setSessionVariable("TOKEN_USUARIO").to(token);
                System.out.println("Autenticación exitosa y token guardado.");
            } else {
                throw new RuntimeException("No se encontró la propiedad 'token' en el JSON: " + body);
            }
        } else {
            throw new RuntimeException("Login fallido. HTTP Status: " + (response != null ? response.statusCode() : "Null") + " - " + (response != null ? response.body() : ""));
        }
    }

    @When("solicita el saldo de la cuenta {string}")
    public void solicitaElSaldoDeLaCuenta(String idCuenta) {
        String tokenUsuario = Serenity.sessionVariableCalled("TOKEN_USUARIO");
        
        if (tokenUsuario == null) {
            throw new RuntimeException("No hay token en la sesión. El Given falló o no se ejecutó.");
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/v1/cuentas/" + idCuenta + "/saldo"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + tokenUsuario)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            Serenity.setSessionVariable("STATUS_CODE").to(response.statusCode());
            
            System.out.println("=== RESPUESTA SALDO DE CUENTA ===");
            System.out.println("Status: " + response.statusCode());
            System.out.println("Body: " + response.body());
            
        } catch (Exception e) {
            throw new RuntimeException("Error en HTTP GET Nativo: " + e.getMessage(), e);
        }
    }

    @Then("el sistema financiero debe responder con estado {int}")
    public void elSistemaFinancieroDebeResponderConEstado(Integer estadoEsperado) {
        Integer statusCodeActual = Serenity.sessionVariableCalled("STATUS_CODE");
        
        Assert.assertEquals("El código de estado HTTP no coincide", estadoEsperado, statusCodeActual);
    }
}