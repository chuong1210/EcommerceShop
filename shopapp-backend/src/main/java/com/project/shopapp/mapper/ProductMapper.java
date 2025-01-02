package com.project.shopapp.mapper;


import com.project.shopapp.models.Product;
import com.project.shopapp.responses.product.ProductResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.stream.Collectors;
@Mapper(componentModel = "spring")
public interface ProductMapper {
//    @Mapping(source = "category.name", target = "categoryName")
//    @Mapping(target = "productImageUrls", expression = "java(product.getProductImages() != null ? product.getProductImages().stream().map(image -> image.getUrl()).collect(Collectors.toList()) : null)")
//    ProductResponse toProductResponse(Product product);

    @Mapping(source = "category.id", target = "categoryId")
    ProductResponse toResponse(Product product);

    List<ProductResponse> toResponseList(List<Product> products);
}
