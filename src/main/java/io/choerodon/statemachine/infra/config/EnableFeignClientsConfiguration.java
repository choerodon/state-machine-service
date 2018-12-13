package io.choerodon.statemachine.infra.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

/**
 * 由于Spock单元测试需要mockFeignClient，因此需要配置是否注入EnableFeignClients
 *
 * @author shinan.chen
 * @since 2018/12/12
 */
@Configuration
@ConditionalOnProperty(name = "feignScan.enabled",havingValue = "true",matchIfMissing = true)
@EnableFeignClients("io.choerodon")
public class EnableFeignClientsConfiguration {
}
