package io.choerodon.statemachine.app.assembler;

import io.choerodon.statemachine.api.dto.StateMachineDTO;
import io.choerodon.statemachine.api.dto.StateMachineNodeDTO;
import io.choerodon.statemachine.api.dto.StateMachineTransfDTO;
import io.choerodon.statemachine.domain.StateMachine;
import io.choerodon.statemachine.domain.StateMachineNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dinghuang123@gmail.com
 * @since 2018/9/27
 */
@Component
public class StateMachineAssembler extends AbstractAssembler {

    @Autowired
    private StateMachineNodeAssembler stateMachineNodeAssembler;

    /**
     * 状态机转换
     *
     * @param stateMachine stateMachine
     * @return StateMachineDTO
     */
    public StateMachineDTO covertStateMachine(final StateMachine stateMachine) {
        StateMachineDTO stateMachineDTO = toTarget(stateMachine, StateMachineDTO.class);
        List<StateMachineNode> nodes = stateMachine.getStateMachineNodes();
        if (null != nodes && !nodes.isEmpty()) {
            List<StateMachineNodeDTO> nodeDTOs = new ArrayList<>(nodes.size());
            nodes.forEach(node -> nodeDTOs.add(stateMachineNodeAssembler.toTarget(node, StateMachineNodeDTO.class)));
            stateMachineDTO.setNodeDTOs(nodeDTOs);
        }
        stateMachineDTO.setTransfDTOs(toTargetList(stateMachine.getStateMachineTransfs(), StateMachineTransfDTO.class));
        return stateMachineDTO;
    }
}
