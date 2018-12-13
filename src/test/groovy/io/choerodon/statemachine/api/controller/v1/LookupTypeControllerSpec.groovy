package io.choerodon.statemachine.api.controller.v1

import io.choerodon.statemachine.IntegrationTestConfiguration
import io.choerodon.statemachine.api.dto.LookupTypeDTO
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
class LookupTypeControllerSpec extends Specification {
    @Autowired
    TestRestTemplate restTemplate
    @Shared
    String baseUrl = '/v1/organizations/{organization_id}/lookup_types'

    def "listLookupType"() {
        given: '准备工作'
        def testOrganizationId = organizationId
        when: '查询所有lookup type类型'
        ParameterizedTypeReference<List<LookupTypeDTO>> typeRef = new ParameterizedTypeReference<List<LookupTypeDTO>>() {
        }
        def entity = restTemplate.exchange(baseUrl, HttpMethod.GET, null, typeRef, testOrganizationId)
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
        organizationId || expRequest | expResponse
        1L             || true       | true
    }
}
