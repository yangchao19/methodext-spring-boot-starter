package com.yang.middleware.methodext;

import com.alibaba.fastjson.JSON;
import com.yang.middleware.methodext.annotation.DoMethodExt;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * @description:
 * @author：杨超
 * @date: 2023/7/16
 * @Copyright：
 */
@Aspect
public class DoMethodExtPoint {

    private Logger logger = LoggerFactory.getLogger(DoMethodExtPoint.class);

    @Pointcut("@annotation(com.yang.middleware.methodext.annotation.DoMethodExt)")
    public void aopPoint(){

    }

    @Around("aopPoint()")
    public Object DoRouter(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取内容
        Method method = getMethod(joinPoint);
        DoMethodExt doMethodExt = method.getAnnotation(DoMethodExt.class);
        // 获取拦截方法名
        String methodName = doMethodExt.method();
        // 功能处理
        Class<?> clazz = getClass(joinPoint);
        Method methodExt = clazz.getMethod(methodName, method.getParameterTypes());
        Class<?> returnType = methodExt.getReturnType();

        // 判断返回类型
        if (!"boolean".equals(returnType.getName())) {
            throw new RuntimeException("annotation @DoMethodExt set method:" + methodName + "returnType is no boolean");
        }

        // 获取代理对象，通过反射执行注解的方法
        boolean invoke = (boolean)methodExt.invoke(joinPoint.getThis(), joinPoint.getArgs());
        return invoke ? joinPoint.proceed() : JSON.parseObject(doMethodExt.returnJson(), method.getReturnType());
    }

    private Method getMethod(JoinPoint jp) throws NoSuchMethodException {
        Signature signature = jp.getSignature();
        MethodSignature methodSignature = (MethodSignature)signature;
        return jp.getTarget().getClass().getMethod(methodSignature.getName(), methodSignature.getParameterTypes());
    }

    private Class<? extends Object> getClass(JoinPoint jp) {
        return jp.getTarget().getClass();
    }
}
