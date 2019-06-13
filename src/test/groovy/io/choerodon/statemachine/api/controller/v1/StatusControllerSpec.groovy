package io.choerodon.statemachine.api.controller.v1

import io.choerodon.asgard.saga.feign.SagaClient
import io.choerodon.core.domain.PageInfo
import io.choerodon.statemachine.IntegrationTestConfiguration
import io.choerodon.statemachine.api.dto.*
import io.choerodon.statemachine.api.service.InitService
import io.choerodon.statemachine.api.service.StateMachineService
import io.choerodon.statemachine.domain.StateMachine
import io.choerodon.statemachine.domain.StateMachineNode
import io.choerodon.statemachine.domain.StateMachineNodeDraft
import io.choerodon.statemachine.domain.Status
import io.choerodon.statemachine.infra.enums.NodeType
import io.choerodon.statemachine.infra.mapper.StateMachineMapper
import io.choerodon.statemachine.infra.mapper.StateMachineNodeDraftMapper
import io.choerodon.statemachine.infra.mapper.StateMachineNodeMapper
import io.choerodon.statemachine.infra.mapper.StatusMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * @author shinan.chen
 * @since 2018/12/10
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Stepwise
class StatusControllerSpec extends Specification {
    @Autowired
    TestRestTemplate restTemplate
    @Autowired
    StateMachineService stateMachineService
    @Autowired
    StatusMapper statusMapper
    @Autowired
    StateMachineMapper stateMachineMapper
    @Autowired
    StateMachineNodeMapper nodeMapper
    @Autowired
    StateMachineNodeDraftMapper nodeDraftMapper
    @Autowired
    InitService initService
    @Autowired
    SagaClient sagaClient
    @Shared
    Long testOrganizationId = 1L
    @Shared
    Long testProjectId = 1L
    @Shared
    def needInit = true
    @Shared
    def needClean = false
    @Shared
    String baseUrl = '/v1/organizations/{organization_id}/status'
    @Shared
    def statusList = []
    @Shared
    def statusIds = []
    @Shared
    def stateMachineIds = []

    /**
     * 初始化
     */
    void setup() {
        if (needInit) {
            needInit = false

            //初始化状态
            initService.initStatus(testOrganizationId)
            //初始化默认状态机
            def stateMachineId = initService.initDefaultStateMachine(testOrganizationId)
            //发布状态机
            stateMachineService.deploy(testOrganizationId, stateMachineId, true)
            //新增一个状态把该状态添加到状态机中
            Status status = new Status()
            status.setName("测试中")
            status.setType("doing")
            status.setOrganizationId(testOrganizationId)
            statusMapper.insert(status)
            StateMachineNode node = new StateMachineNode()
            node.organizationId = testOrganizationId
            node.statusId = status.getId()
            node.type = NodeType.CUSTOM
            node.positionX = 100
            node.positionY = 100
            node.stateMachineId = stateMachineId
            nodeMapper.insert(node)
            StateMachineNodeDraft nodeDraft = new StateMachineNodeDraft()
            nodeDraft.organizationId = testOrganizationId
            nodeDraft.statusId = status.getId()
            nodeDraft.type = NodeType.CUSTOM
            nodeDraft.positionX = 100
            nodeDraft.positionY = 100
            nodeDraft.stateMachineId = stateMachineId
            nodeDraftMapper.insert(nodeDraft)
            stateMachineIds.add(stateMachineId)
            statusList.add(status)
            statusIds.add(status.getId())
            //新增一个状态
            Status status2 = new Status()
            status2.setName("测试中2")
            status2.setType("doing")
            status2.setOrganizationId(testOrganizationId)
            statusMapper.insert(status2)
            statusList.add(status2)
            statusIds.add(status2.getId())
        }
    }
    /**
     * 删除数据
     */
    void cleanup() {
        if (needClean) {
            needClean = false
            //删除状态
            Status status = new Status()
            status.organizationId = testOrganizationId
            statusMapper.delete(status)
            //删除状态机
            StateMachine stateMachine = new StateMachine()
            stateMachine.organizationId = testOrganizationId
            stateMachineMapper.delete(stateMachine)
            //删除节点
            StateMachineNode node = new StateMachineNode()
            node.organizationId = testOrganizationId
            nodeMapper.delete(node)
            //删除草稿节点
            StateMachineNodeDraft draft = new StateMachineNodeDraft()
            draft.organizationId = testOrganizationId
            nodeDraftMapper.delete(draft)
        }
    }

    def "queryStatusList"() {
        given: '准备工作'
        StatusSearchDTO searchDTO = new StatusSearchDTO()
        searchDTO.setType(type)
        searchDTO.setName(name)
        searchDTO.setParam(param)
        when: '分页查询'
        HttpEntity<StatusSearchDTO> httpEntity = new HttpEntity<>(searchDTO)
        ParameterizedTypeReference<PageInfo<StatusWithInfoDTO>> typeRef = new ParameterizedTypeReference<PageInfo<StatusWithInfoDTO>>() {
        }
        def entity = restTemplate.exchange(baseUrl + "/list?page=0&size=10", HttpMethod.POST, httpEntity, typeRef, testOrganizationId)

        then: '返回结果'
        entity.getStatusCode().is2xxSuccessful() == isSuccess
        entity.getBody().size() == size

        where: '测试用例：'
        name  | type    | param || isSuccess | size
        null  | null    | null  || true      | 6
        '待处理' | null    | null  || true      | 1
        null  | null    | '待处理' || true      | 1
        null  | "done"  | null  || true      | 1
        null  | "done1" | null  || true      | 0
    }

    def "create"() {
        given: '准备工作'
        StatusDTO stateDTO = new StatusDTO()
        stateDTO.setName(testName)
        stateDTO.setDescription(testDescription)
        stateDTO.setOrganizationId(testOrganizationId)
        stateDTO.setType(testType)
        when: '创建状态'
        HttpEntity<StatusDTO> httpEntity = new HttpEntity<>(stateDTO)
        def entity = restTemplate.exchange(baseUrl, HttpMethod.POST, httpEntity, StatusDTO, testOrganizationId)

        then: '状态码为200，创建成功'
        def actRequest = false
        def actResponse = false
        if (entity != null) {
            if (entity.getStatusCode().is2xxSuccessful()) {
                actRequest = true
                if (entity.getBody() != null && entity.getBody().getId() != null) {
                    actResponse = true
                }
            }
        }
        actRequest == expRequest
        actResponse == expResponse
        where: '测试用例：'
        testName | testDescription     | testType || expRequest | expResponse
        'new1'   | 'test-description1' | 'done'   || true       | true
        'new2'   | 'test-description1' | 'done1'  || true       | false
        'new1'   | 'test-description1' | 'done'   || true       | false
        null     | 'test-description1' | 'done'   || true       | false
    }

    def "update"() {
        given: '准备工作'
        Status status = statusList.get(0)
        StatusDTO statusDTO = new StatusDTO()
        statusDTO.setOrganizationId(testOrganizationId)
        statusDTO.setType(status.getType())
        statusDTO.setId(status.getId())
        statusDTO.setObjectVersionNumber(1L)
        statusDTO.setName(updateName)

        when: '更新状态'
        HttpEntity<StatusDTO> httpEntity = new HttpEntity<>(statusDTO)
        def entity = restTemplate.exchange(baseUrl + '/{status_id}', HttpMethod.PUT, httpEntity, StatusDTO, testOrganizationId, statusDTO.getId())

        then: '状态码为200，更新成功'
        def actRequest = false
        def actResponse = false
        if (entity != null) {
            if (entity.getStatusCode().is2xxSuccessful()) {
                actRequest = true
                if (entity.getBody() != null && entity.getBody().getId() != null) {
                    actResponse = true
                }
            }
        }
        actRequest == expRequest
        actResponse == expResponse
        where: '测试用例：'
        updateName   || expRequest | expResponse
        'test-name1' || true       | true
        null         || true       | false
    }

    def "queryStatusById"() {
        given: '准备工作'
        def queryId = statusId

        when: '根据id查询问题类型'
        def entity = restTemplate.exchange(baseUrl + "/{status_id}", HttpMethod.GET, null, StatusInfoDTO, testOrganizationId, queryId)

        then: '状态码为200，调用成功'

        def actRequest = false
        def actResponse = false
        if (entity != null) {
            if (entity.getStatusCode().is2xxSuccessful()) {
                actRequest = true
                if (entity.getBody() != null) {
                    if (entity.getBody().getId() != null) {
                        actResponse = true
                    }
                }
            }
        }
        actRequest == expRequest
        actResponse == expResponse

        where: '测试用例：'
        statusId     || expRequest | expResponse
        statusIds[0] || true       | true
        9999L        || true       | false
        null         || false      | false
    }

    def "queryByStateMachineIds"() {
        given: '准备工作'
        def queryId = stateMachineId

        when: '查询状态机下的所有状态'
        HttpEntity<List<Long>> httpEntity = new HttpEntity<>(Arrays.asList(queryId))
        ParameterizedTypeReference<List<StatusDTO>> typeRef = new ParameterizedTypeReference<List<StatusDTO>>() {
        }
        def entity = restTemplate.exchange(baseUrl + "/query_by_state_machine_id", HttpMethod.POST, httpEntity, typeRef, testOrganizationId)

        then: '状态码为200，调用成功'

        def actRequest = false
        def actResponse = false
        if (entity != null) {
            if (entity.getStatusCode().is2xxSuccessful()) {
                actRequest = true
                if (entity.getBody() != null) {
                    if (entity.getBody().size() > 0) {
                        actResponse = true
                    }
                }
            }
        }
        actRequest == expRequest
        actResponse == expResponse

        where: '测试用例：'
        stateMachineId     || expRequest | expResponse
        stateMachineIds[0] || true       | true
        9999L              || true       | false
    }

    def "queryAllStatus"() {
        given: '准备工作'
        def queryId = organizationId

        when: '查询组织下的所有状态'
        ParameterizedTypeReference<List<StatusDTO>> typeRef = new ParameterizedTypeReference<List<StatusDTO>>() {
        }
        def entity = restTemplate.exchange(baseUrl + "/query_all", HttpMethod.GET, null, typeRef, queryId)

        then: '状态码为200，调用成功'

        def actRequest = false
        def actResponse = false
        if (entity != null) {
            if (entity.getStatusCode().is2xxSuccessful()) {
                actRequest = true
                if (entity.getBody() != null) {
                    if (entity.getBody().size() > 0) {
                        actResponse = true
                    }
                }
            }
        }
        actRequest == expRequest
        actResponse == expResponse

        where: '测试用例：'
        organizationId     || expRequest | expResponse
        testOrganizationId || true       | true
        9999L              || true       | false
    }

    def "queryAllStatusMap"() {
        given: '准备工作'
        def queryId = organizationId

        when: '查询组织下的所有状态'
        ParameterizedTypeReference<Map<Long, StatusMapDTO>> typeRef = new ParameterizedTypeReference<Map<Long, StatusMapDTO>>() {
        }
        def entity = restTemplate.exchange(baseUrl + "/list_map", HttpMethod.GET, null, typeRef, queryId)

        then: '状态码为200，调用成功'

        def actRequest = false
        def actResponse = false
        if (entity != null) {
            if (entity.getStatusCode().is2xxSuccessful()) {
                actRequest = true
                if (entity.getBody() != null) {
                    if (entity.getBody().size() > 0) {
                        actResponse = true
                    }
                }
            }
        }
        actRequest == expRequest
        actResponse == expResponse

        where: '测试用例：'
        organizationId     || expRequest | expResponse
        testOrganizationId || true       | true
        9999L              || true       | false
    }

    def "checkName"() {
        given: '准备工作'
        def url = baseUrl + "/check_name?1=1"
        if (name != null) {
            url = url + "&name=" + name
        }

        when: '校验名字是否未被使用'
        def entity = restTemplate.exchange(url, HttpMethod.GET, null, StatusCheckDTO.class, testOrganizationId)

        then: '状态码为200，调用成功'

        def actRequest = false
        def actResponse = false
        if (entity != null) {
            if (entity.getStatusCode().is2xxSuccessful()) {
                actRequest = true
                if (entity.getBody() != null) {
                    actResponse = entity.getBody().getStatusExist()
                }
            }
        }
        actRequest == expRequest
        actResponse == expResponse
        where: '测试用例：'
        name  || expRequest | expResponse
        '新状态' || true       | false
        '待处理' || true       | true
    }

    def "checkNameByProject"() {
        given: '准备工作'
        def url = "/v1/projects/{project_id}/status/project_check_name?1=1"
        if (name != null) {
            url = url + "&name=" + name
        }
        if (testOrganizationId != null) {
            url = url + "&organization_id=" + testOrganizationId
        }

        when: '校验名字是否未被使用'
        def entity = restTemplate.exchange(url, HttpMethod.GET, null, StatusCheckDTO.class, testProjectId)

        then: '状态码为200，调用成功'

        def actRequest = false
        def actResponse = false
        if (entity != null) {
            if (entity.getStatusCode().is2xxSuccessful()) {
                actRequest = true
                if (entity.getBody() != null) {
                    actResponse = entity.getBody().getStatusExist()
                }
            }
        }
        actRequest == expRequest
        actResponse == expResponse
        where: '测试用例：'
        name  || expRequest | expResponse
        '新状态' || true       | false
        '待处理' || true       | true
    }

    def "batchStatusGet"() {
        given: '准备工作'
        def queryId = statusId
        when: '根据ids批量查询状态'
        HttpEntity<List<Long>> httpEntity = new HttpEntity<>(Arrays.asList(queryId))
        ParameterizedTypeReference<Map<Long, Status>> typeRef = new ParameterizedTypeReference<Map<Long, Status>>() {
        }
        def entity = restTemplate.exchange("/v1/status/batch", HttpMethod.POST, httpEntity, typeRef)

        then: '状态码为200，调用成功'

        def actRequest = false
        def actResponse = false
        if (entity != null) {
            if (entity.getStatusCode().is2xxSuccessful()) {
                actRequest = true
                if (entity.getBody().size() > 0) {
                    actResponse = true
                }
            }
        }
        actRequest == expRequest
        actResponse == expResponse
        where: '测试用例：'
        statusId     || expRequest | expResponse
        statusIds[0] || true       | true
        999L         || true       | false
    }

    def "createStatusForAgile"() {
        given: '准备工作'
        def queryStateMachineId = stateMachineId
        StatusDTO statusDTO = new StatusDTO()
        statusDTO.setOrganizationId(testOrganizationId)
        statusDTO.setName(name)
        statusDTO.setType(statusType)
        when: '【敏捷】新增状态'
        HttpEntity<StatusDTO> httpEntity = new HttpEntity<>(statusDTO)
        def entity = restTemplate.exchange(baseUrl + "/create_status_for_agile?state_machine_id=" + queryStateMachineId, HttpMethod.POST, httpEntity, StatusDTO, testOrganizationId)

        then: '状态码为200，调用成功'

        def actRequest = false
        def actResponse = false
        if (entity != null) {
            if (entity.getStatusCode().is2xxSuccessful()) {
                actRequest = true
                if (entity.getBody() != null && entity.getBody().getId() != null) {
                    actResponse = true
                }
            }
        }
        actRequest == expRequest
        actResponse == expResponse
        where: '测试用例：'
        stateMachineId     | name    | statusType || expRequest | expResponse
        stateMachineIds[0] | '测试1'   | 'done'     || true       | true
        stateMachineIds[0] | 'name1' | 'done'     || true       | true
        stateMachineIds[0] | '测试2'   | 'done1'    || true       | false
        9999L              | '测试3'   | 'done'     || true       | false
    }

    def "removeStatusForAgile"() {
        given: '准备工作'
        def queryStateMachineId = stateMachineId
        def queryStatusId = statusId
        def url = baseUrl + "/remove_status_for_agile?1=1"
        if (queryStateMachineId != null) {
            url = url + "&stateMachineId=" + queryStateMachineId
        }
        if (queryStatusId != null) {
            url = url + "&statusId=" + queryStatusId
        }
        when: '【敏捷】新增状态'
        def entity = restTemplate.exchange(url, HttpMethod.DELETE, null, Object, testOrganizationId)

        then: '状态码为200，调用成功'

        def actRequest = false
        def actResponse = false
        if (entity != null) {
            if (entity.getStatusCode().is2xxSuccessful()) {
                actRequest = true
                actResponse = true
            }
        }
        actRequest == expRequest
        actResponse == expResponse
        where: '测试用例：'
        stateMachineId     | statusId     || expRequest | expResponse
        null               | statusIds[0] || false      | false
        stateMachineIds[0] | null         || false      | false
        stateMachineIds[0] | statusIds[0] || true       | true
    }

    def "delete"() {
        given: '准备工作'
        def deleteStatusId = statusId
        when: '删除状态'
        def entity = restTemplate.exchange(baseUrl + '/{status_id}', HttpMethod.DELETE, null, Object, testOrganizationId, deleteStatusId)

        then: '状态码为200，更新成功'
        def actRequest = false
        def actResponse = false
        if (entity != null) {
            if (entity.getStatusCode().is2xxSuccessful()) {
                actRequest = true
                if (entity.getStatusCode().equals(HttpStatus.NO_CONTENT)) {
                    actResponse = true
                    needClean = true
                }
            }
        }
        actRequest == expRequest
        actResponse == expResponse
        where: '测试用例：'
        statusId     || expRequest | expResponse
        9999L        || true       | false
        statusIds[1] || true       | true
    }

}
