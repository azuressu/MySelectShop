package com.sparta.myselectshop.repository;

import com.sparta.myselectshop.dto.ProductResponseDto;
import com.sparta.myselectshop.entity.Product;
import com.sparta.myselectshop.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findAllByUser(User user, Pageable pageable);

    Page<Product> findAllByUserAndProductFolderList_FolderId(User user, Long folderId, Pageable pageable);
    // 앞에 User를 받아옴, ProductFolder에서 양방향 설정을 해줌
    // 받아온 folderId를 넣어주면 해당하는 프로덕트 종류를 가져올 수 있다 (특정 폴더에 속해있는 프로덕트 리스트가 !)
    // 해당 folder_id를 주면, 거기에 해당하는 product_id 리스트를 가져올 수 있다.

    /* select *
    from product p
    where p.user_id = 1;

    select p.id, p.title as product_title, pf.product_id as product_id, pf.folder_id as folder_id
    from
        product p left join product_folder pf
            on p.id = pf.product_id
    where p.user_id = 1;


    select p.id, p.title as product_title, pf.product_id as product_id, pf.folder_id as folder_id
    from
        product p left join product_folder pf
                            on p.id = pf.product_id
    where p.user_id = 1
        and pf.folder_id = 3;


    select p.id, p.title as product_title, pf.product_id as product_id, pf.folder_id as folder_id
    from
        product p left join product_folder pf
                            on p.id = pf.product_id
    where p.user_id = 1
        and pf.folder_id = 3
    order by
        p.id desc;

    select p.id, p.title as product_title, pf.product_id as product_id, pf.folder_id as folder_id
    from
        product p left join product_folder pf
                            on p.id = pf.product_id
    where p.user_id = 1
        and pf.folder_id = 3
    order by
        p.id desc
        limit 10, 10; // 0부터 9까지가 10개, 10부터 19까지 10개,, 그러니까 앞에는 시작할 숫자, 뒤쪽에는 개수 !
    */
}
