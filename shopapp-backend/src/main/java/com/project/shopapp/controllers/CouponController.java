package com.project.shopapp.controllers;

import com.project.shopapp.responses.CouponResponse;
import com.project.shopapp.services.coupon.CouponService;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<CouponResponse> calculateCouponValue(
            @RequestParam("couponCode") String couponCode,
            @RequestParam("totalAmount") double totalAmount) {

        try
        {

        double finalAmount = couponService.caculateCouponValue(couponCode, totalAmount);
        return ResponseEntity.ok(CouponResponse.builder().result(finalAmount).errorMessage("").build());
        }

        catch (Exception e)
        {
            return  ResponseEntity.badRequest().body(CouponResponse.builder().result(totalAmount).errorMessage(e.getMessage()).build());
        }
    }
}
