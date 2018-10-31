package io.choerodon.statemachine.infra.feign;

import io.choerodon.statemachine.infra.feign.fallback.IssueFeignClientFallback;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author shinan.chen
 * @date 2018/10/31
 */
@FeignClient(name = "issue-service", fallback = IssueFeignClientFallback.class)
@Component
public interface IssueFeignClient {
    /**
     * 查询状态机关联的项目id列表
     */
    @RequestMapping(value = "/v1/organizations/{organization_id}/state_machine/query_project_ids", method = RequestMethod.GET)
    ResponseEntity<List<Long>> queryProjectIds(@PathVariable("organization_id") Long organizationId,
                                               @RequestParam Long stateMachineId);
}
