package com.project.shopapp.services.coupon;

import com.project.shopapp.dtos.CategoryDTO;
import com.project.shopapp.models.Category;
import com.project.shopapp.models.Coupon;
import com.project.shopapp.models.CouponCondition;
import com.project.shopapp.models.Product;
import com.project.shopapp.repositories.CategoryRepository;
import com.project.shopapp.repositories.CouponConditionRepository;
import com.project.shopapp.repositories.CouponRepository;
import com.project.shopapp.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponService implements ICouponService {
    private  final CouponRepository couponRepository;
    private  final CouponConditionRepository couponConditionRepositoryRepository;
    @Override
    public double caculateCouponValue(String couponCode, double totalAmount) {
        Coupon coupon = couponRepository.findByCode(couponCode)
                .orElseThrow(() -> new IllegalArgumentException("Coupon not found"));

        if (!coupon.isActive()) {
            throw new IllegalArgumentException("Coupon is not active");
        }

        double discount = calculateDiscount(coupon, totalAmount);
        double finalAmount = totalAmount - discount;
        return finalAmount;
    }

    private double calculateDiscount(Coupon coupon, double totalAmount) {
        List<CouponCondition> conditions = couponConditionRepositoryRepository.findByCouponId(coupon.getId());

        double discount = 0.0;
        double updatedTotalAmount = totalAmount;

        for (CouponCondition condition : conditions) {
            String attribute = condition.getAttribute();
            String operator = condition.getOperator();
            String value = condition.getValue();
            double percentDiscount = Double.valueOf(String.valueOf(condition.getDiscountAmount()));

            if (attribute.equals("minimum_amount")) {
                if (operator.equals(">") && updatedTotalAmount > Double.parseDouble(value)) {
                    discount += updatedTotalAmount * percentDiscount / 100;
                }
            }
            else if (attribute.equals("applicable_date")) {
                LocalDate applicableDate = LocalDate.parse(value);
                LocalDate currentDate = LocalDate.now();

                if(operator.equalsIgnoreCase("BETWEEN") && currentDate.isEqual(applicableDate))
                {
                    discount += updatedTotalAmount * percentDiscount / 100;
                }
            }

            // còn nhiều điều kiện khác nữa
            updatedTotalAmount = updatedTotalAmount - discount;
        }
        return discount;
    }
}