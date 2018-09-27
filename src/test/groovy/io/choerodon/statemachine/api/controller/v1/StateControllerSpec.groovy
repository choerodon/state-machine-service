package io.choerodon.statemachine.api.controller.v1

import io.choerodon.core.domain.Page
import io.choerodon.statemachine.IntegrationTestConfiguration
import io.choerodon.statemachine.api.dto.StatusDTO
import io.choerodon.statemachine.api.service.StatusService
import io.choerodon.statemachine.domain.Status
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
    StatusService service;

    @Shared
    Long testOrginzationId = 1L;

    @Shared
    String baseUrl = '/v1/organizations/{organization_id}/state'

    @Shared
    List<Status> list = new ArrayList<>()

    //初始化40条数据
    def setup() {
        Status del = new Status();
        service.delete(del);//清空数据
        list.clear();
        def testName = 'name'
        def testDescription = 'description'
        def testType = '1'
        for (int i = 0; i < 40; i++) {
            Status state = new Status();
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
        StatusDTO stateDTO = new StatusDTO();
        stateDTO.setName(testName)
        stateDTO.setDescription(testDescription)
        stateDTO.setOrganizationId(testOrginzationId)
        stateDTO.setType(testType)
        when: '状态写入数据库'
        HttpEntity<StatusDTO> httpEntity = new HttpEntity<>(stateDTO)
        def entity = restTemplate.exchange(baseUrl, HttpMethod.POST, httpEntity, StatusDTO, testOrginzationId)

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
        Status state = service.queryById(organizationId,list.get(0).getId());

        StatusDTO stateDTO = new StatusDTO();
        stateDTO.setType(state.getType());
        stateDTO.setObjectVersionNumber(state.getObjectVersionNumber());
        stateDTO.setName(updateName)

        when: '更新状态'
        HttpEntity<StatusDTO> httpEntity = new HttpEntity<>(stateDTO)
        def entity = restTemplate.exchange(baseUrl + '/{state_id}', HttpMethod.PUT, httpEntity, StatusDTO, testOrginzationId, state.getId())

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
        ParameterizedTypeReference<Page<StatusDTO>> typeRef = new ParameterizedTypeReference<Page<StatusDTO>>() {
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
        def statusId = list.get(0).getId()
        def entity = restTemplate.exchange(baseUrl + '/{state_id}', HttpMethod.DELETE, null, Boolean, testOrginzationId, statusId)

        then: '状态码为200，更新成功'
        entity.getStatusCode().is2xxSuccessful() == true
        Status state = new Status();
        state.setId(statusId);
        Status del = service.selectByPrimaryKey(state);
        del == null;

    }


}
