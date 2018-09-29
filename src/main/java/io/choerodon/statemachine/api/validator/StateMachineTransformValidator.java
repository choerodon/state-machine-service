package io.choerodon.statemachine.api.validator;

import io.choerodon.core.exception.CommonException;
import io.choerodon.statemachine.api.dto.StateMachineTransformDTO;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author peng.jiang,dinghuang123@gmail.com
 */
@Component
public class StateMachineTransformValidator {

    public void createValidate(StateMachineTransformDTO transformDTO) {
        if (StringUtils.isEmpty(transformDTO.getStateMachineId())) {
            throw new CommonException("error.stateMachineNode.stateMachineId.empty");
        }
        if (StringUtils.isEmpty(transformDTO.getName())) {
            throw new CommonException("error.stateMachineNode.name.empty");
        }
    }

    public void updateValidate(StateMachineTransformDTO transformDTO) {
        if (transformDTO.getName() != null && transformDTO.getName().length() == 0) {
            throw new CommonException("error.stateMachineNode.name.empty");
        }
    }
}
