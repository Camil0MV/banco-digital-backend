package co.edu.udea.bancodigital.runners;

import io.cucumber.junit.CucumberOptions;
import net.serenitybdd.cucumber.CucumberWithSerenity;
import org.junit.runner.RunWith;

@RunWith(CucumberWithSerenity.class)
@CucumberOptions(
        features = "src/test/resources/features/transferencias.feature",
        glue = "co.edu.udea.bancodigital.stepdefinitions",
        snippets = CucumberOptions.SnippetType.CAMELCASE
)
public class TransferenciasRunner {}