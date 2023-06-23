package com.sparta.myselectshop.service;

import com.sparta.myselectshop.dto.ProductMyPriceRequestDto;
import com.sparta.myselectshop.dto.ProductRequestDto;
import com.sparta.myselectshop.dto.ProductResponseDto;
import com.sparta.myselectshop.entity.Product;
import com.sparta.myselectshop.entity.User;
import com.sparta.myselectshop.entity.UserRoleEnum;
import com.sparta.myselectshop.naver.dto.ItemDto;
import com.sparta.myselectshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public static final int MIN_MY_PRICE = 100;

    // 저장하는 기능
    public ProductResponseDto createProduct(ProductRequestDto requestDto, User user) {
        // new Product하면서 받아온 requestDto를 넣어줌
        Product product = productRepository.save(new Product(requestDto, user));
        return new ProductResponseDto(product);
    }

    @Transactional // 변경 감지 까먹지 말고 달아주기
    public ProductResponseDto updateProduct(Long id, ProductMyPriceRequestDto requestDto) {
        int myprice = requestDto.getMyprice();
        if (myprice < MIN_MY_PRICE) {
            throw new IllegalArgumentException("유효하지 않은 관심 가격입니다. 최소 " + MIN_MY_PRICE + "원 이상으로 설정해 주세요.");
        }

        Product product = productRepository.findById(id).orElseThrow(() ->
                new NullPointerException("해당 상품을 찾을 수 없습니다."));

        product.update(requestDto);

        return new ProductResponseDto(product);
    }

    @Transactional(readOnly = true) // 지연 로딩 기능 사용을 위한 에너테이션
    public Page<ProductResponseDto> getProducts(User user, int page, int size, String sortBy, boolean isAsc) {
        // 정렬
        Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC; // true면 오름차순 false면 내림차순
        Sort sort = Sort.by(direction, sortBy);

        // 페이징 처리하기 위한 Pageable 객체
        Pageable pageable = PageRequest.of(page, size, sort);
        // User 권한 확인
        UserRoleEnum userRoleEnum = user.getRole();

        Page<Product> productList;
        // 일반 계정이라면
        if (userRoleEnum == UserRoleEnum.USER) {
            productList = productRepository.findAllByUser(user, pageable);
        } else {
            productList = productRepository.findAll(pageable); // findAll(): select * from product;
        }

        return productList.map(ProductResponseDto::new);
    }

    @Transactional
    public void updateBySearch(Long id, ItemDto itemDto) {
        Product product = productRepository.findById(id).orElseThrow(() ->
                new NullPointerException("해당 상품은 존재하지 않습니다."));

        product.updateByItemDto(itemDto);
    }

//    public List<ProductResponseDto> getAllProducts() {
//        List<Product> productList = productRepository.findAll();// findAll(): select * from product;
//        List<ProductResponseDto> responseDtoList =  new ArrayList<>();
//
//        for (Product product : productList) {
//            responseDtoList.add(new ProductResponseDto(product));
//        }
//
//        return responseDtoList;
//    }
}
