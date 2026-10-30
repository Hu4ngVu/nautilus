package vn.io.huangnosimp.nautilusserver.server.dto.requests;

import jakarta.validation.constraints.NotNull;

public record ChangeServerVisibility(@NotNull Boolean visibility) {}
