package com.mypopsy.hook;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

@Aspect
public class HookAspect {

    private HookAspect() {}

    @Pointcut("@annotation(com.mypopsy.hook.annotations.Register)")
    public void annotationRegister() {}

    @Pointcut("@annotation(com.mypopsy.hook.annotations.Unregister)")
    public void annotationUnregister() {}

    @Pointcut("@annotation(com.mypopsy.hook.annotations.Hooked)")
    public void annotationHooked() {}

    @Pointcut("execution(* *(..))")
    public void atExecution(){}

    @Before("annotationRegister() && atExecution()")
    public void register(JoinPoint pointcut) {
        Hooker.instance().register(pointcut.getTarget());
    }

    @After("annotationUnregister() && atExecution()")
    public void unregister(JoinPoint pointcut) {
        Hooker.instance().unregister(pointcut.getTarget());
    }

    @Around("annotationHooked() && atExecution()")
    public Object hooked(final ProceedingJoinPoint joinPoint) throws Throwable {
        return Hooker.instance().proceed(new HookedMethodImpl(joinPoint));
    }

    static private class HookedMethodImpl extends HookedMethod {

        private ProceedingJoinPoint joinPoint;

        public HookedMethodImpl(ProceedingJoinPoint joinPoint) {
            this.joinPoint = joinPoint;
        }

        @Override
        public Object proceed() throws Throwable {
            return joinPoint.proceed();
        }

        @Override
        public Object proceed(Object... args) throws Throwable {
            return joinPoint.proceed(args);
        }

        @Override
        public Method method() {
            return ((MethodSignature)joinPoint.getSignature()).getMethod();
        }

        @Override
        public Object target() {
            return joinPoint.getTarget();
        }

        @Override
        public Object[] args() {
            return joinPoint.getArgs();
        }

        @Override
        public String toString() {
            return joinPoint.toString();
        }
    }
}
