package com.sparta.myselectshop.repository;

import com.sparta.myselectshop.entity.Folder;
import com.sparta.myselectshop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FolderRepository extends JpaRepository<Folder, Long> {
    List<Folder> findAllByUserAndNameIn(User user, List<String> folderNamees);
    // query를 짜보자
    /* select * from folder where user_id = ? and name in (? , ? , ?); */ // 예를 들어 값이 3개가 들어왔다면 !
    List<Folder> findAllByUser(User user);

}
