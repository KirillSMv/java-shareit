/*
package ru.practicum.shareit.validationGroups;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;
import java.time.LocalDateTime;

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StartBeforeEndValidator.class)
public @interface StartBeforeEnd {
    String message() default "{javax.validation.constraints.FutureOrPresent.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    LocalDateTime start();

}
*/
