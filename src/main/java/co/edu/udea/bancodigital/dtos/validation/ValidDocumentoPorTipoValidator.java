package co.edu.udea.bancodigital.dtos.validation;

import co.edu.udea.bancodigital.dtos.requests.RegistroRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidDocumentoPorTipoValidator implements ConstraintValidator<ValidDocumentoPorTipo, RegistroRequest> {

    @Override
    public boolean isValid(RegistroRequest request, ConstraintValidatorContext context) {
        if (request == null || request.getIdTipoDoc() == null || request.getNumeroDocumento() == null
                || request.getNumeroDocumento().isBlank()) {
            return true;
        }

        Integer idTipoDoc = request.getIdTipoDoc();
        String numeroDocumento = request.getNumeroDocumento().trim();

        boolean valid;
        String message;

        switch (idTipoDoc) {
            case 1: // CC
                valid = numeroDocumento.matches("^\\d{6,10}$");
                message = "Para CC, numeroDocumento debe tener entre 6 y 10 digitos";
                break;
            case 2: // CE
                valid = numeroDocumento.matches("^[A-Za-z0-9]{6,12}$");
                message = "Para CE, numeroDocumento debe ser alfanumerico entre 6 y 12 caracteres";
                break;
            case 3: // Pasaporte
                valid = numeroDocumento.matches("^[A-Za-z0-9]{6,20}$");
                message = "Para pasaporte, numeroDocumento debe ser alfanumerico entre 6 y 20 caracteres";
                break;
            case 4: // NIT
                valid = numeroDocumento.matches("^(\\d{9,10}|\\d{9,10}-\\d)$");
                message = "Para NIT, numeroDocumento debe tener 9 o 10 digitos (opcionalmente con guion y DV)";
                break;
            case 5: // TI
                valid = numeroDocumento.matches("^\\d{10,11}$");
                message = "Para TI, numeroDocumento debe tener 10 u 11 digitos";
                break;
            default:
                valid = false;
                message = "idTipoDoc no soportado para validacion automatica";
                break;
        }

        if (valid) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode("numeroDocumento")
                .addConstraintViolation();

        return false;
    }
}
