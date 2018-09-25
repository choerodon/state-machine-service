package io.choerodon.statemachine.api.validator;

import io.choerodon.core.exception.CommonException;
import io.choerodon.statemachine.api.dto.StateMachineDTO;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author peng.jiang@hand-china.com
 */
@Component
public class StateMachineValidator {

    public void createValidate(StateMachineDTO stateMachineDTO) {
        if (StringUtils.isEmpty(stateMachineDTO.getName())) {
            throw new CommonException("error.stateMachine.name.empty");
        }
    }

    public void updateValidate(StateMachineDTO stateMachineDTO) {
        if (stateMachineDTO.getName() != null && stateMachineDTO.getName().length() == 0) {
            throw new CommonException("error.stateMachine.name.empty");
        }
    }
}
