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
}
