package io.choerodon.statemachine.app.assembler;

import io.choerodon.statemachine.api.dto.StateMachineNodeDTO;
import io.choerodon.statemachine.api.dto.StatusDTO;
import io.choerodon.statemachine.domain.StateMachineNode;
import io.choerodon.statemachine.domain.StateMachineNodeDraft;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dinghuang123@gmail.com
 * @since 2018/9/27
 */
@Component
public class StateMachineNodeAssembler extends AbstractAssembler {
    @Autowired
    private StatusAssembler statusAssembler;

    public StateMachineNodeDTO draftToNodeDTO(StateMachineNodeDraft node) {
        StateMachineNodeDTO target = toTarget(node, StateMachineNodeDTO.class);
        target.setStatusDTO(statusAssembler.toTarget(node.getStatus(), StatusDTO.class));
        return target;
    }

    public StateMachineNodeDTO toNodeDTO(StateMachineNode node) {
        StateMachineNodeDTO target = toTarget(node, StateMachineNodeDTO.class);
        target.setStatusDTO(statusAssembler.toTarget(node.getStatus(), StatusDTO.class));
        return target;
    }

    public List<StateMachineNodeDTO> draftToList(List<StateMachineNodeDraft> nodes) {
        List<StateMachineNodeDTO> nodeDTOS = new ArrayList<>(nodes.size());
        nodes.forEach(node->{
            nodeDTOS.add(draftToNodeDTO(node));
        });
        return nodeDTOS;
    }

    public List<StateMachineNodeDTO> toList(List<StateMachineNode> nodes) {
        List<StateMachineNodeDTO> nodeDTOS = new ArrayList<>(nodes.size());
        nodes.forEach(node->{
            nodeDTOS.add(toNodeDTO(node));
        });
        return nodeDTOS;
    }
}