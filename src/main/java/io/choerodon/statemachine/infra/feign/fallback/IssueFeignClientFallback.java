package io.choerodon.statemachine.infra.feign.fallback;

import io.choerodon.core.exception.CommonException;
import io.choerodon.statemachine.domain.event.DeployStateMachinePayload;
import io.choerodon.statemachine.infra.feign.IssueFeignClient;
import io.choerodon.statemachine.infra.feign.dto.ChangeStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author shinan.chen
 * @date 2018/10/31
 */
@Component
public class IssueFeignClientFallback implements IssueFeignClient {
    @Override
    public ResponseEntity<Map<String, List<Long>>> queryProjectIdsMap(Long organizationId, Long stateMachineId) {
        throw new CommonException("error.issueFeignClient.queryProjectIdsMap");
    }

    @Override
    public ResponseEntity<Map<String, Object>> checkDeleteNode(Long organizationId, Long stateMachineId, Long statusId) {
        throw new CommonException("error.issueFeignClient.checkDeleteNode");
    }

    @Override
    public ResponseEntity<DeployStateMachinePayload> handleStateMachineChangeStatusByStateMachineId(Long organizationId, Long stateMachineId, ChangeStatus changeStatus) {
        throw new CommonException("error.issueFeignClient.handleStateMachineChangeStatusByStateMachineId");
    }
}


