package com.sparta.myselectshop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration     // 아래 설정을 등록해 활성화
@EnableJpaAuditing // 시간 자동 변경이 가능하도록 함
public class JpaConfig {
}
