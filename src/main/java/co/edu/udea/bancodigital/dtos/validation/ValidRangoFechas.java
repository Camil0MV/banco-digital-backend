package co.edu.udea.bancodigital.dtos.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidRangoFechasValidator.class)
@Documented
public @interface ValidRangoFechas {

    String message() default "El rango de fechas no puede ser superior a 90 días";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
