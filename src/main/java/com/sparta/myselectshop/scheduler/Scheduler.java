package com.sparta.myselectshop.scheduler;

import com.sparta.myselectshop.entity.Product;
import com.sparta.myselectshop.naver.controller.NaverApiController;
import com.sparta.myselectshop.naver.dto.ItemDto;
import com.sparta.myselectshop.naver.service.NaverApiService;
import com.sparta.myselectshop.repository.ProductRepository;
import com.sparta.myselectshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j(topic = "Scheduler")
@Component
@RequiredArgsConstructor
public class Scheduler {

    private final NaverApiService naverApiService;
    private final ProductService productService;
    private final ProductRepository productRepository;

    // 초, 분, 시, 일, 월, 주 순서
    // @Scheduled 에너테이션은 우리가 지정하는 특정 시간마다 메서드가 동작될 것
    // cron : 운영체제에서 어떤 시간마다 특정 작업을 자동 수행하게 하고 싶을 때 사용하는 명령어
    @Scheduled(cron = " 0 0 1 * * *") // 매일 새벽 1시
    public void updatePrice() throws InterruptedException {
        log.info("가격 업데이트 실행");
        List<Product> productList = productRepository.findAll();

        for (Product product : productList) {
            // 1초에 한 상품씩 조회(NAVER 제한)
            TimeUnit.SECONDS.sleep(1);

            // i번째 관심 상품의 제목으로 검색
            String title = product.getTitle();
            List<ItemDto> itemDtoList = naverApiService.searchItems(title);

            if (itemDtoList.size() > 0) {
                ItemDto itemDto = itemDtoList.get(0);
                //i 번째 관심 상품 정보 업데이트
                Long id = product.getId();
                try {
                    // id와 업데이트할 정보가 담긴 itemDto를 넘겨줌
                    productService.updateBySearch(id, itemDto);
                } catch (Exception e) {
                    log.error(id + " : " + e.getMessage());
                }
            }
        }


    }
}
