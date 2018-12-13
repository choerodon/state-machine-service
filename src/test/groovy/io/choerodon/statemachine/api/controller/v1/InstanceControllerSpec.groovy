package io.choerodon.statemachine.api.controller.v1

import io.choerodon.statemachine.IntegrationTestConfiguration
import io.choerodon.statemachine.api.dto.ExecuteResult
import io.choerodon.statemachine.api.dto.InputDTO
import io.choerodon.statemachine.api.dto.StateMachineDTO
import io.choerodon.statemachine.api.service.InitService
import io.choerodon.statemachine.api.service.StateMachineService
import io.choerodon.statemachine.domain.*
import io.choerodon.statemachine.infra.enums.*
import io.choerodon.statemachine.infra.mapper.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * @author shinan.chen
 * @since 2018/12/12
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Stepwise
class InstanceControllerSpec extends Specification {
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
    @Shared
    def needInit = true
    @Shared
    def needClean = false
    @Shared
    Long testOrganizationId = 1L
    @Shared
    String baseUrl = '/v1/organizations/{organization_id}/instances'
    @Shared
    def statusList = []
    @Shared
    List<StateMachineDTO> stateMachineList = new ArrayList<>()
    @Shared
    def stateMachineIds = []
    /**
     * 初始化
     */
    void setup() {
        if (needInit) {
            needInit = false
            //初始化状态
            statusList = initService.initStatus(testOrganizationId)
            //初始化默认状态机
            Long stateMachineId = initService.initDefaultStateMachine(testOrganizationId)
            //发布状态机
            stateMachineService.deploy(testOrganizationId, stateMachineId, true)

            StateMachineDTO stateMachineDTO = stateMachineService.queryStateMachineWithConfigById(testOrganizationId, stateMachineId, false)
            stateMachineList.add(stateMachineDTO)
            stateMachineIds.add(stateMachineId)

            //初始化一个状态机
            StateMachine stateMachine = new StateMachine()
            stateMachine.setId(10L)
            stateMachine.setOrganizationId(testOrganizationId)
            stateMachine.setName("新状态机")
            stateMachine.setDescription("新状态机")
            stateMachine.setStatus(StateMachineStatus.CREATE)
            stateMachine.setDefault(false)
            stateMachineMapper.insert(stateMachine)

            //创建初始状态机节点和转换
            initService.createStateMachineDetail(testOrganizationId, 10L)
            //新增一个状态
            Status status = new Status()
            status.setId(10L)
            status.setName("新状态")
            status.setDescription("新状态")
            status.setOrganizationId(testOrganizationId)
            status.setType(StatusType.DOING)
            statusMapper.insert(status)
            //新增一个节点
            StateMachineNodeDraft nodeDraft = new StateMachineNodeDraft()
            nodeDraft.id = 10L
            nodeDraft.organizationId = testOrganizationId
            nodeDraft.statusId = 10L
            nodeDraft.type = NodeType.CUSTOM
            nodeDraft.positionX = 100
            nodeDraft.positionY = 100
            nodeDraft.stateMachineId = 10L
            nodeDraft.allStatusTransformId = 10L
            nodeDraftMapper.insert(nodeDraft)
            //新增一个转换
            StateMachineTransformDraft transformDraft = new StateMachineTransformDraft()
            transformDraft.id = 10L
            transformDraft.organizationId = testOrganizationId
            transformDraft.name = "新转换"
            transformDraft.description = "新转换"
            transformDraft.type = TransformType.ALL
            transformDraft.conditionStrategy = TransformConditionStrategy.ALL
            transformDraft.endNodeId = 10L
            transformDraft.startNodeId = 0L
            transformDraft.stateMachineId = 10L
            transformDraftMapper.insert(transformDraft)
            //发布状态机
            stateMachineService.deploy(testOrganizationId, 10L, false)
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

    def "startInstance"() {
        given: '准备工作'
        def url = baseUrl + "/start_instance?1=1"
        if (serviceCode != null) {
            url = url + "&service_code=" + serviceCode
        }
        if (stateMachineId != null) {
            url = url + "&state_machine_id=" + stateMachineId
        }
        InputDTO inputDTO = new InputDTO()
        inputDTO.input = input
        inputDTO.instanceId = instanceId
        inputDTO.invokeCode = invokeCode
        when: '创建状态机实例'
        HttpEntity<InputDTO> httpEntity = new HttpEntity<>(inputDTO)
        def entity = restTemplate.exchange(url, HttpMethod.POST, httpEntity, ExecuteResult, testOrganizationId)
        then: '状态码为200，创建成功'
        def actRequest = false
        def actResponse = false
        if (entity != null) {
            if (entity.getStatusCode().is2xxSuccessful()) {
                actRequest = true
                if (entity.getBody() != null && entity.getBody().getSuccess() != null) {
                    actResponse = entity.getBody().getSuccess()
                }
            }
        }
        actRequest == expRequest
        actResponse == expResponse
        where: '测试用例：'
        stateMachineId | serviceCode | input | instanceId | invokeCode || expRequest | expResponse
        10L            | 'agile'     | null  | 1L         | "create"   || true       | true
        10L            | 'test'      | null  | 1L         | "create"   || true       | true
        11L            | 'agile'     | null  | 1L         | "create"   || true       | false
    }

    def "executeTransform"() {
        given: '准备工作'
        def url = baseUrl + "/execute_transform?1=1"
        if (serviceCode != null) {
            url = url + "&service_code=" + serviceCode
        }
        if (stateMachineId != null) {
            url = url + "&state_machine_id=" + stateMachineId
        }
        if (currentStatusId != null) {
            url = url + "&current_status_id=" + currentStatusId
        }
        if (transformId != null) {
            url = url + "&transform_id=" + transformId
        }
        InputDTO inputDTO = new InputDTO()
        inputDTO.input = input
        inputDTO.instanceId = instanceId
        inputDTO.invokeCode = invokeCode
        when: '执行状态转换，并返回转换后的状态'
        HttpEntity<InputDTO> httpEntity = new HttpEntity<>(inputDTO)
        def entity = restTemplate.exchange(url, HttpMethod.POST, httpEntity, ExecuteResult, testOrganizationId)
        then: '状态码为200，创建成功'
        def actRequest = false
        def actResponse = false
        if (entity != null) {
            if (entity.getStatusCode().is2xxSuccessful()) {
                actRequest = true
                if (entity.getBody() != null && entity.getBody().getSuccess() != null) {
                    actResponse = entity.getBody().getSuccess()
                }
            }
        }
        actRequest == expRequest
        actResponse == expResponse
        where: '测试用例：'
        serviceCode | stateMachineId | currentStatusId | transformId | input | instanceId | invokeCode || expRequest | expResponse
        'agile'     | 10L            | 10L             | 10L         | null  | 1L         | "create"   || true       | true
        'agile'     | 11L            | 10L             | 10L         | null  | 1L         | "create"   || true       | false
        'agile'     | 10L            | 11L             | 10L         | null  | 1L         | "create"   || true       | false
        'agile'     | 10L            | 10L             | 11L         | null  | 1L         | "create"   || true       | false
    }

    def "queryListTransform"() {
        given: '准备工作'
        def url = baseUrl + "/transform_list?1=1"
        if (serviceCode != null) {
            url = url + "&service_code=" + serviceCode
        }
        if (stateMachineId != null) {
            url = url + "&state_machine_id=" + stateMachineId
        }
        if (currentStatusId != null) {
            url = url + "&current_status_id=" + currentStatusId
        }
        if (instanceId != null) {
            url = url + "&instance_id=" + instanceId
        }
        when: '获取当前状态拥有的转换列表，feign调用对应服务的条件验证'
        def entity = restTemplate.exchange(url, HttpMethod.GET, null, Object, testOrganizationId)
        then: '状态码为200，创建成功'
        def actRequest = false
        def actResponse = false
        if (entity != null) {
            if (entity.getStatusCode().is2xxSuccessful()) {
                actRequest = true
                if (entity.getBody() != null && entity.getBody().size() > 0) {
                    actResponse = true
                }
            }
        }
        actRequest == expRequest
        actResponse == expResponse
        where: '测试用例：'
        serviceCode | stateMachineId | currentStatusId | instanceId || expRequest | expResponse
        'agile'     | 10L            | 10L             | 1L         || true       | true
    }

    def "queryInitStatusId"() {
        given: '准备工作'
        def url = baseUrl + "/query_init_status_id?1=1"
        if (stateMachineId != null) {
            url = url + "&state_machine_id=" + stateMachineId
        }
        when: '获取状态机的初始状态'
        def entity = restTemplate.exchange(url, HttpMethod.GET, null, Object, testOrganizationId)
        then: '状态码为200，创建成功'
        def actRequest = false
        def actResponse = false
        if (entity != null) {
            if (entity.getStatusCode().is2xxSuccessful()) {
                actRequest = true
                if (entity.getBody() != null&& entity.getBody() instanceof Integer) {
                    actResponse = true
                }
            }
        }
        actRequest == expRequest
        actResponse == expResponse
        where: '测试用例：'
        stateMachineId || expRequest | expResponse
        10L            || true       | true
        11L            || true       | false
    }

    def "queryInitStatusIds"() {
        given: '准备工作'
        def url = baseUrl + "/query_init_status_ids?1=1"
        if (stateMachineId != null) {
            url = url + "&state_machine_id=" + stateMachineId
        }
        when: '获取状态机对应的初始状态Map'
        ParameterizedTypeReference<Map<Long, Long>> typeRef = new ParameterizedTypeReference<Map<Long, Long>>() {
        }
        def entity = restTemplate.exchange(url, HttpMethod.GET, null, typeRef, testOrganizationId)
        then: '状态码为200，创建成功'
        def actRequest = false
        def actResponse = false
        if (entity != null) {
            if (entity.getStatusCode().is2xxSuccessful()) {
                actRequest = true
                if (entity.getBody() != null && entity.getBody() instanceof Map) {
                    actResponse = true
                }
            }
        }
        actRequest == expRequest
        actResponse == expResponse
        where: '测试用例：'
        stateMachineId || expRequest | expResponse
        10L            || true       | true
    }

    def "cleanInstance"() {
        given: '准备工作'
        def url = baseUrl + "/cleanInstance?1=1"
        if (isCleanAll != null) {
            url = url + "&is_clean_all=" + isCleanAll
        }
        when: '手动清理状态机实例'
        def entity = restTemplate.exchange(url, HttpMethod.GET, null, Object, testOrganizationId)
        then: '状态码为200，创建成功'
        def actRequest = false
        def actResponse = false
        if (entity != null) {
            if (entity.getStatusCode().is2xxSuccessful()) {
                actRequest = true
                actResponse = true
                needClean = true
            }
        }
        actRequest == expRequest
        actResponse == expResponse
        where: '测试用例：'
        isCleanAll || expRequest | expResponse
        true       || true       | true
        false      || true       | true
    }
}
