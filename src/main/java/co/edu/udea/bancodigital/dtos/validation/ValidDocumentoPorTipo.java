package co.edu.udea.bancodigital.dtos.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidDocumentoPorTipoValidator.class)
@Documented
public @interface ValidDocumentoPorTipo {

    String message() default "El numeroDocumento no coincide con el idTipoDoc seleccionado";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
