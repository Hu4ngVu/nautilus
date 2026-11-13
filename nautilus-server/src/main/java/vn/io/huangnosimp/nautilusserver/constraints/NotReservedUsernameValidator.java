package vn.io.huangnosimp.nautilusserver.constraints;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Locale;
import java.util.Set;

public class NotReservedUsernameValidator
        implements ConstraintValidator<NotReservedUsername, String> {
    // Keep in sync with RESERVED_USERNAMES in nautilus-client/app/utils/validation.ts
    private static final Set<String> RESERVED =
            Set.of(
                    "api",
                    "auth",
                    "browse",
                    "dashboard",
                    "login",
                    "new",
                    "oauth2",
                    "register",
                    "settings",
                    "stars",
                    "_nuxt",
                    "__nuxt",
                    "favicon.ico",
                    "robots.txt",
                    "sitemap.xml");

    public static boolean isReserved(String username) {
        return RESERVED.contains(username.toLowerCase(Locale.ROOT));
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Null is left to @NotBlank
        return value == null || !isReserved(value);
    }
}
