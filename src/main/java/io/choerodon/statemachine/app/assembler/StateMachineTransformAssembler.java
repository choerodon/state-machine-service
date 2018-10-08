package io.choerodon.statemachine.app.assembler;

import io.choerodon.statemachine.domain.StateMachineNode;
import io.choerodon.statemachine.domain.StateMachineTransform;
import io.choerodon.statemachine.infra.feign.TransformInfo;
import io.choerodon.statemachine.infra.mapper.StateMachineNodeDraftMapper;
import io.choerodon.statemachine.infra.mapper.StateMachineNodeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author dinghuang123@gmail.com
 * @since 2018/9/27
 */
@Component
public class StateMachineTransformAssembler extends AbstractAssembler {
    @Autowired
    private StateMachineNodeMapper nodeMapper;

    /**
     * 转换为客户端需要的转换信息
     * @param stateMachineId
     * @param transforms
     * @return
     */
    public List<TransformInfo> toTransformInfo(Long stateMachineId, List<StateMachineTransform> transforms){
        //获取节点信息
        List<StateMachineNode> nodes = nodeMapper.selectByStateMachineId(stateMachineId);
        Map<Long,Long> nodeMap = nodes.stream().collect(Collectors.toMap(StateMachineNode::getId,StateMachineNode::getStatusId));
        List<TransformInfo> transformInfos = new ArrayList<>(transforms.size());
        transforms.forEach(t->{
            TransformInfo transformInfo = toTarget(t, TransformInfo.class);
            transformInfo.setStartStatusId(nodeMap.get(t.getStartNodeId()));
            transformInfo.setEndStatusId(nodeMap.get(t.getEndNodeId()));
            transformInfos.add(transformInfo);
        });
        return transformInfos;
    }
}
