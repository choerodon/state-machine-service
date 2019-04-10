package io.choerodon.statemachine.infra.feign;

import io.choerodon.statemachine.infra.feign.dto.ProjectDTO;
import io.choerodon.statemachine.infra.feign.fallback.UserFeignClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author shinan.chen
 * @date 2018/10/31
 */
@FeignClient(name = "iam-service", fallback = UserFeignClientFallback.class)
@Component
public interface UserFeignClient {
    /**
     * 按照id查询项目
     */
    @GetMapping(value = "/v1/projects/{project_id}")
    ResponseEntity<ProjectDTO> queryProject(@PathVariable(name = "project_id") Long projectId);
}
