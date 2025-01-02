package com.project.shopapp.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.javafaker.Faker;
import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.dtos.*;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.models.Product;
import com.project.shopapp.models.ProductImage;
import com.project.shopapp.responses.ApiResponse;
import com.project.shopapp.responses.order.OrderDetailResponse;
import com.project.shopapp.responses.order.OrderListResponse;
import com.project.shopapp.responses.product.ProductListResponse;
import com.project.shopapp.responses.product.ProductResponse;
import com.project.shopapp.services.product.IProductRedisService;
import com.project.shopapp.services.product.IProductService;
import com.project.shopapp.utils.MessageKeys;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.prefix}/products")
@RequiredArgsConstructor
public class ProductController {
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    private final IProductService productService;
    private final LocalizationUtils localizationUtils;
    private final IProductRedisService productRedisService;
    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    //POST http://localhost:8088/v1/api/products
    public ApiResponse<Product> createProduct(
            @Valid @RequestBody ProductDTO productDTO,
            BindingResult result
    ) throws DataNotFoundException, Exception {

            if(result.hasErrors()) {
                List<String> errorMessages = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ApiResponse.<Product>builder()
                        .httpStatus(HttpStatus.BAD_REQUEST)
                        .message(String.join("; ",errorMessages)).build();            }
            Product newProduct = productService.createProduct(productDTO);
        return ApiResponse.<Product>builder().data(newProduct)
                .httpStatus(HttpStatus.CREATED)
                .message("Create order detail successfully").build();

    }

    @PostMapping(value = "uploads/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    //POST http://localhost:8088/v1/api/products
    public ResponseEntity<?> uploadImages(
            @PathVariable("id") Long productId,
            @ModelAttribute("files") List<MultipartFile> files
    ){
        try {
            Product existingProduct = productService.getProductById(productId);
            files = files == null ? new ArrayList<MultipartFile>() : files;
            if(files.size() > ProductImage.MAXIMUM_IMAGES_PER_PRODUCT) {
                return ResponseEntity.badRequest().body(localizationUtils
                        .getLocalizedMessage(MessageKeys.UPLOAD_IMAGES_MAX_5));
            }
            List<ProductImage> productImages = new ArrayList<>();
            for (MultipartFile file : files) {
                if(file.getSize() == 0) {
                    continue;
                }
                // Kiểm tra kích thước file và định dạng
                if(file.getSize() > 10 * 1024 * 1024) { // Kích thước > 10MB
                    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                            .body(localizationUtils
                                    .getLocalizedMessage(MessageKeys.UPLOAD_IMAGES_FILE_LARGE));
                }
                String contentType = file.getContentType();
                if(contentType == null || !contentType.startsWith("image/")) {
                    return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                            .body(localizationUtils.getLocalizedMessage(MessageKeys.UPLOAD_IMAGES_FILE_MUST_BE_IMAGE));
                }
                // Lưu file và cập nhật thumbnail trong DTO
                String filename = productService.storeFile(file); // Thay thế hàm này với code của bạn để lưu file
                //lưu vào đối tượng product trong DB
                ProductImage productImage = productService.createProductImage(
                        existingProduct.getId(),
                        ProductImageDTO.builder()
                                .imageUrl(filename)
                                .build()
                );
                productImages.add(productImage);
            }

            return ResponseEntity.ok().body(productImages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/images/{imageName}")
    public ResponseEntity<?> viewImage(@PathVariable String imageName) {
        try {
            java.nio.file.Path imagePath = Paths.get("uploads/"+imageName);
            UrlResource resource = new UrlResource(imagePath.toUri());

            if (resource.exists()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(resource);
            } else {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(new UrlResource(Paths.get("uploads/notfound.jpeg").toUri()));
                //return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("")
    public ApiResponse<ProductListResponse> getProducts(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0", name = "category_id") Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ) throws JsonProcessingException {
        int totalPages = 0;
        //productRedisService.clear();
        // Tạo Pageable từ thông tin trang và giới hạn
        PageRequest pageRequest = PageRequest.of(
                page, limit,
                //Sort.by("createdAt").descending()
                Sort.by("id").ascending()
        );
        logger.info(String.format("keyword = %s, category_id = %d, page = %d, limit = %d",
                keyword, categoryId, page, limit));
        List<ProductResponse> productResponses = productRedisService
                .getAllProducts(keyword, categoryId, pageRequest);
        if (productResponses!=null && !productResponses.isEmpty()) {
            totalPages = productResponses.get(0).getTotalPages();
        }
        if(productResponses == null) {
            Page<ProductResponse> productPage = productService
                    .getAllProducts(keyword, categoryId, pageRequest);
            // Lấy tổng số trang
            totalPages = productPage.getTotalPages();
            productResponses = productPage.getContent();
            // Bổ sung totalPages vào các đối tượng ProductResponse
            for (ProductResponse product : productResponses) {
                product.setTotalPages(totalPages);
            }
            productRedisService.saveAllProducts(
                    productResponses,
                    keyword,
                    categoryId,
                    pageRequest
            );
        }


        return ApiResponse.<ProductListResponse>builder().data(ProductListResponse.builder().products(productResponses)
                .totalPages(totalPages)
                .build()).message("Get product list response successfully").httpStatus(HttpStatus.OK).build();

    }
    //http://localhost:8088/api/v1/products/6
    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> getProductById(
            @PathVariable("id") Long productId
    ) throws  DataNotFoundException,Exception {
            Product existingProduct = productService.getProductById(productId);

        return ApiResponse.<ProductResponse>builder().data(ProductResponse.fromProduct(existingProduct))
                .httpStatus(HttpStatus.OK)
                .message("Get product with id:" +productId+" successfully").build();
    }
    @GetMapping("/by-ids")
    public ApiResponse<List<Product>>  getProductsByIds(@RequestParam("ids") String ids) {
        //eg: 1,3,5,7
            // Tách chuỗi ids thành một mảng các số nguyên
            List<Long> productIds = Arrays.stream(ids.split(","))
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            List<Product> products = productService.findProductsByIds(productIds);

            return ApiResponse.<List<Product>>builder().data(products)
                    .httpStatus(HttpStatus.OK)
                    .message("Get product with id:" +ids+" successfully").build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(security = { @SecurityRequirement(name = "bearer-key") })
    public ApiResponse<String> deleteProduct(@PathVariable long id) {
            productService.deleteProduct(id);
            return ApiResponse.<String>builder().data(String.format("Product with id = %d deleted successfully", id))
                    .httpStatus(HttpStatus.OK).build();

    }
    //@PostMapping("/generateFakeProducts")
    private ResponseEntity<String> generateFakeProducts() {
        Faker faker = new Faker();
        for (int i = 0; i < 1_000_000; i++) {
            String productName = faker.commerce().productName();
            if(productService.existsByName(productName)) {
                continue;
            }
            ProductDTO productDTO = ProductDTO.builder()
                    .name(productName)
                    .price((float)faker.number().numberBetween(10, 90_000_000))
                    .description(faker.lorem().sentence())
                    .thumbnail("")
                    .categoryId((long)faker.number().numberBetween(2, 5))
                    .build();
            try {
                productService.createProduct(productDTO);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }
        return ResponseEntity.ok("Fake Products created successfully");
    }
    //update a product
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    //@SecurityRequirement(name="bearer-key")
    @Operation(security = { @SecurityRequirement(name = "bearer-key") })
    public ApiResponse<Product> updateProduct (
            @PathVariable long id,
            @RequestBody ProductDTO productDTO) throws  DataNotFoundException, Exception{
            Product updatedProduct = productService.updateProduct(id, productDTO);

            return ApiResponse.<Product>builder().data(updatedProduct)
                    .httpStatus(HttpStatus.OK)
                    .message("update product with id:" +id+" successfully").build();

    }
}
