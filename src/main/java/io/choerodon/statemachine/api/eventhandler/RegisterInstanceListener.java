package io.choerodon.statemachine.api.eventhandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.choerodon.statemachine.api.dto.RegisterInstancePayloadDTO;
import io.choerodon.statemachine.api.service.RegisterInstanceService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.concurrent.TimeUnit;

@Component
public class RegisterInstanceListener {

    private static final String REGISTER_TOPIC = "register-server";
    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterInstanceListener.class);
    public static final String STATUS_UP = "UP";
    public static final String STATUS_DOWN = "DOWN";
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${choerodon.statemachine.fetch.time:10}")
    private Integer statemachineFetchTime;

    private RegisterInstanceService registerInstanceService;

    public RegisterInstanceListener(RegisterInstanceService registerInstanceService) {
        this.registerInstanceService = registerInstanceService;
    }

    /**
     * 监听eureka-instance消息队列的新消息处理
     *
     * @param record 消息信息
     */
    @KafkaListener(topics = REGISTER_TOPIC)
    public void handle(ConsumerRecord<byte[], byte[]> record) {
        String message = new String(record.value());
        try {
            LOGGER.info("receive message from register-server, {}", message);
            RegisterInstancePayloadDTO payload = mapper.readValue(message, RegisterInstancePayloadDTO.class);
            Observable.just(payload)
                    .map(t -> {
                        if (STATUS_UP.equals(payload.getStatus())) {
                            registerInstanceService.instanceUpConsumer(payload);
                        } else if (STATUS_DOWN.equals(payload.getStatus())) {
                            registerInstanceService.instanceDownConsumer(payload);
                        }
                        return t;
                    })
                    .retryWhen(x -> x.zipWith(Observable.range(1, statemachineFetchTime),
                            (t, retryCount) -> {
                                if (retryCount >= statemachineFetchTime) {
                                    if (t instanceof RemoteAccessException || t instanceof RestClientException) {
                                        LOGGER.warn("error.registerConsumer.fetchDataError, payload {}", payload);
                                    } else {
                                        LOGGER.warn("error.registerConsumer.msgConsumerError, payload {}", payload);
                                    }
                                }
                                return retryCount;
                            }).flatMap(y -> Observable.timer(2, TimeUnit.SECONDS)))
                    .subscribeOn(Schedulers.io())
                    .subscribe((RegisterInstancePayloadDTO registerInstancePayload) -> {
                    });
        } catch (Exception e) {
            LOGGER.warn("error happened when handle message， {} cause {}", message, e.getCause());
        }
    }


}
