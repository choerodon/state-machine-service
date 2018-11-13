package io.choerodon.statemachine.fixdata.controller;

import io.choerodon.core.base.BaseController;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.statemachine.api.service.InitService;
import io.choerodon.statemachine.domain.Status;
import io.choerodon.statemachine.fixdata.dto.StatusForMoveDataDO;
import io.choerodon.statemachine.fixdata.service.FixDataService;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 敏捷修复数据专用
 *
 * @author shinan.chen
 * @date 2018/10/25
 */

@RestController
@RequestMapping(value = "/v1/fix_data")
public class FixDataController extends BaseController {

    @Autowired
    private FixDataService fixDataService;
    @Autowired
    private InitService initService;

    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_ADMINISTRATOR, InitRoleCode.SITE_DEVELOPER})
    @ApiOperation(value = "修复创建项目默认状态机")
    @PostMapping(value = "/create_state_machine_AG_TE")
    public ResponseEntity<Map<String, Long>> createAGStateMachineAndTEStateMachine(@RequestParam("organization_id") Long organizationId,
                                                                                   @RequestParam("project_code") String projectCode,
                                                                                   @RequestBody List<String> statuses) {
        return new ResponseEntity<>(fixDataService.createAGStateMachineAndTEStateMachine(organizationId, projectCode, statuses), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_ADMINISTRATOR, InitRoleCode.SITE_DEVELOPER})
    @ApiOperation(value = "创建组织默认状态机")
    @GetMapping(value = "/create_default_state_machine")
    public void createDefaultStateMachine(@RequestParam("organization_id") Long organizationId) {
        initService.initDefaultStateMachine(organizationId);
    }

    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_ADMINISTRATOR, InitRoleCode.SITE_DEVELOPER})
    @ApiOperation(value = "修复创建状态")
    @PostMapping(value = "/create_status")
    public ResponseEntity<Boolean> createStatus(@ApiParam(value = "敏捷状态数据", required = true)
                                                @RequestBody List<StatusForMoveDataDO> statusForMoveDataDOList) {
        return new ResponseEntity<>(fixDataService.createStatus(statusForMoveDataDOList), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_ADMINISTRATOR, InitRoleCode.SITE_DEVELOPER})
    @ApiOperation(value = "获取所有组织的所有状态")
    @GetMapping(value = "/query_status")
    public ResponseEntity<Map<Long, List<Status>>> queryAllStatus() {
        return new ResponseEntity<>(fixDataService.queryAllStatus(), HttpStatus.OK);
    }
}
