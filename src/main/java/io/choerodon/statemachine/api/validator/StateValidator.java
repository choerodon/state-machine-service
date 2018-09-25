package io.choerodon.statemachine.api.validator;

import io.choerodon.core.exception.CommonException;
import io.choerodon.statemachine.api.dto.StateDTO;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author peng.jiang@hand-china.com
 */
@Component
public class StateValidator {

    public void validate(StateDTO stateDTO) {
        if (StringUtils.isEmpty(stateDTO.getName())) {
            throw new CommonException("error.state.name.empty");
        }
        if (StringUtils.isEmpty(stateDTO.getType())) {
            throw new CommonException("error.state.name.empty");
        }
        if (stateDTO.getType().length() != 1) {
            throw new CommonException("error.state.type.length");
        }
    }
}
