package com.project.shopapp.services.coupon;

import com.project.shopapp.dtos.CategoryDTO;
import com.project.shopapp.models.Category;

import java.util.List;

public interface ICouponService {
double caculateCouponValue(String couponCode, double totalAmount);

}
