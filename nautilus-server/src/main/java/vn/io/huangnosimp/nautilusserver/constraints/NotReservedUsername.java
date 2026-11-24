package vn.io.huangnosimp.nautilusserver.constraints;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Rejects usernames that collide with the client's top-level routes: profiles live at {@code
 * /{username}}, so such a profile would be unreachable.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NotReservedUsernameValidator.class)
public @interface NotReservedUsername {
    String message() default "This username is reserved";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
