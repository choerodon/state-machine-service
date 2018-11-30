package io.choerodon.statemachine.api.controller.v1;

import io.choerodon.core.base.BaseController;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.statemachine.api.dto.StateMachineDTO;
import io.choerodon.statemachine.api.dto.StateMachineWithStatusDTO;
import io.choerodon.statemachine.api.service.InitService;
import io.choerodon.statemachine.api.service.StateMachineService;
import io.choerodon.statemachine.api.validator.StateMachineValidator;
import io.choerodon.statemachine.domain.event.ProjectEvent;
import io.choerodon.statemachine.infra.enums.SchemeApplyType;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author peng.jiang, dinghuang123@gmail.com
 */
@RestController
@RequestMapping(value = "/v1/organizations/{organization_id}/state_machines")
public class StateMachineController extends BaseController {

    @Autowired
    private StateMachineService stateMachineService;

    @Autowired
    private StateMachineValidator stateMachineValidator;
    @Autowired
    private InitService initService;

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "分页查询状态机列表")
    @CustomPageRequest
    @GetMapping
    public ResponseEntity<Page<StateMachineDTO>> pagingQuery(@ApiIgnore
                                                             @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest,
                                                             @PathVariable("organization_id") Long organizationId,
                                                             @RequestParam(required = false) String name,
                                                             @RequestParam(required = false) String description,
                                                             @RequestParam(required = false) String[] param) {
        return new ResponseEntity<>(stateMachineService.pageQuery(pageRequest, new StateMachineDTO(name, description, organizationId),
                param != null ? Arrays.stream(param).collect(Collectors.joining(",")) : null), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "创建状态机")
    @PostMapping
    public ResponseEntity<StateMachineDTO> create(@PathVariable("organization_id") Long organizationId, @RequestBody StateMachineDTO stateMachineDTO) {
        stateMachineValidator.createValidate(stateMachineDTO);
        return new ResponseEntity<>(stateMachineService.create(organizationId, stateMachineDTO), HttpStatus.CREATED);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "更新状态机")
    @PutMapping(value = "/{state_machine_id}")
    public ResponseEntity<StateMachineDTO> update(@PathVariable("organization_id") Long organizationId,
                                                  @PathVariable("state_machine_id") Long stateMachineId,
                                                  @RequestBody StateMachineDTO stateMachineDTO) {
        stateMachineValidator.updateValidate(stateMachineDTO);
        return new ResponseEntity<>(stateMachineService.update(organizationId, stateMachineId, stateMachineDTO), HttpStatus.CREATED);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "删除状态机")
    @DeleteMapping(value = "/{state_machine_id}")
    public ResponseEntity<Boolean> delete(@PathVariable("organization_id") Long organizationId,
                                          @PathVariable("state_machine_id") Long stateMachineId) {
        return new ResponseEntity<>(stateMachineService.delete(organizationId, stateMachineId), HttpStatus.NO_CONTENT);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "发布状态机")
    @GetMapping(value = "/deploy/{state_machine_id}")
    public ResponseEntity<Boolean> deploy(@PathVariable("organization_id") Long organizationId,
                                          @PathVariable("state_machine_id") Long stateMachineId) {
        return new ResponseEntity<>(stateMachineService.deploy(organizationId, stateMachineId, true), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "获取状态机及配置（草稿/新建）")
    @GetMapping(value = "/with_config_draft/{state_machine_id}")
    public ResponseEntity<StateMachineDTO> queryStateMachineWithConfigDraftById(@PathVariable("organization_id") Long organizationId,
                                                                                @PathVariable("state_machine_id") Long stateMachineId) {
        return new ResponseEntity<>(stateMachineService.queryStateMachineWithConfigById(organizationId, stateMachineId, true), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "获取状态机原件及配置（活跃）")
    @GetMapping(value = "/with_config_deploy/{state_machine_id}")
    public ResponseEntity<StateMachineDTO> queryStateMachineWithConfigOriginById(@PathVariable("organization_id") Long organizationId,
                                                                                 @PathVariable("state_machine_id") Long stateMachineId) {

        return new ResponseEntity<>(stateMachineService.queryStateMachineWithConfigById(organizationId, stateMachineId, false), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "获取状态机（无配置）")
    @GetMapping(value = "/{state_machine_id}")
    public ResponseEntity<StateMachineDTO> queryStateMachineById(@PathVariable("organization_id") Long organizationId,
                                                                 @PathVariable("state_machine_id") Long stateMachineId) {
        return new ResponseEntity<>(stateMachineService.queryStateMachineById(organizationId, stateMachineId), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "获取组织默认状态机")
    @GetMapping(value = "/default")
    public ResponseEntity<StateMachineDTO> queryDefaultStateMachine(@PathVariable("organization_id") Long organizationId) {
        return new ResponseEntity<>(stateMachineService.queryDefaultStateMachine(organizationId), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "删除草稿")
    @DeleteMapping(value = "/delete_draft/{state_machine_id}")
    public ResponseEntity<StateMachineDTO> deleteDraft(@PathVariable("organization_id") Long organizationId,
                                                       @PathVariable("state_machine_id") Long stateMachineId) {
        return new ResponseEntity<>(stateMachineService.deleteDraft(organizationId, stateMachineId), HttpStatus.NO_CONTENT);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "校验状态机名字是否未被使用")
    @GetMapping(value = "/check_name")
    public ResponseEntity<Boolean> checkName(@PathVariable("organization_id") Long organizationId,
                                             @RequestParam("name") String name) {
        return Optional.ofNullable(stateMachineService.checkName(organizationId, name))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.stateMachineName.check"));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "获取组织下所有状态机")
    @GetMapping(value = "/query_all")
    public ResponseEntity<List<StateMachineDTO>> queryAll(@PathVariable("organization_id") Long organizationId) {
        return new ResponseEntity<>(stateMachineService.queryAll(organizationId), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "【初始化项目】创建项目时创建该项目的状态机，返回状态机id")
    @PostMapping(value = "/create_with_create_project")
    public ResponseEntity<Long> createStateMachineWithCreateProject(@PathVariable("organization_id") Long organizationId,
                                                                    @RequestParam("applyType") String applyType,
                                                                    @RequestBody ProjectEvent projectEvent) {
        Long stateMachineId = null;
        if (applyType.equals(SchemeApplyType.AGILE)) {
            stateMachineId = initService.initAGStateMachine(organizationId, projectEvent);
        } else if (applyType.equals(SchemeApplyType.TEST)) {
            stateMachineId = initService.initTEStateMachine(organizationId, projectEvent);
        }
        return new ResponseEntity<>(stateMachineId, HttpStatus.CREATED);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "【issue服务】批量活跃状态机")
    @PostMapping(value = "/active_state_machines")
    public ResponseEntity<Boolean> activeStateMachines(@PathVariable("organization_id") Long organizationId,
                                                       @RequestBody List<Long> stateMachineIds) {
        return new ResponseEntity<>(stateMachineService.activeStateMachines(organizationId, stateMachineIds), HttpStatus.CREATED);
    }


    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "【issue服务】批量使活跃状态机变成未活跃")
    @PostMapping(value = "/not_active_state_machines")
    public ResponseEntity<Boolean> notActiveStateMachines(@PathVariable("organization_id") Long organizationId,
                                                          @RequestBody List<Long> stateMachineIds) {
        return new ResponseEntity<>(stateMachineService.notActiveStateMachines(organizationId, stateMachineIds), HttpStatus.CREATED);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "【issue服务】获取组织下所有状态机，包含状态")
    @GetMapping(value = "/query_all_with_status")
    public ResponseEntity<List<StateMachineWithStatusDTO>> queryAllWithStatus(@PathVariable("organization_id") Long organizationId) {
        return new ResponseEntity<>(stateMachineService.queryAllWithStatus(organizationId), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "移除单个节点")
    @DeleteMapping(value = "/remove_node")
    public ResponseEntity removeStateMachineNode(@ApiParam(value = "组织id", required = true)
                                                 @PathVariable("organization_id") Long organizationId,
                                                 @ApiParam(value = "state machine id", required = true)
                                                 @RequestParam Long stateMachineId,
                                                 @ApiParam(value = "status id", required = true)
                                                 @RequestParam Long statusId) {
        stateMachineService.removeStateMachineNode(organizationId, stateMachineId, statusId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
