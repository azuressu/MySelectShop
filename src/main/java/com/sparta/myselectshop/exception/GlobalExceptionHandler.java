package com.sparta.myselectshop.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice // 클래스 레벨의 에너테이션
public class GlobalExceptionHandler {

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<RestApiException> handleException(IllegalArgumentException ex) {
        RestApiException restApiException = new RestApiException(ex.getMessage(), HttpStatus.BAD_REQUEST.value());
        return new ResponseEntity<>(
                // JSON 형태로 반환할 것
                // Http Body
                restApiException,
                // Http Status Code
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler({NullPointerException.class})
    public ResponseEntity<RestApiException> nullPointerExceptionHandler(NullPointerException ex) {
        RestApiException restApiException = new RestApiException(ex.getMessage(), HttpStatus.NOT_FOUND.value());

        return new ResponseEntity<>(
            // Http Body
            restApiException,
            // Http Status code
            HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler({ProductNotFoundException.class})
    public ResponseEntity<RestApiException> notFoundProductExceptionHandler(ProductNotFoundException ex) {
        // 여기서의 not_found.value()는 출력될 때 보이는 부분
        RestApiException restApiException = new RestApiException(ex.getMessage(), HttpStatus.NOT_FOUND.value());
        return new ResponseEntity<>(
            // Http Body
            restApiException,
            // Http Status Code
            HttpStatus.NOT_FOUND // 얘는 postman에서 위쪽에 status를 표시할 때 뜸(404 Not Found 라던가)
        );
    }


}
