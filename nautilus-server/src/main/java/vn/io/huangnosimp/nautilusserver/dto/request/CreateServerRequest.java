package vn.io.huangnosimp.nautilusserver.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import vn.io.huangnosimp.nautilusserver.constants.ServerType;

public record CreateServerRequest(
        @NotBlank
        String servername,

        @NotNull
        boolean visibility,

        @NotNull
        boolean requiresPremium,

        @NotNull
        ServerType type,

        @NotBlank
        String summary) {
}
