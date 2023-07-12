package com.sparta.myselectshop.service;

import com.sparta.myselectshop.dto.ProductMyPriceRequestDto;
import com.sparta.myselectshop.dto.ProductRequestDto;
import com.sparta.myselectshop.dto.ProductResponseDto;
import com.sparta.myselectshop.entity.*;
import com.sparta.myselectshop.exception.ProductNotFoundException;
import com.sparta.myselectshop.naver.dto.ItemDto;
import com.sparta.myselectshop.repository.FolderRepository;
import com.sparta.myselectshop.repository.ProductFolderRepository;
import com.sparta.myselectshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final FolderRepository folderRepository;
    private final ProductFolderRepository productFolderRepository;
    private final MessageSource messageSource;

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
            throw new IllegalArgumentException( // 네 번째 파라미터는 언어를 설정하는 것
                    messageSource.getMessage("below.min.my.price", new Integer[]{MIN_MY_PRICE},"Wrong Price", Locale.getDefault())
            );
        }

        Product product = productRepository.findById(id).orElseThrow(() ->
                new ProductNotFoundException(messageSource.getMessage("not.found.product", null, "Not Found Product", Locale.getDefault())));

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

    public void addFolder(Long productId, Long folderId, User user) {
        Product product = productRepository.findById(productId).orElseThrow(
                () -> new NullPointerException("해당 상품이 존재하지 않습니다."));

        Folder folder = folderRepository.findById(folderId).orElseThrow(
                () -> new NullPointerException("해당 폴더가 존재하지 않습니다."));

        if (!product.getUser().getId().equals(user.getId())
                || !folder.getUser().getId().equals(user.getId())) { // 해당하지 않으면 (고객이 다르면?)
            throw new IllegalArgumentException("회원 님의 관심상품이 아니거나, 회원님의 폴더가 아닙니다.");
        }

        // 중복 확인 이미 등록된 폴더인지 확인하자
        // 예로, 디지털 기기라는 폴더를 만들어서 맥북을 넣어놨는데, 또 넣으려고 한다면? 안됨 !!
        Optional<ProductFolder> overlapFolder = productFolderRepository.findByProductAndFolder(product, folder);

        // 실제로 데이터 들어있는지 확인
        if (overlapFolder.isPresent()) {
            throw new IllegalArgumentException("중복된 폴더입니다.");
        }
        
        productFolderRepository.save(new ProductFolder(product, folder)); // 외래키 설정으로 두개를 넣어주어야 함
    }

    public Page<ProductResponseDto> getProductsInFolder(Long folderId, int page, int size, String sortBy, boolean isAsc, User user) {
        // 정렬
        Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC; // true면 오름차순 false면 내림차순
        Sort sort = Sort.by(direction, sortBy);

        // 페이징 처리하기 위한 Pageable 객체
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> products = productRepository.findAllByUserAndProductFolderList_FolderId(user, folderId, pageable);

        // 변환하기
        Page<ProductResponseDto> responseDtoList = products.map(ProductResponseDto::new);

        return responseDtoList;
    }

    // 페이지네이션 메소드 만들어보기 !! - pageable 타입을 반환하도록

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
