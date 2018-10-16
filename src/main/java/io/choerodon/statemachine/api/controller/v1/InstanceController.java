package io.choerodon.statemachine.api.controller.v1;

import io.choerodon.core.base.BaseController;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.statemachine.api.dto.ExecuteResult;
import io.choerodon.statemachine.api.service.InitService;
import io.choerodon.statemachine.api.service.InstanceService;
import io.choerodon.statemachine.domain.Status;
import io.choerodon.statemachine.infra.feign.dto.TransformInfo;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author shinan.chen
 * @date 2018/9/17
 */
@RestController
@RequestMapping(value = "/v1/organizations/{organization_id}/instances")
public class InstanceController extends BaseController {

    @Autowired
    private InstanceService instanceService;
    @Autowired
    private InitService initService;

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "创建状态机实例，并返回初始状态")
    @GetMapping(value = "/start_instance")
    public ResponseEntity<ExecuteResult> startInstance(@PathVariable("organization_id") Long organizationId,
                                                       @RequestParam("service_code") String serviceCode,
                                                       @RequestParam("state_machine_id") Long stateMachineId,
                                                       @RequestParam("instance_id") Long instanceId) {
        ExecuteResult result = instanceService.startInstance(organizationId, serviceCode, stateMachineId, instanceId);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "执行状态转换，并返回转换后的状态")
    @GetMapping(value = "/execute_transform")
    public ResponseEntity<ExecuteResult> executeTransform(@PathVariable("organization_id") Long organizationId,
                                                          @RequestParam("service_code") String serviceCode,
                                                          @RequestParam("state_machine_id") Long stateMachineId,
                                                          @RequestParam("instance_id") Long instanceId,
                                                          @RequestParam("current_status_id") Long currentStatusId,
                                                          @RequestParam("transform_id") Long transformId) {
        ExecuteResult result = instanceService.executeTransform(organizationId, serviceCode, stateMachineId, instanceId, currentStatusId, transformId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "获取当前状态拥有的转换列表，feign调用对应服务的条件验证")
    @GetMapping(value = "/transform_list")
    public ResponseEntity<List<TransformInfo>> queryListTransform(@PathVariable("organization_id") Long organizationId,
                                                                  @RequestParam("service_code") String serviceCode,
                                                                  @RequestParam("state_machine_id") Long stateMachineId,
                                                                  @RequestParam("instance_id") Long instanceId,
                                                                  @RequestParam("current_status_id") Long currentStateId) {
        return new ResponseEntity<>(instanceService.queryListTransform(organizationId, serviceCode, stateMachineId, instanceId, currentStateId), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "测试创建项目初始化状态、状态机")
    @GetMapping(value = "/testInit")
    @Transactional(rollbackFor = Exception.class)
    public void testInit(@PathVariable("organization_id") Long organizationId) {
        //初始化状态
        List<Status> initStatuses = initService.initStatus(organizationId);
        //初始化敏捷状态机
        initService.initAGStateMachine(organizationId, initStatuses);
    }
}