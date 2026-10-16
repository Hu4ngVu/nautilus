package vn.io.huangnosimp.nautilusserver.dto.request;

import jakarta.validation.constraints.NotBlank;
import vn.io.huangnosimp.nautilusserver.constants.ServerType;

public record CreateServerRequest(@NotBlank String servername, @NotBlank boolean visibility, @NotBlank ServerType type, @NotBlank String summary) {
}
