package io.choerodon.statemachine.infra.feign.fallback;

import io.choerodon.core.exception.CommonException;
import io.choerodon.statemachine.api.dto.ExecuteResult;
import io.choerodon.statemachine.api.dto.StateMachineConfigDTO;
import io.choerodon.statemachine.api.dto.StateMachineTransfDTO;
import io.choerodon.statemachine.infra.feign.CustomFeignClientAdaptor;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.List;

/**
 * @author shinan.chen
 * @date 2018/9/20
 */
public class CustomFeignClientAdaptorFallBack implements CustomFeignClientAdaptor {

    @Override
    public void action(URI baseUri) {

    }

    @Override
    public ResponseEntity<List<StateMachineTransfDTO>> filterTransfsByConfig(URI baseUri, List<StateMachineTransfDTO> transfs) {
        throw new CommonException("error.customFeignClientAdaptor.filterTransfsByConfig");
    }

    @Override
    public ResponseEntity<ExecuteResult> executeConfig(URI baseUri, List<StateMachineConfigDTO> configs) {
        throw new CommonException("error.customFeignClientAdaptor.filterTransfsByConfig");
    }
}
