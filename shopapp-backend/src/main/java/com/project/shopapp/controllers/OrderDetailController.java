package com.project.shopapp.controllers;

import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.dtos.*;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.models.Order;
import com.project.shopapp.models.OrderDetail;
import com.project.shopapp.responses.ApiResponse;
import com.project.shopapp.responses.order.OrderDetailResponse;
import com.project.shopapp.services.orderdetails.OrderDetailService;
import com.project.shopapp.utils.MessageKeys;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("${api.prefix}/order_details")
@RequiredArgsConstructor
public class OrderDetailController {
    private final OrderDetailService orderDetailService;
    private final LocalizationUtils localizationUtils;
    //Thêm mới 1 order detail
    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ApiResponse<OrderDetailResponse> createOrderDetail (
            @Valid  @RequestBody OrderDetailDTO orderDetailDTO) throws  Exception {
            OrderDetail newOrderDetail = orderDetailService.createOrderDetail(orderDetailDTO);
            return ApiResponse.<OrderDetailResponse>builder().data(OrderDetailResponse.fromOrderDetail(newOrderDetail))
                    .httpStatus(HttpStatus.CREATED)
                    .message("Create order detail successfully").build();


    }
    @GetMapping("/{id}")
    public ApiResponse<OrderDetailResponse> getOrderDetail(
            @Valid @PathVariable("id") Long id) throws DataNotFoundException {
        OrderDetail orderDetail = orderDetailService.getOrderDetail(id);

        return ApiResponse.<OrderDetailResponse>builder().data(OrderDetailResponse.fromOrderDetail(orderDetail))
                .httpStatus(HttpStatus.OK)
                .message("Get order detail successfully").build();
    }
    //lấy ra danh sách các order_details của 1 order nào đó
    @GetMapping("/order/{orderId}")
    public ApiResponse<List<OrderDetailResponse>> getOrderDetails(
            @Valid @PathVariable("orderId") Long orderId
    ) throws  Exception {
        List<OrderDetail> orderDetails = orderDetailService.findByOrderId(orderId);
        List<OrderDetailResponse> orderDetailResponses = orderDetails
                .stream()
                .map(OrderDetailResponse::fromOrderDetail)
                .toList();
        return ApiResponse.<List<OrderDetailResponse>>builder().data(orderDetailResponses)
                .httpStatus(HttpStatus.OK)
                .message("Get list order detail successfully").build();
    }
    @PutMapping("/{id}")
    @Operation(security = { @SecurityRequirement(name = "bearer-key") })
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ApiResponse<OrderDetail> updateOrderDetail(
            @Valid @PathVariable("id") Long id,
            @RequestBody OrderDetailDTO orderDetailDTO) throws  DataNotFoundException,Exception {

            OrderDetail orderDetail = orderDetailService.updateOrderDetail(id, orderDetailDTO);
        return ApiResponse.<OrderDetail>builder().data(orderDetail).message("Update order detail successfully").httpStatus(HttpStatus.OK).build();


    }
    @DeleteMapping("/{id}")
    @Operation(security = { @SecurityRequirement(name = "bearer-key") })
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ApiResponse<String> deleteOrderDetail(
            @Valid @PathVariable("id") Long id) {
        orderDetailService.deleteById(id);
   String result=localizationUtils
           .getLocalizedMessage(MessageKeys.DELETE_ORDER_DETAIL_SUCCESSFULLY);

        return ApiResponse.<String>builder().data(result).message(result).httpStatus(HttpStatus.OK).build();

    }


}
