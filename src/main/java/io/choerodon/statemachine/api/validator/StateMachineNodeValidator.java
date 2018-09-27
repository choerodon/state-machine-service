package io.choerodon.statemachine.api.validator;

import io.choerodon.core.exception.CommonException;
import io.choerodon.statemachine.api.dto.StateMachineNodeDTO;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author peng.jiang@hand-china.com
 */
@Component
public class StateMachineNodeValidator {

    public void createValidate(StateMachineNodeDTO nodeDTO) {
        if (StringUtils.isEmpty(nodeDTO.getStateMachineId())) {
            throw new CommonException("error.stateMachineNode.stateMachineId.empty");
        }
        if (StringUtils.isEmpty(nodeDTO.getStatusId()) && nodeDTO.getStateDTO() == null) {
            throw new CommonException("error.stateMachineNode.state.null");
        }
        if (StringUtils.isEmpty(nodeDTO.getStatusId()) && nodeDTO.getStateDTO() != null && StringUtils.isEmpty(nodeDTO.getStateDTO().getName())) {
            throw new CommonException("error.stateMachineNode.state.name.empty");
        }
    }

    public void updateValidate(StateMachineNodeDTO nodeDTO) {
        if (StringUtils.isEmpty(nodeDTO.getStatusId()) && nodeDTO.getStateDTO() == null) {
            throw new CommonException("error.stateMachineNode.state.null");
        }
        if (nodeDTO.getStateDTO() != null && StringUtils.isEmpty(nodeDTO.getStateDTO().getName())) {
            throw new CommonException("error.stateMachineNode.state.name.empty");
        }
    }
}
