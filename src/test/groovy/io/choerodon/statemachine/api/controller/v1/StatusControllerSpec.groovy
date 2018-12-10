package io.choerodon.statemachine.api.controller.v1

import io.choerodon.core.domain.Page
import io.choerodon.statemachine.IntegrationTestConfiguration
import io.choerodon.statemachine.api.dto.*
import io.choerodon.statemachine.api.service.StatusService
import io.choerodon.statemachine.domain.StateMachine
import io.choerodon.statemachine.domain.StateMachineNode
import io.choerodon.statemachine.domain.StateMachineNodeDraft
import io.choerodon.statemachine.domain.Status
import io.choerodon.statemachine.infra.enums.NodeType
import io.choerodon.statemachine.infra.enums.StateMachineStatus
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

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * @author shinan.chen
 * @since 2018/12/10
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class StatusControllerSpec extends Specification {
    @Autowired
    TestRestTemplate restTemplate
    @Autowired
    StatusService service
    @Autowired
    StatusMapper statusMapper
    @Autowired
    StateMachineMapper stateMachineMapper
    @Autowired
    StateMachineNodeMapper nodeMapper
    @Autowired
    StateMachineNodeDraftMapper nodeDraftMapper
    @Shared
    Long testOrganizationId = 1L
    @Shared
    Long testProjectId = 1L
    @Shared
    String baseUrl = '/v1/organizations/{organization_id}/status'
    @Shared
    List<Status> list = new ArrayList<>()

    //初始化10条数据
    def setup() {
        Status del = new Status()
        statusMapper.delete(del)//清空数据
        list.clear()
        def testName = 'name'
        def testDescription = 'description'
        def testType = 'done'
        for (int i = 1; i <= 10; i++) {
            Status status = new Status()
            status.setId(i)
            status.setName(testName + i)
            status.setDescription(testDescription + i)
            status.setOrganizationId(testOrganizationId)
            status.setType(testType)
            int isInsert = statusMapper.insert(status)
            if (isInsert == 1) {
                list.add(status)
            }
        }
    }

    /**
     * 初始化一个活跃状态机
     */
    void initStateMachine() {
        StateMachine stateMachine = new StateMachine()
        stateMachineMapper.delete(stateMachine)
        stateMachine.setId(1L)
        stateMachine.setName("初始状态机")
        stateMachine.setOrganizationId(1L)
        stateMachine.setStatus(StateMachineStatus.ACTIVE)
        stateMachineMapper.insert(stateMachine)

        StateMachineNode node = new StateMachineNode()
        node.organizationId = testOrganizationId
        node.statusId = 1L
        node.type = NodeType.CUSTOM
        node.positionX = 100
        node.positionY = 100
        node.stateMachineId = 1L
        nodeMapper.insert(node)

        StateMachineNodeDraft nodeDraft = new StateMachineNodeDraft()
        nodeDraft.organizationId = testOrganizationId
        nodeDraft.statusId = 1L
        nodeDraft.type = NodeType.CUSTOM
        nodeDraft.positionX = 100
        nodeDraft.positionY = 100
        nodeDraft.stateMachineId = 1L
        nodeDraftMapper.insert(nodeDraft)
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
        testName     | testDescription     | testType || expRequest | expResponse
        'test-name1' | 'test-description1' | 'done'   || true       | true
        'test-name1' | 'test-description1' | 'done1'  || true       | false
        'name1'      | 'test-description1' | 'done'   || true       | false
        null         | 'test-description1' | 'done'   || true       | false
    }

    def "update"() {
        given: '准备工作'
        Status status = list.get(0)
        StatusDTO statusDTO = new StatusDTO()
        statusDTO.setId(status.getId())
        statusDTO.setType(status.getType())
        statusDTO.setObjectVersionNumber(1L)
        statusDTO.setName(updateName)

        when: '更新状态'
        HttpEntity<StatusDTO> httpEntity = new HttpEntity<>(statusDTO)
        def entity = restTemplate.exchange(baseUrl + '/{status_id}', HttpMethod.PUT, httpEntity, StatusDTO, testOrganizationId, status.getId())

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
                }
            }
        }
        actRequest == expRequest
        actResponse == expResponse
        where: '测试用例：'
        statusId || expRequest | expResponse
        1L       || true       | true
        999L     || true       | false
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
        statusId || expRequest | expResponse
        1L       || true       | true
        9999L    || true       | false
        null     || false      | false
    }

    def "queryByStateMachineIds"() {
        given: '准备工作'
        initStateMachine()
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
        stateMachineId || expRequest | expResponse
        1L             || true       | true
        9999L          || true       | false
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
        organizationId || expRequest | expResponse
        1L             || true       | true
        9999L          || true       | false
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
        organizationId || expRequest | expResponse
        1L             || true       | true
        9999L          || true       | false
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
        name         || expRequest | expResponse
        'init_name1' || true       | false
        'name1'      || true       | true
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
        name         || expRequest | expResponse
        'init_name1' || true       | false
        'name1'      || true       | true
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
        statusId || expRequest | expResponse
        1L       || true       | true
        999L     || true       | false
    }

    def "createStatusForAgile"() {
        given: '准备工作'
        initStateMachine()
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
        stateMachineId | name    | statusType || expRequest | expResponse
        1L             | '测试'    | 'done'     || true       | true
        1L             | 'name1' | 'done'     || true       | true
        1L             | '测试'    | 'done1'    || true       | false
        2L             | '测试'    | 'done'     || true       | false
    }

    def "removeStatusForAgile"() {
        given: '准备工作'
        initStateMachine()

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
        stateMachineId | statusId || expRequest | expResponse
        1L             | 1L       || true       | true
        null           | 1L       || false      | false
        1L             | null     || false      | false
    }

    def "queryStatusList"() {
        given: '准备工作'
        StatusSearchDTO searchDTO = new StatusSearchDTO()
        searchDTO.setType(type)
        searchDTO.setName(name)
        searchDTO.setParam(param)
        when: '分页查询'
        HttpEntity<StatusSearchDTO> httpEntity = new HttpEntity<>(searchDTO)
        ParameterizedTypeReference<Page<StatusWithInfoDTO>> typeRef = new ParameterizedTypeReference<Page<StatusWithInfoDTO>>() {
        }
        def entity = restTemplate.exchange(baseUrl + "/list?page=0&size=10", HttpMethod.POST, httpEntity, typeRef, testOrganizationId)

        then: '返回结果'
        entity.getStatusCode().is2xxSuccessful() == isSuccess
        entity.getBody().size() == size

        where: '测试用例：'
        name    | type    | param  || isSuccess | size
        null    | null    | null   || true      | 10
        'name2' | null    | null   || true      | 1
        null    | null    | 'name' || true      | 10
        null    | "done"  | null   || true      | 10
        null    | "done1" | null   || true      | 0
    }
}
