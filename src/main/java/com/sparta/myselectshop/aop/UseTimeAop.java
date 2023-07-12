package com.sparta.myselectshop.aop;

import com.sparta.myselectshop.entity.ApiUseTime;
import com.sparta.myselectshop.entity.User;
import com.sparta.myselectshop.repository.ApiUseTimeRepository;
import com.sparta.myselectshop.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j(topic = "UseTimeAop")
@Aspect // AOP 를 사용하기 위해 클래스에 달아야 할 에너테이션
@Component
@RequiredArgsConstructor
public class UseTimeAop {

    private final ApiUseTimeRepository apiUseTimeRepository;

    // 아래 컨트롤러들은 restAPI -> JSON을 반환하는 Restful 요청을 받아서
    @Pointcut("execution(* com.sparta.myselectshop.controller.ProductController.*(..))")
    private void product() {}

    @Pointcut("execution(* com.sparta.myselectshop.controller.FolderController.*(..))")
    private void folder() {}

    @Pointcut("execution(* com.sparta.myselectshop.naver.controller.NaverApiController.*(..))")
    private void naver() {}

    @Around("product() || folder() || naver()") // controller의 메소드 수행 전에 일단 시작시간을 체크해야 하기 때문
    public Object execute(ProceedingJoinPoint joinPoint) throws Throwable { // 파라미터는 ProceedingJoinPoint를 받아옴
        // 측정 시작 시간
        long startTime = System.currentTimeMillis();
        // try가 전, finally가 후
        try {
            // 핵심기능 수행 (해당하는 컨트롤러의 메소드를 수행시킨다는 의미)
            Object output = joinPoint.proceed();
            return output;
        } finally {
            // 측정 종료 시간
            long endTime = System.currentTimeMillis();
            // 수행 시간
            long runTime = endTime - startTime;

            // 로그인 회원이 없는 경우, 수행시간을 기록하지 않음 (인증 객체를 가져와서 Authentication에 넣어줌)
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal().getClass() == UserDetailsImpl.class) {
                // 로그인 회원 정보
                UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
                User loginUser = userDetails.getUser();

                // API 사용시간 및 DB에 기록
                ApiUseTime apiUseTime = apiUseTimeRepository.findByUser(loginUser).orElse(null);
                if (apiUseTime == null) {
                    // 로그인 회원의 기록이 없으면
                    apiUseTime = new ApiUseTime(loginUser, runTime);
                } else {
                    // 로그인 회원의 기록이 이미 있으면
                    apiUseTime.addUseTime(runTime);
                }

                log.info("[API Use Time] Username: " + loginUser.getUsername() + ", Total Time: " + apiUseTime.getTotalTime());
                apiUseTimeRepository.save(apiUseTime);
            }
        }
    }




}
