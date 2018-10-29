package io.choerodon.statemachine.fixdata.controller;

import io.choerodon.core.base.BaseController;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.statemachine.api.dto.StatusDTO;
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

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "修复创建项目默认状态机")
    @PostMapping(value = "/create_state_machine")
    public ResponseEntity<Long> createStateMachine(@RequestParam("organization_id") Long organizationId,
                                                   @RequestParam("project_code") String projectCode,
                                                   @RequestBody List<String> statuses) {
        return new ResponseEntity<>(fixDataService.createStateMachine(organizationId, projectCode, statuses), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "修复创建状态")
    @PostMapping(value = "/create_status")
    public ResponseEntity<Boolean> createStatus(@ApiParam(value = "敏捷状态数据", required = true)
                                                                @RequestBody List<StatusForMoveDataDO> statusForMoveDataDOList) {
        return new ResponseEntity<>(fixDataService.createStatus(statusForMoveDataDOList), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "获取所有组织的所有状态")
    @GetMapping(value = "/query_status")
    public ResponseEntity<Map<Long, List<Status>>> queryAllStatus() {
        return new ResponseEntity<>(fixDataService.queryAllStatus(), HttpStatus.OK);
    }
}
