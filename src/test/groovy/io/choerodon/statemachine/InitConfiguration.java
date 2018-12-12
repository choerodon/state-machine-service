package io.choerodon.statemachine;

import io.choerodon.asgard.saga.feign.SagaClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.PostConstruct;

/**
 * @author shinan.chen
 * @since 2018/12/12
 */
@TestConfiguration
public class InitConfiguration {
    @MockBean(name = "sagaClient")
    private SagaClient sagaClient;
}
