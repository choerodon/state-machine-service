package io.choerodon.statemachine.infra.feign.fallback;

import io.choerodon.core.exception.CommonException;
import io.choerodon.statemachine.infra.feign.UserFeignClient;
import io.choerodon.statemachine.infra.feign.dto.ProjectDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * @author shinan.chen
 * @date 2018/10/31
 */
@Component
public class UserFeignClientFallback implements UserFeignClient {

    @Override
    public ResponseEntity<ProjectDTO> queryProject(Long projectId) {
        throw new CommonException("error.userFeignClient.queryProject");
    }
}


