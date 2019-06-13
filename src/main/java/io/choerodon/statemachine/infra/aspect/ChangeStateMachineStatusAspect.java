package io.choerodon.statemachine.infra.aspect;

import io.choerodon.core.exception.CommonException;
import io.choerodon.mybatis.entity.Criteria;
import io.choerodon.statemachine.api.service.StateMachineService;
import io.choerodon.statemachine.domain.StateMachine;
import io.choerodon.statemachine.infra.enums.StateMachineStatus;
import io.choerodon.statemachine.infra.mapper.StateMachineMapper;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author shinan.chen
 * @since 2018/11/23
 */
@Aspect
@Component
@Transactional(rollbackFor = Exception.class)
public class ChangeStateMachineStatusAspect {
    @Autowired
    private StateMachineMapper stateMachineMapper;
    @Autowired
    private StateMachineService stateMachineService;

    @Pointcut("@annotation(io.choerodon.statemachine.infra.annotation.ChangeStateMachineStatus)")
    public void updateStatusPointcut() {
        throw new UnsupportedOperationException();
    }

    @Around("updateStatusPointcut()")
    public Object interceptor(ProceedingJoinPoint pjp) {
        // 下面两个数组中，参数值和参数名的个数和位置是一一对应的。
        Object[] args = pjp.getArgs();
        String[] argNames = ((MethodSignature) pjp.getSignature()).getParameterNames();
        Long stateMachineId = null;
        for (int i = 0; i < argNames.length; i++) {
            if (argNames[i].equals("stateMachineId")) {
                stateMachineId = Long.valueOf(args[i] + "");
            }
        }
        StateMachine stateMachine = stateMachineMapper.selectByPrimaryKey(stateMachineId);
        if (stateMachine == null) {
            throw new CommonException("error.stateMachine.notFound");
        }
        if (stateMachine.getStatus().equals(StateMachineStatus.ACTIVE)) {
            stateMachine.setStatus(StateMachineStatus.DRAFT);
            Criteria criteria = new Criteria();
            criteria.update("status");
            stateMachineMapper.updateByPrimaryKeyOptions(stateMachine, criteria);
        }

        try {
            return pjp.proceed();
        } catch (Throwable e) {
            throw new CommonException("error.changeStateMachineStatusAspect.proceed", e);
        }
    }
}
