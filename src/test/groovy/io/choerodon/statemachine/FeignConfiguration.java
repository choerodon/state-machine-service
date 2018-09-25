package io.choerodon.statemachine;

import feign.Contract;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@TestConfiguration
public class FeignConfiguration {

    @Bean
    public Contract feignContract() {

        return new Contract.Default();
    }

}