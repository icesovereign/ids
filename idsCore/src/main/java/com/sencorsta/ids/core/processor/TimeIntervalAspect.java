package com.sencorsta.ids.core.processor;

import cn.hutool.aop.aspects.SimpleAspect;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.lang.Console;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

/**
 * 通过日志打印方法的执行时间的切面
 *
 * @author Looly
 */
@Slf4j
public class TimeIntervalAspect extends SimpleAspect {
    private static final long serialVersionUID = 1L;

    private final TimeInterval interval = new TimeInterval();

    @Override
    public boolean before(Object target, Method method, Object[] args) {
        interval.start();
        return true;
    }

    @Override
    public boolean after(Object target, Method method, Object[] args, Object returnVal) {
        log.trace("Method [{}.{}] execute spend [{}]ms return value [{}]",
                target.getClass().getName(),
                method.getName(),
                interval.intervalMs(),
                returnVal);
        return true;
    }
}
