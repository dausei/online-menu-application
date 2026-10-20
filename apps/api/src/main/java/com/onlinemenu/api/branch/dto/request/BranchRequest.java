package com.onlinemenu.api.branch.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Body for creating (POST) and fully updating (PUT) a branch. */
public record BranchRequest(
        @Schema(example = "Downtown")
        @NotBlank @Size(max = 120)
        String name,

        @Schema(example = "downtown", description = "URL-friendly unique identifier")
        @NotBlank @Size(max = 120)
        @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$",
                message = "must contain only lowercase letters, digits and single hyphens")
        String slug,

        @Schema(example = "Abay Ave 10, Almaty")
        @Size(max = 255)
        String address,

        @Schema(example = "+7 700 000 00 00")
        @Size(max = 32)
        String phone,

        @Schema(example = "true", description = "Defaults to true when omitted")
        Boolean active
) {
}
