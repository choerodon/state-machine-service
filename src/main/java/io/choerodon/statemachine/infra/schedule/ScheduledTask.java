package io.choerodon.statemachine.infra.schedule;

import io.choerodon.statemachine.infra.factory.MachineFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author shinan.chen
 * @since 2018/11/16
 */
@Component
public class ScheduledTask {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTask.class);
    @Autowired
    private MachineFactory machineFactory;

    /**
     * 每天凌晨清理状态机实例
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanInstance() {
        logger.info("【定时任务】清理内存中状态机构建器个，状态机实例个");
    }
}
