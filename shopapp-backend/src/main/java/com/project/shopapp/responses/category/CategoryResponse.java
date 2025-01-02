package com.project.shopapp.responses.category;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.shopapp.models.Category;
import com.project.shopapp.models.Product;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryResponse {


    @JsonProperty("errors")
    @Builder.Default
    private List<String> errors = new ArrayList<>();

    @JsonProperty("category")
    private Category category;
}

