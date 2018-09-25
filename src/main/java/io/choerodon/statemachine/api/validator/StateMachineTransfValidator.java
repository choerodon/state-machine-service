package io.choerodon.statemachine.api.validator;

import io.choerodon.core.exception.CommonException;
import io.choerodon.statemachine.api.dto.StateMachineTransfDTO;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author peng.jiang@hand-china.com
 */
@Component
public class StateMachineTransfValidator {

    public void createValidate(StateMachineTransfDTO transfDTO) {
        if (StringUtils.isEmpty(transfDTO.getStateMachineId())) {
            throw new CommonException("error.stateMachineNode.stateMachineId.empty");
        }
        if (StringUtils.isEmpty(transfDTO.getName())) {
            throw new CommonException("error.stateMachineNode.name.empty");
        }
    }

    public void updateValidate(StateMachineTransfDTO transfDTO) {
        if (transfDTO.getName() != null && transfDTO.getName().length() == 0) {
            throw new CommonException("error.stateMachineNode.name.empty");
        }
    }
}
