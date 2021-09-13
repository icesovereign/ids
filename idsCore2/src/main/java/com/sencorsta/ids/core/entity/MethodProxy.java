package com.sencorsta.ids.core.entity;


import cn.hutool.core.util.ClassUtil;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Method;

/**
 * 代理的类和方法
 *
 * @author daibin
 */
@AllArgsConstructor
@Data
public class MethodProxy {
//    String className;
//    String methodName;
//    String parameterName;
    Object obj;
    Method method;
    Class<Object> valueType;
}
