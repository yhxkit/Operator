package com.sample.operator.app.ctrl.common.mnt;

import com.sample.operator.app.dto.common.NamedMntInfo;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor
public class MntAspect
{
    private final MeterRegistry meterRegistry;

    // 전체 api 트래픽 
    @Before("@within(org.springframework.web.bind.annotation.RestController)")
    public void recordTrafficRest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();

            String uri = request.getRequestURI();
            String ip = request.getRemoteAddr();

            Counter counter = meterRegistry.counter(NamedMntInfo.apiReqCnt, "url", uri, "ip", ip);
            counter.increment();
        }
    }


    // 전체 mvc 트래픽
    @Before("@within(org.springframework.stereotype.Controller)")
    public void recordTrafficMvc()
    {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();

            String uri = request.getRequestURI();
            String ip = request.getRemoteAddr();

            Counter counter = meterRegistry.counter(NamedMntInfo.mvnReqCnt, "url", uri, "ip", ip);
            counter.increment();
        }
    }

    // api 정상 응답
    @AfterReturning("@within(org.springframework.web.bind.annotation.RestController)")
    public void recordAfterSuccess() {

    }


    // api 익셉션 발생
    @AfterThrowing("@within(org.springframework.web.bind.annotation.RestController)")
    public void recordAfterException() {

    }


    @Before(value = "execution(public * com.sample.operator.app.ctrl..*RestCtrl.*(..))")
    public void test()
    {

        System.out.println("이러면 두개되냐고 ");
    }

    @AfterReturning(pointcut = "execution(public * com.sample.operator.app.ctrl..*RestCtrl.*(..))", returning = "result")
    public void afterTest(Object result)
    {
        System.out.println("결과 알려줄래? " + result);
    }

}
