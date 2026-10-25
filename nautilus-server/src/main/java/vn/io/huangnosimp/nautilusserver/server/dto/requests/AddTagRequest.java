package vn.io.huangnosimp.nautilusserver.server.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Map;
import java.util.Set;
import vn.io.huangnosimp.nautilusserver.server.constants.TagCategory;

public record AddTagRequest(
        @NotNull
                Map<
                                @NotNull TagCategory,
                                @NotNull @Size(max = 20) Set<@NotBlank @Size(max = 50) String>>
                        tags) {}
