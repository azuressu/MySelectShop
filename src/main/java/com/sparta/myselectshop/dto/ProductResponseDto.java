package com.sparta.myselectshop.dto;

import com.sparta.myselectshop.entity.Product;
import com.sparta.myselectshop.entity.ProductFolder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class ProductResponseDto {

    private Long id;
    private String title;
    private String link;
    private String image;
    private int lprice;
    private int myprice;

    // 관심상품 하나에 폴더가 여러 개 등록될 수 있음
    private List<FolderResponseDto> productFolderList = new ArrayList<>();

    public ProductResponseDto(Product product) {
        this.id = product.getId();
        this.title = product.getTitle();
        this.link = product.getLink();
        this.image = product.getImage();
        this.lprice = product.getLprice();
        this.myprice = product.getMyprice();
        /* Product에 우리가 연결한 중간 테이블 ProductFolderList를 가지고 온다
        * 거기에 폴더 정보가 있을 것 (우리가 원하는 것은 폴더 정보)
        * 그 폴더를 FolderResponseDto로 변환하면서 productFolderList 에 집어넣는다
        * productFolderList에는 결국 이 폴더에 정보들이 담기게 됨*/
        for (ProductFolder productFolder : product.getProductFolderList()) {
            productFolderList.add(new FolderResponseDto(productFolder.getFolder()));
        }
    }
}
