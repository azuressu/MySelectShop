package com.sparta.myselectshop.controller;

import com.sparta.myselectshop.dto.FolderRequestDto;
import com.sparta.myselectshop.dto.FolderResponseDto;
import com.sparta.myselectshop.exception.RestApiException;
import com.sparta.myselectshop.security.UserDetailsImpl;
import com.sparta.myselectshop.service.FolderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;

    @PostMapping("/folders")
    public void addFolders(@RequestBody FolderRequestDto folderRequestDto, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        // @RequestBody: 폴더 이름이 여러 개 들어오니까 받아오려고
        // @AuthenticationPrincipal: 인증 객체에서 userDetails 받아옴
        List<String> folderNamees = folderRequestDto.getFolderNames();

        folderService.addFolders(folderNamees, userDetails.getUser());
    }

    @GetMapping("/folders")
    public List<FolderResponseDto> getFolders(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return folderService.getFolders(userDetails.getUser());
    }


    // 컨트롤러 안에서 IllegalArgumentException 이 터졌을 때 이걸 잡아챈다
    // 그리고 파타미러토 IllegalArgumentException을 받아오고 거기서 message를 뽑아내고 있다
    // @ExceptionHandler은 spring에서 예외처리를 잡기 위해서 사용하는 에너테이션으로 여기에서 발생한 예외 처리를 위해 사용된다
    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<RestApiException> handleException(IllegalArgumentException ex) {
        System.out.println("FolderController.handleException"); // soutm: 해당 클래스의 어떤 메서드인지
        RestApiException restApiException = new RestApiException(ex.getMessage(), HttpStatus.BAD_REQUEST.value());
        return new ResponseEntity<>(
                // HTTP BODY
                restApiException,
                // HTTP Status Code
                HttpStatus.BAD_REQUEST
        );

    }


}
