package com.onlinemenu.api.menu.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** Body for creating (POST) and fully updating (PUT) a menu item. */
public record MenuItemRequest(
        @Schema(example = "1")
        @NotNull
        Long branchId,

        @Schema(example = "Beef Burger")
        @NotBlank @Size(max = 150)
        String name,

        @Schema(example = "Juicy beef patty, cheddar, pickles, house sauce")
        @Size(max = 1000)
        String description,

        @Schema(example = "Burgers")
        @NotBlank @Size(max = 80)
        String category,

        @Schema(example = "3490.00")
        @NotNull
        @DecimalMin(value = "0.00")
        @Digits(integer = 8, fraction = 2)
        BigDecimal price,

        @Schema(example = "true", description = "Defaults to true when omitted")
        Boolean available,

        @Schema(example = "10", description = "Position inside the category, defaults to 0")
        @Min(0)
        Integer sortOrder
) {
}
