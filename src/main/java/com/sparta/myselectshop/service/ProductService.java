package com.sparta.myselectshop.service;

import com.sparta.myselectshop.dto.ProductRequestDto;
import com.sparta.myselectshop.dto.ProductResponseDto;
import com.sparta.myselectshop.entity.Product;
import com.sparta.myselectshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    // 저장하는 기능
    public ProductResponseDto createProduct(ProductRequestDto requestDto) {
        // new Product하면서 받아온 requestDto를 넣어줌
        Product product = productRepository.save(new Product(requestDto));
        return new ProductResponseDto(product);
    }
}
