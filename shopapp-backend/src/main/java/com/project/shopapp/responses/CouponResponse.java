package com.project.shopapp.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CouponResponse {
    @JsonProperty("result")
    private Double result;

    @JsonProperty("errorMessage")
    private String errorMessage;
}
