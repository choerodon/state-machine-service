package io.choerodon.statemachine.infra.feign;

import feign.RequestLine;
import io.choerodon.statemachine.api.dto.ExecuteResult;
import io.choerodon.statemachine.api.dto.StateMachineConfigDTO;
import io.choerodon.statemachine.api.dto.StateMachineTransformDTO;
import io.choerodon.statemachine.infra.feign.fallback.CustomFeignClientAdaptorFallBack;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.List;

/**
 * @author shinan.chen
 * @date 2018/9/17
 */
@FeignClient(name="customFeignClient",fallback = CustomFeignClientAdaptorFallBack.class)
public interface CustomFeignClientAdaptor {

    @RequestLine("GET")
    void action(URI baseUri);

    /**
     * 调用对应服务，通过条件验证过滤掉转换
     * @param baseUri
     * @param transforms
     * @return
     */
    @RequestLine("POST")
    ResponseEntity<List<StateMachineTransformDTO>> filterTransformsByConfig(URI baseUri, List<StateMachineTransformDTO> transforms);

    /**
     * 调用对应服务，执行条件，验证，后置处理
     * @param baseUri
     * @param configs
     * @return
     */
    @RequestLine("POST")
    ResponseEntity<ExecuteResult> executeConfig(URI baseUri, List<StateMachineConfigDTO> configs);
}
