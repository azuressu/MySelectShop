package com.sparta.myselectshop.entity;

import com.sparta.myselectshop.dto.ProductMyPriceRequestDto;
import com.sparta.myselectshop.dto.ProductRequestDto;
import com.sparta.myselectshop.naver.dto.ItemDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "product")
@NoArgsConstructor
public class Product extends Timestamped{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String image;

    @Column(nullable = false)
    private String link;

    @Column(nullable = false)
    private int lprice;

    @Column(nullable = false)
    private int myprice;

    @ManyToOne(fetch = FetchType.LAZY) // 제품을 선택할 때마다 고객 정보다 늘 필요한 것이 아님(정말 필요할 때만 조회할 수 있도록)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "product") // 타겟팅이 되는 엔티티의 필드 (조인 컬럼으로 관계를 맺고 있는 그 컬럼명)
    /* 여기는 지연 로딩으로 설정되어있음. 이를 즉시 로딩으로 바꿀지 아니면 지연 로딩 조회 기능을 사용할까? 결정해야 함
    * 다만, 우리가 Product를 조회 할 때마다 계속해서 무조건 ProductFolderList 정보가 필요하다고 한다면 즉시 로딩이 좋고,
    * 그게 아니라 때에 따라서 필요할 수도 있고, 아닐수도 있다고 하면 지금 상태인 지연 로딩으로 놔두기 */
    private List<ProductFolder> productFolderList = new ArrayList<>();

    public Product(ProductRequestDto requestDto, User user){
        this.title = requestDto.getTitle();
        this.image = requestDto.getImage();
        this.link = requestDto.getLink();
        this.lprice = requestDto.getLprice();
        this.user = user;
    }

    public void update(ProductMyPriceRequestDto requestDto) {
        this.myprice = requestDto.getMyprice();
    }

    public void updateByItemDto(ItemDto itemDto) {
        this.lprice = itemDto.getLprice();
    }
}
