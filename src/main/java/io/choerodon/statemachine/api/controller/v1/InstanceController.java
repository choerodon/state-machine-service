package io.choerodon.statemachine.api.controller.v1;

import io.choerodon.core.base.BaseController;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.statemachine.api.dto.ExecuteResult;
import io.choerodon.statemachine.api.dto.StateMachineTransfDTO;
import io.choerodon.statemachine.api.service.InstanceService;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;
import java.util.List;

/**
 * @author shinan.chen
 * @date 2018/9/17
 */
@RestController
@RequestMapping(value = "/v1/organizations/{organization_id}/instance")
public class InstanceController extends BaseController {

    @Autowired
    private InstanceService instanceService;

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
    @GetMapping(value = "/execute_transf")
    public ResponseEntity<ExecuteResult> executeTransf(@PathVariable("organization_id") Long organizationId,
                                                       @RequestParam("service_code") String serviceCode,
                                                       @RequestParam("state_machine_id") Long stateMachineId,
                                                       @RequestParam("instance_id") Long instanceId,
                                                       @RequestParam("current_state_id") Long currentStateId,
                                                       @RequestParam("transf_id") Long transfId) {
        ExecuteResult result = instanceService.executeTransf(organizationId, serviceCode, stateMachineId, instanceId, currentStateId, transfId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "获取当前状态拥有的转换列表，feign调用对应服务的条件验证")
    @GetMapping(value = "/transf_list")
    public ResponseEntity<List<StateMachineTransfDTO>> transfList(@PathVariable("organization_id") Long organizationId,
                                                                  @RequestParam("service_code") String serviceCode,
                                                                  @RequestParam("state_machine_id") Long stateMachineId,
                                                                  @RequestParam("instance_id") Long instanceId,
                                                                  @RequestParam("current_state_id") Long currentStateId) {
        return new ResponseEntity<>(instanceService.transfList(organizationId, serviceCode, stateMachineId, instanceId, currentStateId), HttpStatus.OK);
    }

    @GetMapping(value = "/test")
    public void getAllCustomers(@PathVariable("organization_id") Long organizationId) throws URISyntaxException {
        instanceService.test();
    }

}