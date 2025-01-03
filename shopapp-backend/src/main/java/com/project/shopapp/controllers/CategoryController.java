package com.project.shopapp.controllers;

import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.components.converters.CategoryMessageConverter;
import com.project.shopapp.dtos.*;
import com.project.shopapp.models.Category;
import com.project.shopapp.responses.ApiResponse;
import com.project.shopapp.responses.category.CategoryResponse;
import com.project.shopapp.services.category.CategoryService;
import com.project.shopapp.utils.MessageKeys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/categories")
//@Validated
//Dependency Injection
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;
    private final LocalizationUtils localizationUtils;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    //Nếu tham số truyền vào là 1 object thì sao ? => Data Transfer Object = Request Object
    public ApiResponse<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryDTO categoryDTO,
            BindingResult result) {
        CategoryResponse categoryResponse = new CategoryResponse();
        if(result.hasErrors()) {
            List<String> errorMessages = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            categoryResponse.setErrors(errorMessages);
            return ApiResponse.<CategoryResponse>builder().data(categoryResponse).message(localizationUtils.getLocalizedMessage(MessageKeys.INSERT_CATEGORY_FAILED)).httpStatus(HttpStatus.OK).build();
        }
        Category category = categoryService.createCategory(categoryDTO);
        categoryResponse.setCategory(category);
        kafkaTemplate.send("insert-category", category);
        this.kafkaTemplate.setMessageConverter(new CategoryMessageConverter());

        return ApiResponse.<CategoryResponse>builder().data(categoryResponse).message(localizationUtils.getLocalizedMessage(MessageKeys.INSERT_CATEGORY_SUCCESSFULLY)).httpStatus(HttpStatus.OK).build();
    }

    //Hiện tất cả các categories
    @GetMapping("")
    public ApiResponse<List<Category>> getAllCategories(
            @RequestParam("page")     int page,
            @RequestParam("limit")    int limit
    ) {
        List<Category> categories = categoryService.getAllCategories();
        this.kafkaTemplate.send("get-all-categories",categories);
        return ApiResponse.<List<Category>>builder().data(categories).message("Susscess get list category").httpStatus(HttpStatus.OK).build();
    }

    @GetMapping("/{id}")
    public ApiResponse<Category> getCategoryById (
            @PathVariable("id") Long categoryId
    ) throws Exception {
            Category existingCategory = categoryService.getCategoryById(categoryId);
        return ApiResponse.<Category>builder().data(existingCategory).message("Susscess get  category by Id").httpStatus(HttpStatus.OK).build();

    }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ApiResponse<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryDTO categoryDTO
    ) {
        CategoryResponse updateCategoryResponse = new CategoryResponse();
        Category ct=categoryService.updateCategory(id, categoryDTO);
        updateCategoryResponse.setCategory(ct);
        return ApiResponse.<CategoryResponse>builder().data(updateCategoryResponse).message(localizationUtils.getLocalizedMessage(MessageKeys.UPDATE_CATEGORY_SUCCESSFULLY)).httpStatus(HttpStatus.OK).build();
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ApiResponse<Boolean> deleteCategory(@PathVariable Long id) throws Exception {
            categoryService.deleteCategory(id);
        return ApiResponse.<Boolean>builder().data(true).message(localizationUtils.getLocalizedMessage(MessageKeys.DELETE_CATEGORY_SUCCESSFULLY,id)).httpStatus(HttpStatus.OK).build();


    }
}

