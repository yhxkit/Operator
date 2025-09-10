package com.sample.operator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;


@EnableAspectJAutoProxy // 모니터링용
@EnableJpaAuditing      // JPA create time 값
@SpringBootApplication
public class OperatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(OperatorApplication.class, args);
    }

}
