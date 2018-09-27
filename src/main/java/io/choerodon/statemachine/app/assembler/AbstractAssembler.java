package io.choerodon.statemachine.app.assembler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 抽象Assembler转换类,如果需要简单的转换，继承此类即可，要实现自己的，则重写方法
 *
 * @author dinghuang123@gmail.com
 * * @since 2018/9/7
 */
abstract class AbstractAssembler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAssembler.class);

    /**
     * 转换到目标类
     *
     * @param source source
     * @param tClass tClass
     * @return target
     */
    @SuppressWarnings("unchecked")
    public <T, V> V toTarget(T source, Class<V> tClass) {
        if (source != null) {
            try {
                V target = tClass.newInstance();
                if (target != null) {
                    BeanUtils.copyProperties(source, target);
                }
                return target;
            } catch (InstantiationException | IllegalAccessException e) {
                LOGGER.info("Exception", e);
            }
        }
        return null;
    }

    /**
     * 转换到目标类
     *
     * @param source source
     * @param tClass tClass
     * @param ignore 要忽略的字段
     * @return target
     */
    @SuppressWarnings("unchecked")
    public <T, V> V toTarget(T source, Class<V> tClass, String... ignore) {
        if (source != null) {
            try {
                V target = tClass.newInstance();
                if (target != null) {
                    BeanUtils.copyProperties(source, target, ignore);
                }
                return target;
            } catch (InstantiationException | IllegalAccessException e) {
                LOGGER.info("Exception", e);
            }
        }
        return null;
    }

    /**
     * List转换到目标类
     *
     * @param source source
     * @param tClass tClass
     * @return target
     */
    @SuppressWarnings("unchecked")
    public <T extends List, V> List<V> toTargetList(T source, Class<V> tClass) {
        if (source != null && !source.isEmpty()) {
            List<V> targetList = new ArrayList<>(source.size());
            source.forEach(s -> {
                V target = toTarget(s, tClass);
                targetList.add(target);
            });
            return targetList;
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * List转换到目标类
     *
     * @param source source
     * @param tClass tClass
     * @param ignore ignore
     * @return target
     */
    @SuppressWarnings("unchecked")
    public <T extends List, V> List<V> toTargetList(T source, Class<V> tClass, String... ignore) {
        if (source != null && !source.isEmpty()) {
            List<V> targetList = new ArrayList<>(source.size());
            source.forEach(s -> {
                V target = toTarget(s, tClass, ignore);
                targetList.add(target);
            });
            return targetList;
        } else {
            return new ArrayList<>();
        }
    }


}
