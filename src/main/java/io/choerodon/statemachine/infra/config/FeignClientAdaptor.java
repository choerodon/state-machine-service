package io.choerodon.statemachine.infra.config;

import feign.*;
import feign.codec.Decoder;
import feign.codec.Encoder;
import io.choerodon.statemachine.infra.feign.CustomFeignClientAdaptor;
import org.springframework.cloud.netflix.feign.FeignClientsConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author shinan.chen
 * @date 2018/9/27
 */
@Configuration
@Import(FeignClientsConfiguration.class)
public class FeignClientAdaptor {
    /**
     * 配置自定义feign
     *
     * @param client
     * @param decoder
     * @param encoder
     * @param interceptor
     * @return
     */
    @Bean
    public CustomFeignClientAdaptor instanceServiceImpl(Client client, Decoder decoder, Encoder encoder, RequestInterceptor interceptor) {
        return Feign.builder().encoder(encoder).decoder(decoder)
                .client(client)
                .contract(new Contract.Default())
                .requestInterceptor(interceptor)
                .target(Target.EmptyTarget.create(CustomFeignClientAdaptor.class));
    }
}
