package io.choerodon.statemachine.app.assembler;

import io.choerodon.statemachine.api.dto.StateMachineConfigDTO;
import io.choerodon.statemachine.api.dto.StateMachineDTO;
import io.choerodon.statemachine.api.dto.StateMachineNodeDTO;
import io.choerodon.statemachine.api.dto.StateMachineTransformDTO;
import io.choerodon.statemachine.domain.*;
import io.choerodon.statemachine.infra.enums.ConfigType;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author dinghuang123@gmail.com
 * @since 2018/9/27
 */
@Component
public class StateMachineAssembler extends AbstractAssembler {
}
