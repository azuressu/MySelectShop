package com.sparta.myselectshop.service;

import com.sparta.myselectshop.dto.ProductMyPriceRequestDto;
import com.sparta.myselectshop.dto.ProductRequestDto;
import com.sparta.myselectshop.dto.ProductResponseDto;
import com.sparta.myselectshop.entity.Product;
import com.sparta.myselectshop.entity.User;
import com.sparta.myselectshop.repository.FolderRepository;
import com.sparta.myselectshop.repository.ProductFolderRepository;
import com.sparta.myselectshop.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class) // @Mock 사용을 위한 설정
class ProductServiceTest {

    /* 가짜 객체를 만들어서 Mockito가 넣어줌
    * 우리는 Service 단을 테스트 하려고 하는데,
    * 이게 Repository에 연결이 되어 있어서, 분리해서 테스트를 할 수 없는 상황
    * 근데 이런 문제를 해결해주기 위해서 mockito 프레임워크 라이브러리라는 것이 생겼고,
    * 이게 가짜 객체를 제공해준다 */

    @Mock
    ProductRepository productRepository;

    @Mock
    FolderRepository folderRepository;

    @Mock
    ProductFolderRepository productFolderRepository;

    @Test
    @DisplayName("관심 상품 희망가 - 최저가 이상으로 변경")
    void test1() {
        // given
        Long productId = 100L;
        int myprice = ProductService.MIN_MY_PRICE + 3_000_000;

        ProductMyPriceRequestDto requestMyPriceDto = new ProductMyPriceRequestDto();
        requestMyPriceDto.setMyprice(myprice);

        User user = new User();
        ProductRequestDto requestProductDto = new ProductRequestDto(
                "Apple <b>맥북</b> <b>프로</b> 16형 2021년 <b>M1</b> Max 10코어 실버 (MK1H3KH/A) ",
                "https://shopping-phinf.pstatic.net/main_2941337/29413376619.20220705152340.jpg",
                "https://search.shopping.naver.com/gate.nhn?id=29413376619",
                3515000
        );

        Product product = new Product(requestProductDto, user);

        ProductService productService = new ProductService(productRepository, folderRepository, productFolderRepository); // 생성자의 파라미터로 3개를 넣어주어야 하는데 (서비스에서 처럼)

        // given을 하고, 내가 넣어줄 부분 코드를 작성한다
        // will return 하고, return할 값을 주면 됨
        given(productRepository.findById(productId)).willReturn(Optional.of(product));

        // when
        ProductResponseDto result = productService.updateProduct(productId, requestMyPriceDto);
        // 이 updateProduct 메소드 안을 보면, findById를 해서 Product를

        // then
        assertEquals(myprice, result.getMyprice());
    }

    @Test
    @DisplayName("관심 상품 희망가 - 최저가 미만으로 변경")
    void test2() {
        // given
        Long productId = 200L;
        int myprice = ProductService.MIN_MY_PRICE - 50;

        ProductMyPriceRequestDto requestMyPriceDto = new ProductMyPriceRequestDto();
        requestMyPriceDto.setMyprice(myprice);

        ProductService productService = new ProductService(productRepository, folderRepository, productFolderRepository);

        // when
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                productService.updateProduct(productId, requestMyPriceDto);
        });

        // then
        assertEquals(
                "유효하지 않은 관심 가격입니다. 최소 " + ProductService.MIN_MY_PRICE + " 원 이상으로 설정해 주세요.",
                exception.getMessage()
        );
    }

}