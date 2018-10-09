package io.choerodon.statemachine.infra.feign;

import io.choerodon.statemachine.infra.feign.dto.ProjectDTO;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author peng.jiang
 */
@FeignClient(name = "iam-service")
@Component
public interface UserFeignClient {
    /**
     * 按照id查询项目
     */
    @RequestMapping(value = "/v1/projects/{project_id}", method = RequestMethod.GET)
    public ResponseEntity<ProjectDTO> queryProject(@PathVariable(name = "project_id") Long projectId);
}
