package com.project.shopapp.controllers;

import com.project.shopapp.models.Role;
import com.project.shopapp.responses.ApiResponse;
import com.project.shopapp.services.role.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/roles")
@RequiredArgsConstructor
@CrossOrigin("*")
public class RoleController {
    private final RoleService roleService;
    @GetMapping("")
    public ApiResponse<List<Role>> getAllRoles() {
        List<Role> roles = roleService.getAllRoles();
        return ApiResponse.<List<Role>>builder().data(roles).httpStatus(HttpStatus.OK).
                message("Get list role successfully").build();
    }
}
