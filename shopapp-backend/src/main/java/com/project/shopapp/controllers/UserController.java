package com.project.shopapp.controllers;

import com.github.javafaker.App;
import com.project.shopapp.exceptions.AppException;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.exceptions.ErrorCode;
import com.project.shopapp.exceptions.InvalidPasswordException;
import com.project.shopapp.models.Token;
import com.project.shopapp.models.User;
import com.project.shopapp.responses.ApiResponse;
import com.project.shopapp.responses.user.LoginResponse;
import com.project.shopapp.responses.RegisterResponse;
import com.project.shopapp.responses.user.UserListResponse;
import com.project.shopapp.responses.user.UserResponse;
import com.project.shopapp.services.token.ITokenService;
import com.project.shopapp.services.user.IUserService;
import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.utils.MessageKeys;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import com.project.shopapp.dtos.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("${api.prefix}/users")
@RequiredArgsConstructor
public class UserController {
    private final IUserService userService;
    private final LocalizationUtils localizationUtils;
    private final ITokenService tokenService;

    @GetMapping("")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ApiResponse<UserListResponse> getAllUser(
            @RequestParam(defaultValue = "", required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ) throws Exception{

            // Tạo Pageable từ thông tin trang và giới hạn
            PageRequest pageRequest = PageRequest.of(
                    page, limit,
                    //Sort.by("createdAt").descending()
                    Sort.by("id").ascending()
            );
            Page<UserResponse> userPage = userService.findAll(keyword, pageRequest)
                                .map(UserResponse::fromUser);

            // Lấy tổng số trang
            int totalPages = userPage.getTotalPages();
            List<UserResponse> userResponses = userPage.getContent();
            UserListResponse userListResponse=UserListResponse
                    .builder()
                    .users(userResponses)
                    .totalPages(totalPages)
                    .build();
            return ApiResponse.<UserListResponse>builder().data(userListResponse).httpStatus(HttpStatus.FOUND).message("Get user list successfully").build();

    }
    @PostMapping("/register")
    //can we register an "admin" user ?
    public ApiResponse<RegisterResponse> createUser(
            @Valid @RequestBody UserDTO userDTO,
            BindingResult result
    ) throws  Exception, AppException {
        RegisterResponse registerResponse = new RegisterResponse();

        if (result.hasErrors()) {
            List<String> errorMessages = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();

            registerResponse.setMessage(errorMessages.toString());
            return ApiResponse.<RegisterResponse>builder().httpStatus(HttpStatus.BAD_REQUEST).message(String.join(";",errorMessages)). build();
        }

        if (!userDTO.getPassword().equals(userDTO.getRetypePassword())) {
            registerResponse.setMessage(localizationUtils.getLocalizedMessage(MessageKeys.PASSWORD_NOT_MATCH));
            throw  new AppException(ErrorCode.INVALID_PASSWORD);
        }

            User user = userService.createUser(userDTO);
            registerResponse.setMessage("Đăng ký tài khoản thành công");
            registerResponse.setUser(user);
        return ApiResponse.<RegisterResponse>builder().data(registerResponse).httpStatus(HttpStatus.CREATED).message(localizationUtils.getLocalizedMessage(MessageKeys.REGISTER_SUCCESSFULLY)).build();

    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(
            @Valid @RequestBody UserLoginDTO userLoginDTO,
            HttpServletRequest request
    ) throws  Exception {
        // Kiểm tra thông tin đăng nhập và sinh token
            String token = userService.login(
                    userLoginDTO.getPhoneNumber(),
                    userLoginDTO.getPassword(),
                    userLoginDTO.getRoleId() == null ? 1 : userLoginDTO.getRoleId()
            );
            String userAgent = request.getHeader("User-Agent");
            User userDetail = userService.getUserDetailsFromToken(token);
            Token jwtToken = tokenService.addToken(userDetail, token, isMobileDevice(userAgent));

            // Trả về token trong response
            return ApiResponse.<LoginResponse>builder()
                            .message(localizationUtils.getLocalizedMessage(MessageKeys.LOGIN_SUCCESSFULLY))
                    .data(LoginResponse.builder().token(jwtToken.getToken())
                    .tokenType(jwtToken.getTokenType())
                    .refreshToken(jwtToken.getRefreshToken())
                    .username(userDetail.getUsername())
                    .roles(userDetail.getAuthorities().stream().map(item -> item.getAuthority()).toList())
                    .id(userDetail.getId())
                    .build()).httpStatus(HttpStatus.OK).build();
//        } catch (Exception e) {
//            return ApiResponse.<LoginResponse>builder()
//                            .message(localizationUtils.getLocalizedMessage(MessageKeys.LOGIN_FAILED, e.getMessage()))
//                            .build();
//
////             throw new AppException(ErrorCode.INVALID_USERNAME_OR_PASSWORD);
//        }
    }
    @PostMapping("/refreshToken")
    public  ApiResponse<LoginResponse> refreshToken(
            @Valid @RequestBody RefreshTokenDTO refreshTokenDTO
    ) throws  Exception{

            User userDetail = userService.getUserDetailsFromRefreshToken(refreshTokenDTO.getRefreshToken());
            Token jwtToken = tokenService.refreshToken(refreshTokenDTO.getRefreshToken(), userDetail);
        return ApiResponse.<LoginResponse>builder()
                .message(localizationUtils.getLocalizedMessage(MessageKeys.REFRESH_SUCCESSFULLY)).data(
                 LoginResponse.builder()   .token(jwtToken.getToken())
                    .tokenType(jwtToken.getTokenType())
                    .refreshToken(jwtToken.getRefreshToken())
                    .username(userDetail.getUsername())
                    .roles(userDetail.getAuthorities().stream().map(item -> item.getAuthority()).toList())
                    .id(userDetail.getId()).build()
                ).httpStatus(HttpStatus.OK).
                build();


        }
private boolean isMobileDevice(String userAgent) {
    // Kiểm tra User-Agent header để xác định thiết bị di động
    // Ví dụ đơn giản:
    return userAgent.toLowerCase().contains("mobile");
}
    @PostMapping("/details")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ApiResponse<UserResponse> getUserDetails(
            @RequestHeader("Authorization") String authorizationHeader
    ) throws  Exception{
            String extractedToken = authorizationHeader.substring(7); // Loại bỏ "Bearer " từ chuỗi token
            User user = userService.getUserDetailsFromToken(extractedToken);
            return ApiResponse.<UserResponse>builder().data(UserResponse.fromUser(user))
                    .httpStatus(HttpStatus.OK).message("Get user detail successfully").build();

    }
    @PutMapping("/details/{userId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    @Operation(security = { @SecurityRequirement(name = "bearer-key") })
    public ApiResponse<UserResponse> updateUserDetails(
            @PathVariable Long userId,
            @RequestBody UpdateUserDTO updatedUserDTO,
            @RequestHeader("Authorization") String authorizationHeader
    ) throws Exception {
            String extractedToken = authorizationHeader.substring(7);
            User user = userService.getUserDetailsFromToken(extractedToken);
            // Ensure that the user making the request matches the user being updated
            if (user.getId() != userId) {
//                return ApiResponse.<UserResponse>builder().httpStatus(HttpStatus.FORBIDDEN)
//                        .message("Not match user").build();
                throw  new AppException(ErrorCode.USER_NOT_EXISTED);
            }
            User updatedUser = userService.updateUser(userId, updatedUserDTO);
        return ApiResponse.<UserResponse>builder().httpStatus(HttpStatus.OK)
                .message("Update user successfully").data(UserResponse.fromUser(updatedUser)).build();

    }
    @PutMapping("/reset-password/{userId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ApiResponse<String> resetPassword(@Valid @PathVariable long userId){
        try {
            String newPassword = UUID.randomUUID().toString().substring(0, 5); // Tạo mật khẩu mới
            userService.resetPassword(userId, newPassword);
            return ApiResponse.<String>builder().httpStatus(HttpStatus.OK)
                    .message("Reset password successfully").data(newPassword).build();

        } catch (InvalidPasswordException e) {
            return ApiResponse.<String>builder().data("Invalid password").message(e.getMessage()).build();
        } catch (DataNotFoundException e) {
            return ApiResponse.<String>builder().data("User not found").message(e.getMessage()).build();

        }
    }
    @PutMapping("/block/{userId}/{active}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ApiResponse<Boolean> blockOrEnable(
            @Valid @PathVariable long userId,
            @Valid @PathVariable int active
    ) {
        try {
            userService.blockOrEnable(userId, active > 0);
            String message = active > 0 ? "Successfully enabled the user." : "Successfully blocked the user.";
            return ApiResponse.<Boolean>builder().httpStatus(HttpStatus.OK)
                    .message(message).data(true).build();

        } catch (DataNotFoundException e) {
            return ApiResponse.<Boolean>builder().httpStatus(HttpStatus.OK)
                    .message(e.getMessage()).data(false).build();
        }
    }
}
