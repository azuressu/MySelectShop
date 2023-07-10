package com.sparta.myselectshop.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {

    /* RestTemplate에는 추가적으로 옵션을 설정하는 경우가 많아서
    * 수동으로 Bean 등록을 하는 편이다
    * 수동 Bean 등록을 위해서는 Class에는 @Configuration, 메소드에는 @Bean을 달아주어야 한다 */

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
                // RestTemplate으로 외부 API를 호출할 경우, 일정 시간이 지나도 응답이 없을 때
                // 무한 대기 상태 방지를 위해서 강제 종료를 설정한다
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(5))
                .build();
    }
}
