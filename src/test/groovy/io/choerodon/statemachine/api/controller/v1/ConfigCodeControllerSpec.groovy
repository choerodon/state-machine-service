package io.choerodon.statemachine.api.controller.v1


import io.choerodon.statemachine.IntegrationTestConfiguration
import io.choerodon.statemachine.api.dto.ConfigCodeDTO
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
import org.springframework.http.HttpMethod
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * @author shinan.chen
 * @since 2018/12/13
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Stepwise
class ConfigCodeControllerSpec extends Specification {
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
    ConfigCodeMapper configCodeMapper
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
    String baseUrl = '/v1/organizations/{organization_id}/config_codes'
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
            initService.createStateMachineDetail(testOrganizationId, 10L, "default")
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
            ConfigCode configCode = new ConfigCode()
            configCode.service = "agile"
            configCode.type = ConfigType.ACTION
            configCode.name = "发消息"
            configCode.code = "send_action"
            configCodeMapper.insert(configCode)
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

    def "queryByTransformId"() {
        given: '准备工作'
        def testTransformId = transformId
        def testType = type
        when: '获取未配置的条件，验证，后置动作等列表'
        ParameterizedTypeReference<List<ConfigCodeDTO>> typeRef = new ParameterizedTypeReference<List<ConfigCodeDTO>>() {
        }
        def entity = restTemplate.exchange(baseUrl + "/{transform_id}?type=" + testType, HttpMethod.GET, null, typeRef, testOrganizationId, testTransformId)
        then: '状态码为200，创建成功'
        def actRequest = false
        def actResponse = false
        if (entity != null) {
            if (entity.getStatusCode().is2xxSuccessful()) {
                actRequest = true
                if (entity.getBody() != null && entity.getBody().size() > 0) {
                    actResponse = true
                    needClean = true
                }
            }
        }
        actRequest == expRequest
        actResponse == expResponse
        where: '测试用例：'
        transformId | type                 || expRequest | expResponse
        10L         | ConfigType.ACTION    || true       | true
        10L         | ConfigType.VALIDATOR || true       | false
    }

    def "clean"() {
        needClean = true
    }
}
