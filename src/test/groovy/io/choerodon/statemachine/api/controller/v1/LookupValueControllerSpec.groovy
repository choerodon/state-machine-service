package io.choerodon.statemachine.api.controller.v1

import io.choerodon.statemachine.IntegrationTestConfiguration
import io.choerodon.statemachine.api.dto.LookupTypeDTO
import io.choerodon.statemachine.api.dto.LookupTypeWithValuesDTO
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
class LookupValueControllerSpec extends Specification {
    @Autowired
    TestRestTemplate restTemplate
    @Shared
    String baseUrl = '/v1/organizations/{organization_id}/lookup_values'

    def "queryLookupValueByCode"() {
        given: '准备工作'
        def testOrganizationId = organizationId
        def testTypeCode = typeCode
        when: '根据type code查询其下的value值'
        def entity = restTemplate.exchange(baseUrl + "/{typeCode}", HttpMethod.GET, null, LookupTypeWithValuesDTO, testOrganizationId, testTypeCode)
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
        organizationId | typeCode               || expRequest | expResponse
        1L             | "state_machine_config" || true       | true
    }
}
