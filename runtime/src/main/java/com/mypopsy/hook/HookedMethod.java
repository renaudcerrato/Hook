package com.mypopsy.hook;

import com.mypopsy.hook.annotations.Param;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

abstract public class HookedMethod<T> {

    private Map<String, Integer> namedArguments;

    public T proceed() throws Throwable {
        return proceed(args());
    }

    abstract public T proceed(Object ... args) throws Throwable;
    abstract public Method method();
    abstract public Object target();
    abstract public Object[] args();


    int getPosition(Param param) {
        if(namedArguments == null) {
            final Annotation[][] annotations = method().getParameterAnnotations();
            namedArguments = new HashMap<>();
            for(int i = 0; i < annotations.length; i++) {
                for(Annotation annotation: annotations[i]) {
                    if(annotation instanceof Param) {
                        namedArguments.put(((Param) annotation).value(), i);
                        break;
                    }
                }
            }
        }
        final Integer position = namedArguments.get(param.value());
        if(position == null) return -1;
        return position;
    }
}
