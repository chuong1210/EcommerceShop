package com.project.shopapp.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiResponse<T> {

    @JsonProperty("message")
    private String message;
    @Builder.Default
    @JsonProperty("code")

    int code = 10000;
    @JsonProperty("status")

    private HttpStatus httpStatus;
    @JsonProperty("data")

    T data;
}
