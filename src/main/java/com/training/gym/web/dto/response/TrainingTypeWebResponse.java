package com.training.gym.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Training specialization identifier and name")
public record TrainingTypeWebResponse(

        Long id,
        String name
) {
}
