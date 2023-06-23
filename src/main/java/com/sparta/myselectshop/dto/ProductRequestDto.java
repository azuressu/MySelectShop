package com.sparta.myselectshop.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestDto {

    // 관심상품명
    private String title;
    // 관심상품 썸네일 image url
    private String image;
    // 관심상품 구매링크 url
    private String link;
    // 관심상품의 최저가
    private int lprice;

}
