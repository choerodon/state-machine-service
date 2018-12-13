package io.choerodon.statemachine;

import io.choerodon.eureka.event.EurekaEventHandler;
import io.choerodon.resource.annoation.EnableChoerodonResourceServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
@EnableChoerodonResourceServer
public class StateMachineServiceApplication {
    public static void main(String[] args) {
        //此处执行Eureka服务发现的初始化
        EurekaEventHandler.getInstance().init();
        SpringApplication.run(StateMachineServiceApplication.class, args);
    }
}

