package com.project.shopapp.controllers;
import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.dtos.*;
import com.project.shopapp.exceptions.AppException;
import com.project.shopapp.exceptions.ErrorCode;
import com.project.shopapp.models.User;
import com.project.shopapp.responses.ApiResponse;
import com.project.shopapp.responses.CommentResponse;
import com.project.shopapp.responses.category.CategoryResponse;
import com.project.shopapp.services.comment.CommentService;
import com.project.shopapp.utils.MessageKeys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("${api.prefix}/comments")
//@Validated
//Dependency Injection
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;
    private final LocalizationUtils localizationUtils;

    @GetMapping("")
    public ApiResponse<List<CommentResponse>> getAllComments(
            @RequestParam(value = "user_id", required = false) Long userId,
            @RequestParam("product_id") Long productId
    ) {
        List<CommentResponse> commentResponses;
        if (userId == null) {
            commentResponses = commentService.getCommentsByProduct(productId);
        } else {
            commentResponses = commentService.getCommentsByUserAndProduct(userId, productId);
        }
        return ApiResponse.<List<CommentResponse>>builder().data(commentResponses).httpStatus(HttpStatus.OK).message(localizationUtils.getLocalizedMessage(MessageKeys.COMMENT_SUCCESSFULLY)).build();
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ApiResponse<Boolean> updateComment(
            @PathVariable("id") Long commentId,
            @Valid @RequestBody CommentDTO commentDTO
    )  throws  Exception {
            User loginUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (!Objects.equals(loginUser.getId(), commentDTO.getUserId())) {
                throw  new AppException(ErrorCode.COMMENT_ERROR);
            }
            commentService.updateComment(commentId, commentDTO);
             return ApiResponse.<Boolean>builder().data(true).httpStatus(HttpStatus.OK).message(localizationUtils.getLocalizedMessage(MessageKeys.COMMENT_SUCCESSFULLY)).build();


    }
    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ApiResponse<Boolean> insertComment(
            @Valid @RequestBody CommentDTO commentDTO
    )  throws  Exception {
        // Insert the new comment
        User loginUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!Objects.equals(loginUser.getId(), commentDTO.getUserId())) {
            throw new AppException(ErrorCode.COMMENT_ERROR);

        }
        commentService.insertComment(commentDTO);
        return ApiResponse.<Boolean>builder().data(true).httpStatus(HttpStatus.OK).message(localizationUtils.getLocalizedMessage(MessageKeys.COMMENT_SUCCESSFULLY)).build();

    }
}
