package io.choerodon.statemachine.api.controller.v1

import io.choerodon.asgard.saga.feign.SagaClient
import io.choerodon.core.domain.Page
import io.choerodon.statemachine.IntegrationTestConfiguration
import io.choerodon.statemachine.api.dto.StateMachineDTO
import io.choerodon.statemachine.api.dto.StateMachineWithStatusDTO
import io.choerodon.statemachine.api.service.InitService
import io.choerodon.statemachine.api.service.StateMachineService
import io.choerodon.statemachine.domain.StateMachine
import io.choerodon.statemachine.domain.StateMachineNode
import io.choerodon.statemachine.domain.StateMachineNodeDraft
import io.choerodon.statemachine.domain.StateMachineTransform
import io.choerodon.statemachine.domain.StateMachineTransformDraft
import io.choerodon.statemachine.domain.Status
import io.choerodon.statemachine.domain.event.ProjectEvent
import io.choerodon.statemachine.infra.enums.StateMachineStatus
import io.choerodon.statemachine.infra.mapper.StateMachineMapper
import io.choerodon.statemachine.infra.mapper.StateMachineNodeDraftMapper
import io.choerodon.statemachine.infra.mapper.StateMachineNodeMapper
import io.choerodon.statemachine.infra.mapper.StateMachineTransformDraftMapper
import io.choerodon.statemachine.infra.mapper.StateMachineTransformMapper
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
    StateMachineTransformMapper transformMapper
    @Autowired
    StateMachineTransformDraftMapper transformDraftMapper
    @Autowired
    StateMachineService stateMachineService
    @Autowired
    InitService initService
    @Autowired
    SagaClient sagaClient
    @Shared
    def needInit = true
    @Shared
    def needClean = false
    @Shared
    Long testOrganizationId = 1L
    @Shared
    Long testProjectId = 1L
    @Shared
    String baseUrl = '/v1/organizations/{organization_id}/state_machines'
    @Shared
    def stateMachineList = []
    @Shared
    def stateMachineIds = []
    /**
     * 初始化
     */
    void setup() {
        if (needInit) {
            needInit = false
            //mock
            sagaClient.startSaga(_, _) >> null

            //初始化状态
            initService.initStatus(testOrganizationId)
            //初始化默认状态机
            def stateMachineId = initService.initDefaultStateMachine(testOrganizationId)
            //发布状态机
            stateMachineService.deploy(testOrganizationId, stateMachineId, true)
            //新增一个状态机
            StateMachineDTO stateMachineDTO = new StateMachineDTO()
            stateMachineDTO.setStatus(StateMachineStatus.CREATE)
            stateMachineDTO.setName("新状态机")
            stateMachineDTO.setDescription("描述")
            stateMachineDTO.setOrganizationId(testOrganizationId)
            stateMachineDTO = stateMachineService.create(testOrganizationId, stateMachineDTO)
            stateMachineList.add(stateMachineDTO)
            stateMachineIds.add(stateMachineDTO.getId())
            //新增一个状态机
            StateMachineDTO stateMachineDTO2 = new StateMachineDTO()
            stateMachineDTO2.setStatus(StateMachineStatus.CREATE)
            stateMachineDTO2.setName("新新状态机")
            stateMachineDTO2.setDescription("描述")
            stateMachineDTO2.setOrganizationId(testOrganizationId)
            stateMachineDTO2 = stateMachineService.create(testOrganizationId, stateMachineDTO2)
            stateMachineList.add(stateMachineDTO2)
            stateMachineIds.add(stateMachineDTO2.getId())
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
            //删除转换
            StateMachineTransform transform = new StateMachineTransform()
            transform.organizationId = testOrganizationId
            transformMapper.delete(transform)
            //删除草稿转换
            StateMachineTransformDraft transformDraft = new StateMachineTransformDraft()
            transformDraft.organizationId = testOrganizationId
            transformDraftMapper.delete(transformDraft)
        }
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
        name       | description | param || expRequest | expResponseSize
        null       | null        | null  || true       | 3
        '默认'       | null        | null  || true       | 1
        null       | null        | '默认'  || true       | 1
        'notFound' | null        | null  || true       | 0
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
        testName | testDescription     || expRequest | expResponse
        '新状态机1'  | 'test-description1' || true       | true
        '默认状态机'  | 'test-description1' || true       | false
        null     | 'test-description1' || true       | false
    }

    def "update"() {
        given: '准备工作'
        StateMachineDTO stateMachineDTO = stateMachineList.get(0)
        stateMachineDTO.setName(updateName)

        when: '更新状态'
        HttpEntity<StateMachineDTO> httpEntity = new HttpEntity<>(stateMachineDTO)
        def entity = restTemplate.exchange(baseUrl + '/{state_machine_id}', HttpMethod.PUT, httpEntity, StateMachineDTO, testOrganizationId, stateMachineDTO.getId())

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
        updateName || expRequest | expResponse
        '新状态机2099' || true       | true
        '默认状态机'    || true       | false
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
        stateMachineId     || expRequest | expResponse
        stateMachineIds[0] || true       | true
        stateMachineIds[0] || true       | false
        9999L              || true       | false
        null               || false      | false
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
        stateMachineId     || expRequest | expResponse
        stateMachineIds[0] || true       | true
        9999L              || true       | false
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
        stateMachineId     || expRequest | expResponse
        stateMachineIds[0] || true       | true
        9999L              || true       | false
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
        stateMachineId     || expRequest | expResponse
        stateMachineIds[0] || true       | true
        9999L              || true       | false
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
        stateMachineId     || expRequest | expResponse
        stateMachineIds[0] || true       | true
        999L               || true       | false
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
        name    || expRequest | expResponse
        '新新名字'  || true       | false
        '默认状态机' || true       | true
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
        'agile'   | 'csn'       | 'csn测试'     || true       | true
        'test1'   | 'csn'       | 'csn测试'     || true       | false
    }

    def "activeStateMachines"() {
        given: '准备工作'
        List<Long> testStateMachineIds = new ArrayList<>()
        testStateMachineIds.add(stateMachineId)
        when: '【issue服务】批量活跃状态机'
        HttpEntity<List<Long>> httpEntity = new HttpEntity<>(testStateMachineIds)
        def entity = restTemplate.exchange(baseUrl + "/active_state_machines", HttpMethod.POST, httpEntity, Object, testOrganizationId)

        then: '状态码为200，创建成功'
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
        stateMachineId     || expRequest | expResponse
        stateMachineIds[0] || true       | true
        null               || true       | true
        999L               || true       | true
    }

    def "notActiveStateMachines"() {
        given: '准备工作'
        List<Long> testStateMachineIds = new ArrayList<>()
        testStateMachineIds.add(stateMachineId)
        when: '【issue服务】批量使活跃状态机变成未活跃'
        HttpEntity<List<Long>> httpEntity = new HttpEntity<>(testStateMachineIds)
        def entity = restTemplate.exchange(baseUrl + "/not_active_state_machines", HttpMethod.POST, httpEntity, Object, testOrganizationId)

        then: '状态码为200，创建成功'
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
        stateMachineId     || expRequest | expResponse
        stateMachineIds[0] || true       | true
        null               || true       | true
        999L               || true       | true
    }

    def "queryAllWithStatus"() {
        given: '准备工作'
        def queryId = organizationId

        when: '【issue服务】获取组织下所有状态机，包含状态'
        ParameterizedTypeReference<List<StateMachineWithStatusDTO>> typeRef = new ParameterizedTypeReference<List<StateMachineWithStatusDTO>>() {
        }
        def entity = restTemplate.exchange(baseUrl + "/query_all_with_status", HttpMethod.GET, null, typeRef, queryId)

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

    def "queryByOrgId"() {
        given: '准备工作'
        def queryId = organizationId

        when: '【[issue服务】获取组织下所有状态机（无配置）'
        ParameterizedTypeReference<List<StateMachineDTO>> typeRef = new ParameterizedTypeReference<List<StateMachineDTO>>() {
        }
        def entity = restTemplate.exchange(baseUrl + "/query_by_org_id", HttpMethod.GET, null, typeRef, queryId)

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
        stateMachineId     || expRequest | expResponse
        stateMachineIds[0] || true       | true
        999L               || true       | false
    }

}
