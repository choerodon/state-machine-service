package io.choerodon.statemachine.infra.schedule;

import io.choerodon.statemachine.infra.factory.MachineFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author shinan.chen
 * @since 2018/11/16
 */
@Component
public class ScheduledTask {

    @Autowired
    private MachineFactory machineFactory;

    /**
     * 每天凌晨清理状态机实例
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanInstance() {
        machineFactory.cleanInstances();
    }
}
