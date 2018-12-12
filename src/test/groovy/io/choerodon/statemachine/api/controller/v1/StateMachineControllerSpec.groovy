package io.choerodon.statemachine.api.controller.v1

import io.choerodon.asgard.saga.feign.SagaClient
import io.choerodon.core.domain.Page
import io.choerodon.statemachine.IntegrationTestConfiguration
import io.choerodon.statemachine.api.dto.StateMachineDTO
import io.choerodon.statemachine.api.dto.StatusCheckDTO
import io.choerodon.statemachine.api.dto.StatusMapDTO
import io.choerodon.statemachine.domain.StateMachine
import io.choerodon.statemachine.domain.StateMachineNode
import io.choerodon.statemachine.domain.StateMachineNodeDraft
import io.choerodon.statemachine.domain.Status
import io.choerodon.statemachine.domain.event.ProjectEvent
import io.choerodon.statemachine.infra.enums.NodeType
import io.choerodon.statemachine.infra.enums.StateMachineStatus
import io.choerodon.statemachine.infra.mapper.StateMachineMapper
import io.choerodon.statemachine.infra.mapper.StateMachineNodeDraftMapper
import io.choerodon.statemachine.infra.mapper.StateMachineNodeMapper
import io.choerodon.statemachine.infra.mapper.StatusMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
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
class StateMachineControllerSpec extends Specification {
    @Autowired
    TestRestTemplate restTemplate
    @Autowired
    StatusMapper statusMapper
    @Autowired
    StateMachineMapper stateMachineMapper
    @Autowired
    StateMachineNodeMapper nodeMapper
    @Autowired
    StateMachineNodeDraftMapper nodeDraftMapper
    @Autowired
    @Qualifier("sagaClient")
    SagaClient sagaClient
    @Shared
    Long testOrganizationId = 1L
    @Shared
    Long testProjectId = 1L
    @Shared
    String baseUrl = '/v1/organizations/{organization_id}/state_machines'
    @Shared
    List<StateMachine> list = new ArrayList<>()
    @Shared
    StateMachine createStateMachine
    @Shared
    StateMachine defaultStateMachine

    //初始化10条数据
    def setup() {
        StateMachine del = new StateMachine()
        stateMachineMapper.delete(del)//清空数据
        list.clear()
        def testName = 'name'
        def testDescription = 'description'
        for (int i = 1; i <= 8; i++) {
            StateMachine stateMachine = new StateMachine()
            stateMachine.setId(i)
            stateMachine.setStatus(StateMachineStatus.ACTIVE)
            stateMachine.setName(testName + i)
            stateMachine.setDescription(testDescription + i)
            stateMachine.setOrganizationId(testOrganizationId)
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

            list.add(stateMachine)
        }
        StateMachine defaultStateMachine = new StateMachine()
        defaultStateMachine.setId(9)
        defaultStateMachine.setStatus(StateMachineStatus.CREATE)
        defaultStateMachine.setName(testName + 9)
        defaultStateMachine.setDefault(true)
        defaultStateMachine.setDescription(testDescription + 9)
        defaultStateMachine.setOrganizationId(testOrganizationId)
        stateMachineMapper.insert(defaultStateMachine)

        StateMachineNodeDraft defaultNodeDraft = new StateMachineNodeDraft()
        defaultNodeDraft.organizationId = testOrganizationId
        defaultNodeDraft.statusId = 1L
        defaultNodeDraft.type = NodeType.CUSTOM
        defaultNodeDraft.positionX = 100
        defaultNodeDraft.positionY = 100
        defaultNodeDraft.stateMachineId = 1L
        nodeDraftMapper.insert(defaultNodeDraft)

        list.add(defaultStateMachine)
        defaultStateMachine = defaultStateMachine

        StateMachine stateMachine = new StateMachine()
        stateMachine.setId(10)
        stateMachine.setStatus(StateMachineStatus.CREATE)
        stateMachine.setName(testName + 10)
        stateMachine.setDescription(testDescription + 10)
        stateMachine.setOrganizationId(testOrganizationId)
        stateMachineMapper.insert(stateMachine)

        StateMachineNodeDraft nodeDraft = new StateMachineNodeDraft()
        nodeDraft.organizationId = testOrganizationId
        nodeDraft.statusId = 1L
        nodeDraft.type = NodeType.CUSTOM
        nodeDraft.positionX = 100
        nodeDraft.positionY = 100
        nodeDraft.stateMachineId = 1L
        nodeDraftMapper.insert(nodeDraft)

        list.add(stateMachine)
        createStateMachine = stateMachine

        and: 'mockSagaClient'
        sagaClient.startSaga(_,_) >> null
    }

    def "pagingQuery"() {
        given: '准备工作'
        def url = baseUrl + "?1=1"
        if (name != null) {
            url = url + "&name=" + name
        }
        if (description != null) {
            url = url + "&description=" + description
        }
        if (param != null) {
            url = url + "&param=" + param
        }
        when: '分页查询'
        ParameterizedTypeReference<Page<StateMachineDTO>> typeRef = new ParameterizedTypeReference<Page<StateMachineDTO>>() {
        }
        def entity = restTemplate.exchange(url, HttpMethod.GET, null, typeRef, testOrganizationId)

        then: '返回结果'
        def actRequest = false
        def actResponseSize = 0
        if (entity != null) {
            if (entity.getStatusCode().is2xxSuccessful()) {
                actRequest = true
                if (entity.getBody() != null) {
                    actResponseSize = entity.getBody().size()
                }
            }
        }
        actRequest == expRequest
        actResponseSize == expResponseSize

        where: '测试用例：'
        name       | description    | param  || expRequest | expResponseSize
        null       | null           | null   || true       | 10
        'name2'    | null           | null   || true       | 1
        null       | 'description2' | null   || true       | 1
        null       | null           | 'name' || true       | 10
        'notFound' | null           | null   || true       | 0
    }

    def "create"() {
        given: '准备工作'
        StateMachineDTO stateMachineDTO = new StateMachineDTO()
        stateMachineDTO.setName(testName)
        stateMachineDTO.setDescription(testDescription)
        stateMachineDTO.setOrganizationId(testOrganizationId)
        when: '创建状态机'
        HttpEntity<StateMachineDTO> httpEntity = new HttpEntity<>(stateMachineDTO)
        def entity = restTemplate.exchange(baseUrl, HttpMethod.POST, httpEntity, StateMachineDTO, testOrganizationId)

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
        testName     | testDescription     || expRequest | expResponse
        'test-name1' | 'test-description1' || true       | true
        'name1'      | 'test-description1' || true       | false
        null         | 'test-description1' || true       | false
    }

    def "update"() {
        given: '准备工作'
        StateMachine stateMachine = list.get(0)
        StateMachineDTO stateMachineDTO = new StateMachineDTO()
        stateMachineDTO.setId(stateMachine.getId())
        stateMachineDTO.setStatus(StateMachineStatus.CREATE)
        stateMachineDTO.setObjectVersionNumber(1L)
        stateMachineDTO.setName(updateName)

        when: '更新状态'
        HttpEntity<StateMachineDTO> httpEntity = new HttpEntity<>(stateMachineDTO)
        def entity = restTemplate.exchange(baseUrl + '/{state_machine_id}', HttpMethod.PUT, httpEntity, StateMachineDTO, testOrganizationId, stateMachine.getId())

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
        'name2'      || true       | false
        null         || true       | true
    }

    def "delete"() {
        given: '准备工作'
        def deleteStateMachineId = stateMachineId
        when: '删除状态'
        def entity = restTemplate.exchange(baseUrl + '/{state_machine_id}', HttpMethod.DELETE, null, Object, testOrganizationId, deleteStateMachineId)

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
        stateMachineId || expRequest | expResponse
        1L             || true       | true
        999L           || true       | false
    }

    def "deploy"() {
        given: '准备工作'
        def queryId = stateMachineId

        when: '发布状态机'
        def entity = restTemplate.exchange(baseUrl + "/deploy/{state_machine_id}", HttpMethod.GET, null, Object, testOrganizationId, queryId)

        then: '状态码为200，调用成功'

        def actRequest = false
        def actResponse = false
        if (entity != null) {
            if (entity.getStatusCode().is2xxSuccessful()) {
                actRequest = true
                if (entity.getBody() != null && entity.getBody() instanceof Boolean) {
                    actResponse = entity.getBody()
                }
            }
        }
        actRequest == expRequest
        actResponse == expResponse

        where: '测试用例：'
        stateMachineId || expRequest | expResponse
        10L            || true       | true
        1L             || true       | false
        9999L          || true       | false
        null           || false      | false
    }

    def "queryStateMachineWithConfigDraftById"() {
        given: '准备工作'
        def queryId = stateMachineId

        when: '获取状态机及配置（草稿/新建）'
        def entity = restTemplate.exchange(baseUrl + "/with_config_draft/{state_machine_id}", HttpMethod.GET, null, StateMachineDTO, testOrganizationId, queryId)

        then: '状态码为200，调用成功'

        def actRequest = false
        def actResponse = false
        if (entity != null) {
            if (entity.getStatusCode().is2xxSuccessful()) {
                actRequest = true
                if (entity.getBody() != null) {
                    if (entity.getBody().getId() != null) {
                        if (entity.getBody().getNodeDTOs().size() > 0) {
                            actResponse = true
                        }
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

    def "queryStateMachineWithConfigOriginById"() {
        given: '准备工作'
        def queryId = stateMachineId

        when: '获取状态机原件及配置（活跃）'
        def entity = restTemplate.exchange(baseUrl + "/with_config_deploy/{state_machine_id}", HttpMethod.GET, null, StateMachineDTO, testOrganizationId, queryId)

        then: '状态码为200，调用成功'

        def actRequest = false
        def actResponse = false
        if (entity != null) {
            if (entity.getStatusCode().is2xxSuccessful()) {
                actRequest = true
                if (entity.getBody() != null) {
                    if (entity.getBody().getId() != null) {
                        if (entity.getBody().getNodeDTOs().size() > 0) {
                            actResponse = true
                        }
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

    def "queryStateMachineById"() {
        given: '准备工作'
        def queryId = stateMachineId

        when: '获取状态机（无配置）'
        def entity = restTemplate.exchange(baseUrl + "/{state_machine_id}", HttpMethod.GET, null, StateMachineDTO, testOrganizationId, queryId)

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
        stateMachineId || expRequest | expResponse
        1L             || true       | true
        9999L          || true       | false
    }

    def "queryDefaultStateMachine"() {
        given: '准备工作'
        when: '获取组织默认状态机'
        def entity = restTemplate.exchange(baseUrl + "/default", HttpMethod.GET, null, StateMachineDTO, testOrganizationId)

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
        expRequest | expResponse
        true       | true
    }

    def "deleteDraft"() {
        given: '准备工作'
        def deleteStateMachineId = stateMachineId
        when: '删除草稿'
        def entity = restTemplate.exchange(baseUrl + '/delete_draft/{state_machine_id}', HttpMethod.DELETE, null, Object, testOrganizationId, deleteStateMachineId)

        then: '状态码为200，更新成功'
        def actRequest = false
        def actResponse = false
        if (entity != null) {
            if (entity.getStatusCode().is2xxSuccessful()) {
                actRequest = true
                if (entity.getStatusCode() == HttpStatus.NO_CONTENT) {
                    actResponse = true
                }
            }
        }
        actRequest == expRequest
        actResponse == expResponse
        where: '测试用例：'
        stateMachineId || expRequest | expResponse
        1L             || true       | true
        999L           || true       | false
    }

    def "checkName"() {
        given: '准备工作'
        def url = baseUrl + "/check_name?1=1"
        if (name != null) {
            url = url + "&name=" + name
        }

        when: '校验状态机名字是否未被使用'
        def entity = restTemplate.exchange(url, HttpMethod.GET, null, Object, testOrganizationId)

        then: '状态码为200，调用成功'

        def actRequest = false
        def actResponse = false
        if (entity != null) {
            if (entity.getStatusCode().is2xxSuccessful()) {
                actRequest = true
                if (entity.getBody() != null && entity.getBody() instanceof Boolean) {
                    actResponse = entity.getBody()
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

    def "queryAll"() {
        given: '准备工作'
        def queryId = organizationId

        when: '查询组织下的所有状态机'
        ParameterizedTypeReference<List<StateMachineDTO>> typeRef = new ParameterizedTypeReference<List<StateMachineDTO>>() {
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

    def "createStateMachineWithCreateProject"() {
        given: '准备工作'
        ProjectEvent projectEvent = new ProjectEvent()
        projectEvent.setProjectId(testProjectId)
        projectEvent.setProjectCode(projectCode)
        projectEvent.setProjectName(projectName)
        when: '【初始化项目】创建项目时创建该项目的状态机，返回状态机id'
        HttpEntity<ProjectEvent> httpEntity = new HttpEntity<>(projectEvent)
        def entity = restTemplate.exchange(baseUrl + "/create_with_create_project?applyType=" + applyType, HttpMethod.POST, httpEntity, Object, testOrganizationId)

        then: '状态码为200，创建成功'
        def actRequest = false
        def actResponse = false
        if (entity != null) {
            if (entity.getStatusCode().is2xxSuccessful()) {
                actRequest = true
                if (entity.getBody() != null) {
                    actResponse = true
                }
            }
        }
        actRequest == expRequest
        actResponse == expResponse
        where: '测试用例：'
        applyType | projectCode | projectName || expRequest | expResponse
        'test'    | 'csn'       | 'csn测试'     || true       | true
        'agile'    | 'csn'       | 'csn测试'     || true       | true
        'test1'    | 'csn'       | 'csn测试'     || true       | false
    }

    def "notActiveStateMachines"() {
        given: '准备工作'
        ProjectEvent projectEvent = new ProjectEvent()
        projectEvent.setProjectId(testProjectId)
        projectEvent.setProjectCode(projectCode)
        projectEvent.setProjectName(projectName)
        when: '【issue服务】批量使活跃状态机变成未活跃'
        HttpEntity<ProjectEvent> httpEntity = new HttpEntity<>(projectEvent)
        def entity = restTemplate.exchange(baseUrl + "/active_state_machines?applyType=" + applyType, HttpMethod.POST, httpEntity, Object, testOrganizationId)

        then: '状态码为200，创建成功'
        def actRequest = false
        def actResponse = false
        if (entity != null) {
            if (entity.getStatusCode().is2xxSuccessful()) {
                actRequest = true
                if (entity.getBody() != null && entity.getBody() instanceof Long) {
                    actResponse = true
                }
            }
        }
        actRequest == expRequest
        actResponse == expResponse
        where: '测试用例：'
        applyType | projectCode | projectName || expRequest | expResponse
        'test'    | 'csn'       | 'csn测试'     || true       | true
        'agile'    | 'csn'       | 'csn测试'     || true       | true
        'test1'    | 'csn'       | 'csn测试'     || true       | false
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
        StateMachineDTO stateMachineDTO = new StateMachineDTO()
        stateMachineDTO.setOrganizationId(testOrganizationId)
        stateMachineDTO.setName(name)
        stateMachineDTO.setType(statusType)
        when: '【敏捷】新增状态'
        HttpEntity<StateMachineDTO> httpEntity = new HttpEntity<>(stateMachineDTO)
        def entity = restTemplate.exchange(baseUrl + "/create_status_for_agile?state_machine_id=" + queryStateMachineId, HttpMethod.POST, httpEntity, StateMachineDTO, testOrganizationId)

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
}
