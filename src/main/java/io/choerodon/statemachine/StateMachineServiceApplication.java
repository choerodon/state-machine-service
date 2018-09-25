package io.choerodon.statemachine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;

import io.choerodon.resource.annoation.EnableChoerodonResourceServer;

@SpringBootApplication
@EnableFeignClients
@EnableEurekaClient
@EnableChoerodonResourceServer
public class StateMachineServiceApplication {
    public static void main(String[] args){
        SpringApplication.run(StateMachineServiceApplication.class, args);
    }
}

