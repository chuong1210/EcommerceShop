package com.project.shopapp.controllers;

import com.project.shopapp.responses.ApiResponse;
import com.project.shopapp.responses.CouponResponse;
import com.project.shopapp.services.coupon.CouponService;
import com.project.shopapp.utils.MessageKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("${api.prefix}/coupons")
//@Validated
//Dependency Injection
@RequiredArgsConstructor
public class CouponController {
    private final CouponService couponService;

    @GetMapping("/calculate")
    public ApiResponse<CouponResponse> calculateCouponValue(
            @RequestParam("couponCode") String couponCode,
            @RequestParam("totalAmount") double totalAmount) {



        double finalAmount = couponService.caculateCouponValue(couponCode, totalAmount);
        return ApiResponse.<CouponResponse>builder().data(CouponResponse.builder().result(finalAmount).build()).httpStatus(HttpStatus.OK).message("Apply coupon is successfully").build();


    }
}
