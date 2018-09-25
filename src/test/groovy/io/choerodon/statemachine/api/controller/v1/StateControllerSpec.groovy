package io.choerodon.statemachine.api.controller.v1

import io.choerodon.core.domain.Page
import io.choerodon.statemachine.IntegrationTestConfiguration
import io.choerodon.statemachine.api.dto.StateDTO
import io.choerodon.statemachine.api.service.StateService
import io.choerodon.statemachine.domain.State
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import spock.lang.Shared
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * @author peng.jiang@hand-china.com
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class StateControllerSpec extends Specification {

    @Autowired
    TestRestTemplate restTemplate

    @Autowired
    StateService service;

    @Shared
    Long testOrginzationId = 1L;

    @Shared
    String baseUrl = '/v1/organizations/{organization_id}/state'

    @Shared
    List<State> list = new ArrayList<>()

    //初始化40条数据
    def setup() {
        State del = new State();
        service.delete(del);//清空数据
        list.clear();
        def testName = 'name'
        def testDescription = 'description'
        def testType = '1'
        for (int i = 0; i < 40; i++) {
            State state = new State();
            state.setName(testName + i)
            state.setDescription(testDescription + i)
            state.setOrganizationId(testOrginzationId)
            state.setType(testType)
            int isInsert = service.insert(state)
            if (isInsert == 1) {
                list.add(state)
            }
        }
    }

    def "create"() {
        given: '创建状态'
        StateDTO stateDTO = new StateDTO();
        stateDTO.setName(testName)
        stateDTO.setDescription(testDescription)
        stateDTO.setOrganizationId(testOrginzationId)
        stateDTO.setType(testType)
        when: '状态写入数据库'
        HttpEntity<StateDTO> httpEntity = new HttpEntity<>(stateDTO)
        def entity = restTemplate.exchange(baseUrl, HttpMethod.POST, httpEntity, StateDTO, testOrginzationId)

        then: '状态码为200，创建成功'
        entity.getStatusCode().is2xxSuccessful() == isSuccess
        (entity.getBody().getId() != null) == reponseDTO

        where: '测试用例：'
        testName     | testDescription     | testType || isSuccess | reponseDTO
        'test-name1' | 'test-description1' | '1'      || true      | true
        'test-name2' | 'test-description2' | '1234'   || true      | false
//        null         | 'test-description3' | '1'      || true      | false
//        'test-name4' | 'test-description4' | null     || true      | false
    }

    def "update"() {
        given: '初始化一个状态'
        State state = new State();
        state.setId(list.get(0).getId());
        state = service.selectByPrimaryKey(state);

        StateDTO stateDTO = new StateDTO();
        stateDTO.setType(state.getType());
        stateDTO.setObjectVersionNumber(state.getObjectVersionNumber());
        stateDTO.setName(updateName)

        when: '更新状态'
        HttpEntity<StateDTO> httpEntity = new HttpEntity<>(stateDTO)
        def entity = restTemplate.exchange(baseUrl + '/{state_id}', HttpMethod.PUT, httpEntity, StateDTO, testOrginzationId, state.getId())

        then: '状态码为200，更新成功'
        entity.getStatusCode().is2xxSuccessful() == isSuccess
        (entity.getBody().getId() != null) == reponseDTO

        where: '测试用例：'
        updateName   || isSuccess | reponseDTO
        'test-name1' || true      | true
//        null         || false
    }

    def "pagingQuery"() {

        when: '分页查询'
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
        if (type != null) {
            url = url + "&type=" + type
        }
        ParameterizedTypeReference<Page<StateDTO>> typeRef = new ParameterizedTypeReference<Page<StateDTO>>() {
        };
        def entity = restTemplate.exchange(url, HttpMethod.GET, null, typeRef, testOrginzationId)

        then: '返回结果'
        entity.getStatusCode().is2xxSuccessful() == isSuccess
        entity.getBody().size() == size

        where: '测试用例：'
        name     | description     | type | param  || isSuccess | size
        null     | null            | null | null   || true      | 20
        'name19' | null            | null | null   || true      | 1
        null     | 'description19' | null | null   || true      | 1
        'name19' | 'description19' | null | null   || true      | 1
        null     | null            | null | 'name' || true      | 20
        null     | null            | "1"  | null   || true      | 20
        null     | null            | "2"  | null   || true      | 0
    }

    def "delete"() {
        when: '发布状态机'
        def stateId = list.get(0).getId()
        def entity = restTemplate.exchange(baseUrl + '/{state_id}', HttpMethod.DELETE, null, Boolean, testOrginzationId, stateId)

        then: '状态码为200，更新成功'
        entity.getStatusCode().is2xxSuccessful() == true
        State state = new State();
        state.setId(stateId);
        State del = service.selectByPrimaryKey(state);
        del == null;

    }


}
