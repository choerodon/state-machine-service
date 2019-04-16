package io.choerodon.statemachine.infra.feign;

import io.choerodon.statemachine.domain.event.DeployStateMachinePayload;
import io.choerodon.statemachine.infra.feign.dto.ChangeStatus;
import io.choerodon.statemachine.infra.feign.fallback.IssueFeignClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author shinan.chen
 * @date 2018/10/31
 */
@FeignClient(name = "issue-service", fallback = IssueFeignClientFallback.class)
@Component
public interface IssueFeignClient {
    /**
     * 查询状态机关联的项目id列表
     *
     * @param organizationId
     * @param stateMachineId
     * @return
     */
    @GetMapping(value = "/v1/organizations/{organization_id}/state_machine/query_project_ids_map")
    ResponseEntity<Map<String, List<Long>>> queryProjectIdsMap(@PathVariable("organization_id") Long organizationId,
                                                               @RequestParam("stateMachineId") Long stateMachineId);

    /**
     * 【内部调用】状态机删除节点的校验，是否可以直接删除
     *
     * @param organizationId
     * @param stateMachineId
     * @param statusId
     * @return
     */
    @GetMapping(value = "/v1/organizations/{organization_id}/state_machine/check_delete_node")
    ResponseEntity<Map<String, Object>> checkDeleteNode(@PathVariable("organization_id") Long organizationId,
                                                        @RequestParam("stateMachineId") Long stateMachineId,
                                                        @RequestParam("statusId") Long statusId);

    /**
     * 状态机删除节点的数据处理，判断哪些项目要删除哪些状态
     *
     * @param organizationId
     * @param stateMachineId
     * @param changeStatus
     * @return
     */
    @PostMapping(value = "/v1/organizations/{organization_id}/state_machine/handle_state_machine_change_status_by_state_machine_id")
    ResponseEntity<DeployStateMachinePayload> handleStateMachineChangeStatusByStateMachineId(@PathVariable("organization_id") Long organizationId,
                                                                                             @RequestParam("stateMachineId") Long stateMachineId,
                                                                                             @RequestBody ChangeStatus changeStatus);
}
