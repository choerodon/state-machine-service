package io.choerodon.statemachine;

import io.choerodon.asgard.saga.dto.StartInstanceDTO;
import io.choerodon.asgard.saga.feign.SagaClient;
import io.choerodon.core.convertor.ApplicationContextHelper;
import io.choerodon.statemachine.api.dto.ExecuteResult;
import io.choerodon.statemachine.api.dto.InputDTO;
import io.choerodon.statemachine.api.service.impl.InitServiceImpl;
import io.choerodon.statemachine.infra.enums.TransformConditionStrategy;
import io.choerodon.statemachine.infra.enums.TransformType;
import io.choerodon.statemachine.infra.feign.CustomFeignClientAdaptor;
import io.choerodon.statemachine.infra.feign.IssueFeignClient;
import io.choerodon.statemachine.infra.feign.dto.TransformInfo;
import io.choerodon.statemachine.infra.feign.fallback.CustomFeignClientAdaptorFallBack;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author shinan.chen
 * @since 2018/12/13
 */
@Configuration
public class MockConfiguration {
    @Bean
    SagaClient sagaClient() {
        SagaClient sagaClient = Mockito.mock(SagaClient.class);
        Mockito.when(sagaClient.startSaga(Matchers.anyString(), Matchers.any(StartInstanceDTO.class))).thenReturn(null);
        InitServiceImpl initService = ApplicationContextHelper.getSpringFactory().getBean(InitServiceImpl.class);
        initService.setSagaClient(sagaClient);
        return sagaClient;
    }

    @Bean
    @Primary
    CustomFeignClientAdaptor customFeignClientAdaptor() {
        CustomFeignClientAdaptor customFeignClientAdaptor = Mockito.mock(CustomFeignClientAdaptorFallBack.class);
        ExecuteResult executeResult = new ExecuteResult();
        executeResult.setSuccess(true);
        executeResult.setResultStatusId(1L);
        Mockito.when(customFeignClientAdaptor.executeConfig(Matchers.any(URI.class), Matchers.any(InputDTO.class))).thenReturn(new ResponseEntity(executeResult, HttpStatus.OK));

        List<TransformInfo> transformInfos = new ArrayList<>();
        TransformInfo transformInfo = new TransformInfo();
        transformInfo.setId(10L);
        transformInfo.setOrganizationId(1L);
        transformInfo.setName("新转换");
        transformInfo.setType(TransformType.ALL);
        transformInfo.setConditionStrategy(TransformConditionStrategy.ALL);
        transformInfo.setStateMachineId(10L);
        transformInfo.setEndStatusId(10L);
        transformInfo.setStartStatusId(0L);
        transformInfos.add(transformInfo);
        Mockito.when(customFeignClientAdaptor.filterTransformsByConfig(Matchers.any(URI.class), Matchers.any(ArrayList.class))).thenReturn(new ResponseEntity(transformInfos, HttpStatus.OK));
        return customFeignClientAdaptor;
    }

    @Bean
    @Primary
    IssueFeignClient issueFeignClient() {
        IssueFeignClient issueFeignClient = Mockito.mock(IssueFeignClient.class);
        Map<String, Object> result = new HashMap<>(2);
        result.put("canDelete", true);
        result.put("count", 0);
        Mockito.when(issueFeignClient.checkDeleteNode(Matchers.any(Long.class), Matchers.any(Long.class), Matchers.any(Long.class))).thenReturn(new ResponseEntity(result, HttpStatus.OK));

//        List<TransformInfo> transformInfos = new ArrayList<>();
//        TransformInfo transformInfo = new TransformInfo();
//        transformInfo.setId(10L);
//        transformInfo.setOrganizationId(1L);
//        transformInfo.setName("新转换");
//        transformInfo.setType(TransformType.ALL);
//        transformInfo.setConditionStrategy(TransformConditionStrategy.ALL);
//        transformInfo.setStateMachineId(10L);
//        transformInfo.setEndStatusId(10L);
//        transformInfo.setStartStatusId(0L);
//        transformInfos.add(transformInfo);
//        Mockito.when(customFeignClientAdaptor.filterTransformsByConfig(Matchers.any(URI.class), Matchers.any(ArrayList.class))).thenReturn(new ResponseEntity(transformInfos, HttpStatus.OK));
        return issueFeignClient;
    }
}
