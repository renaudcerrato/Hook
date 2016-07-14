package com.mypopsy.hook;

import com.mypopsy.hook.annotations.After;
import com.mypopsy.hook.annotations.Before;
import com.mypopsy.hook.annotations.Call;
import com.mypopsy.hook.annotations.Hook;
import com.mypopsy.hook.annotations.Hooked;
import com.mypopsy.hook.annotations.Param;
import com.mypopsy.hook.annotations.Result;
import com.mypopsy.hook.annotations.Returning;
import com.mypopsy.hook.annotations.Target;
import com.mypopsy.hook.internal.OrderedLinkedList;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Hooker {

    private static Hooker sInstance;

    private final Map<String, LL<LL.HookEntry>> hooks =
            Collections.synchronizedMap(new HashMap<String, LL<LL.HookEntry>>());

    private final Map<String, LL<LL.BeforeEntry>> befores =
            Collections.synchronizedMap(new HashMap<String, LL<LL.BeforeEntry>>());

    private final Map<String, LL<LL.BeforeEntry>> calls =
            Collections.synchronizedMap(new HashMap<String, LL<LL.BeforeEntry>>());

    private final Map<String, LL<LL.AfterEntry>> afters =
            Collections.synchronizedMap(new HashMap<String, LL<LL.AfterEntry>>());

    private final Map<String, LL<LL.AfterEntry>> returnings =
            Collections.synchronizedMap(new HashMap<String, LL<LL.AfterEntry>>());

    private final HashMap<Object, Set<OrderedLinkedList.Node>> subscribers = new HashMap<>();


    public static Hooker instance() {
        if(sInstance == null) sInstance = new Hooker();
        return sInstance;
    }

    private Hooker() {}

    public synchronized void register(Object subscriber) {
        if(isRegistered(subscriber)) return;

        Class<?> klass = subscriber.getClass();
        while (klass != Object.class)
        {
            final List<Method> allMethods = new ArrayList<>(Arrays.asList(klass.getDeclaredMethods()));
            for (final Method method : allMethods) {
                if(method.isBridge() || method.isSynthetic()) {
                    continue;
                }
                if (method.isAnnotationPresent(Hook.class)) {
                    register(subscriber, method, method.getAnnotation(Hook.class));
                }else if (method.isAnnotationPresent(Before.class)) {
                    register(subscriber, method, method.getAnnotation(Before.class));
                }else if (method.isAnnotationPresent(After.class)) {
                    register(subscriber, method, method.getAnnotation(After.class));
                }else if (method.isAnnotationPresent(Call.class)) {
                    register(subscriber, method, method.getAnnotation(Call.class));
                }else if (method.isAnnotationPresent(Returning.class)) {
                    register(subscriber, method, method.getAnnotation(Returning.class));
                }
            }
            klass = klass.getSuperclass();
        }
    }

    public boolean isRegistered(Object object) {
        return subscribers.containsKey(object);
    }

    public synchronized void unregister(Object subscriber) {
        final Set<OrderedLinkedList.Node> entries = subscribers.remove(subscriber);
        if(entries == null) return;
        for(OrderedLinkedList.Node entry : entries) {
            entry.remove();
        }
    }

    private void register(Object subscriber, Method method, Hook annotation) {
        if(annotation == null) return;

        final String name = annotation.value();
        LL<LL.HookEntry> nodes = hooks.get(name);

        if(nodes == null) {
            hooks.put(name, nodes = new LL<>());
        }

        add(subscriber,  nodes.insert(nodes.new HookEntry(subscriber, method, annotation.priority())));
    }

    private void register(Object subscriber, Method method, Before annotation) {
        if(annotation == null) return;

        final String name = annotation.value();
        LL<LL.BeforeEntry> nodes = befores.get(name);

        if(nodes == null) {
            befores.put(name, nodes = new LL<>());
        }

        add(subscriber,  nodes.insert(nodes.new BeforeEntry(subscriber, method, annotation.priority())));
    }

    private void register(Object subscriber, Method method, Call annotation) {
        if(annotation == null) return;

        final String name = annotation.value();
        LL<LL.BeforeEntry> nodes = calls.get(name);

        if(nodes == null) {
            calls.put(name, nodes = new LL<>());
        }

        add(subscriber,  nodes.insert(nodes.new BeforeEntry(subscriber, method, annotation.priority())));
    }

    private void register(Object subscriber, Method method, After annotation) {
        if(annotation == null) return;

        final String name = annotation.value();
        LL<LL.AfterEntry> nodes = afters.get(name);

        if(nodes == null) {
            afters.put(name, nodes = new LL<>());
        }

        add(subscriber,  nodes.insert(nodes.new AfterEntry(subscriber, method, annotation.priority())));
    }

    private void register(Object subscriber, Method method, Returning annotation) {
        if(annotation == null) return;

        final String name = annotation.value();
        LL<LL.AfterEntry> nodes = returnings.get(name);

        if(nodes == null) {
            returnings.put(name, nodes = new LL<>());
        }

        add(subscriber,  nodes.insert(nodes.new AfterEntry(subscriber, method, annotation.priority())));
    }

    private void add(Object subscriber, OrderedLinkedList.Node entry) {
        Set<OrderedLinkedList.Node> set = subscribers.get(subscriber);

        if(set == null) {
            subscribers.put(subscriber, set = new HashSet<>());
        }

        set.add(entry);
    }

    /*package*/ Object proceed(HookedMethod root) throws Throwable {
        return new AroundHookedMethod(new BeforeHookedMethod(new AfterHookedMethod(root))).proceed();
    }

    private class AroundHookedMethod extends HookedMethodDelegate {

        AroundHookedMethod(HookedMethod instance) {
            super(instance);
        }

        @Override
        public Object proceed() throws Throwable {
            return proceed(args());
        }

        @Override
        public Object proceed(Object... args) throws Throwable {
            final Hooked hook = method().getAnnotation(Hooked.class);
            notifyCall(hook, args);

            final LL<LL.HookEntry> nodes = hooks.get(hook.value());
            if(nodes != null) {
                final LL.HookEntry first = nodes.first();
                if(first != null) {
                    final Object ret = first.proceed(delegate(), args);
                    notifyReturning(hook, args, ret);
                    return ret;
                }
            }

            final Object ret = super.proceed(args);
            notifyReturning(hook, args, ret);
            return ret;
        }

        private void notifyCall(Hooked annotation, Object[] args) throws Throwable {
            final LL<LL.BeforeEntry> nodes = calls.get(annotation.value());
            if(nodes == null) return;
            for(LL.BeforeEntry entry: nodes) {
                entry.invoke(delegate(), args);
            }
        }

        private void notifyReturning(Hooked annotation, Object[] args, Object ret) throws Throwable {
            final LL<LL.AfterEntry> nodes = returnings.get(annotation.value());
            if(nodes == null) return;
            for(LL.AfterEntry entry: nodes) {
                entry.invoke(delegate(), args, ret);
            }
        }
    }

    private class BeforeHookedMethod extends HookedMethodDelegate {

        BeforeHookedMethod(HookedMethod instance) {
            super(instance);
        }

        @Override
        public Object proceed() throws Throwable {
            return proceed(args());
        }

        @Override
        public Object proceed(Object... args) throws Throwable {
            final Hooked annotation = method().getAnnotation(Hooked.class);
            final LL<LL.BeforeEntry> nodes = befores.get(annotation.value());
            if(nodes != null) {
                for(LL.BeforeEntry entry: nodes) {
                    entry.invoke(delegate(), args);
                }
            }
            return super.proceed(args);
        }
    }

    private class AfterHookedMethod extends HookedMethodDelegate {

        AfterHookedMethod(HookedMethod instance) {
            super(instance);
        }

        @Override
        public Object proceed() throws Throwable {
            return proceed(args());
        }

        @Override
        public Object proceed(Object... args) throws Throwable {
            final Hooked annotation = method().getAnnotation(Hooked.class);
            final LL<LL.AfterEntry> nodes = afters.get(annotation.value());
            final Object ret = super.proceed(args);
            if(nodes != null) {
                for(LL.AfterEntry entry : nodes) {
                    entry.invoke(delegate(), args, ret);
                }
            }
            return ret;
        }
    }

    @SuppressWarnings("unchecked")
    private static class LL<T extends LL.Entry> extends OrderedLinkedList<T> {

        abstract class Entry extends Node {

            final Object target;
            final Method method;
            final int priority;

            final Annotation annotations[];

            protected Entry(Object target, Method method, int priority) {
                this.target = target;
                this.method = method;
                this.priority = priority;

                final Annotation[][] a = method.getParameterAnnotations();
                annotations = new Annotation[a.length];

                for(int i = 0; i < annotations.length; i++) {
                    for(Annotation an: a[i]) {
                        if(isSupported(an, i)) {
                            annotations[i] = an;
                            break;
                        }
                    }
                }
            }

            protected abstract boolean isSupported(Annotation annotation, int position);

            Object invoke(Object[] args) throws InvocationTargetException, IllegalAccessException {

                if(!method.isAccessible()) {
                    method.setAccessible(true);
                }

                if(args == null || args.length == 0)
                    return method.invoke(target);
                else
                    return method.invoke(target, args);
            }

            @Override
            public int compareTo(Node other) {
                return priority - ((Entry)other).priority;
            }
        }

        class HookEntry extends Entry {

            HookEntry(Object target, Method method, int priority) {
                super(target, method, priority);

                final Class<?>[] types = method.getParameterTypes();
                if(types.length == 0 || !HookedMethod.class.isAssignableFrom(types[0])) {
                    throw new IllegalArgumentException(method+": first argument must be of type "+HookedMethod.class);
                }
            }

            @Override
            protected boolean isSupported(Annotation annotation, int position) {
                if(position > 1) {
                    final Annotation previous = annotations[position - 1];
                    if (annotation == null && previous != null || annotation != null && previous == null)
                        throw new IllegalArgumentException(method +
                                ": missing annotation on argument " + position +
                                " (either none or all optional arguments must be annotated)");
                }

                if(annotation instanceof Result)
                    throw new IllegalArgumentException(Result.class+" annotation can only be used"
                            +" on methods annotated with @" + After.class + " or @" + Returning.class);

                return annotation instanceof Param || annotation instanceof Target;
            }

            Object proceed(final HookedMethod hooked, Object[] args) throws Throwable {
                final Object[] invokeArgs = new Object[annotations.length];
                invokeArgs[0] = new ChainedHookedMethod(hooked, args);

                for(int i = 1; i < invokeArgs.length; i++) {

                    if(annotations[1] == null) {
                        invokeArgs[i] = args[i - 1];
                    }else {
                        final Annotation annotation = annotations[i];
                        if(annotation instanceof Target)
                            invokeArgs[i] = target;
                        else{
                            final int pos = hooked.getPosition((Param) annotation);

                            if (pos == -1) {
                                throw new IllegalStateException("can't find " + annotation + " on " + hooked);
                            }

                            invokeArgs[i] = args[pos];
                        }
                    }
                }

                return invoke(invokeArgs);
            }

            class ChainedHookedMethod extends HookedMethodDelegate {

                private final Object[] args;

                public ChainedHookedMethod(HookedMethod original, Object[] args) {
                    super(original);
                    this.args = args;
                }

                @Override
                public Object proceed() throws Throwable {
                    return proceed(args());
                }

                @Override
                public Object proceed(Object... newArgs) throws Throwable {

                    if(newArgs != null) {
                        System.arraycopy(newArgs, 0, args, 0, Math.min(newArgs.length, args.length));
                    }

                    final HookEntry n = (HookEntry) next;
                    if (n != null) {
                        return n.proceed(delegate(), args);
                    }

                    return super.proceed(args);
                }

                @Override
                public Object[] args() {
                    return args;
                }
            }
        }

        class BeforeEntry extends Entry {


            protected BeforeEntry(Object target, Method method, int priority) {
                super(target, method, priority);

                if(method.getReturnType() != void.class)
                    throw new IllegalArgumentException(method+" must return void");
            }

            @Override
            protected boolean isSupported(Annotation annotation, int position) {
                if(position > 0) {
                    final Annotation previous = annotations[position - 1];
                    if (annotation == null && previous != null || annotation != null && previous == null)
                        throw new IllegalArgumentException(method +
                                ": missing annotation on argument " + position +
                                " (either none or all arguments must be annotated)");
                }

                if(annotation instanceof Result)
                    throw new IllegalArgumentException(Result.class+" annotation can only be used"
                            +" on methods annotated with @" + After.class + " or @" + Returning.class);

                return annotation instanceof Param || annotation instanceof Target;
            }

            void invoke(HookedMethod hooked, Object[] args) throws Throwable {

                if(annotations.length == 0) {
                    // no arguments?
                    invoke(null);
                }else if(annotations[0] == null) {
                    // no @Params annotation? assume same signature
                    invoke(args);
                }else {
                    final Object[] invokeArgs = new Object[annotations.length];

                    for (int i = 0; i < invokeArgs.length; i++) {
                        final Annotation annotation = annotations[i];

                        if(annotation instanceof Target)
                            invokeArgs[i] = target;
                        else{
                            final int pos = hooked.getPosition((Param) annotation);

                            if (pos == -1) {
                                throw new IllegalStateException("can't find " + annotation + " on " + hooked);
                            }

                            invokeArgs[i] = args[pos];
                        }
                    }

                    invoke(invokeArgs);
                }
            }
        }

        class AfterEntry extends Entry {

            protected AfterEntry(Object target, Method method, int priority) {
                super(target, method, priority);

                if(method.getReturnType() != void.class)
                    throw new IllegalArgumentException(method+" must return void");
            }

            @Override
            protected boolean isSupported(Annotation annotation, int position) {
                if(position > 0) {
                    final Annotation previous = annotations[position - 1];
                    if (annotation == null && previous != null || annotation != null && previous == null)
                        throw new IllegalArgumentException(method +
                                ": missing annotation on argument " + position +
                                " (either none or all arguments must be annotated)");
                }
                return annotation instanceof Param || annotation instanceof Target || annotation instanceof Result;
            }

            void invoke(HookedMethod hooked, Object[] args, Object result) throws InvocationTargetException, IllegalAccessException {
                if(annotations.length == 0) {
                    // no arguments?
                    invoke(null);
                }else if(annotations[0] == null) {
                    // no annotations? assume same signature
                    invoke(args);
                }else {
                    final Object[] invokeArgs = new Object[annotations.length];

                    for (int i = 0; i < invokeArgs.length; i++) {
                        final Annotation annotation = annotations[i];

                        if(annotation instanceof Target)
                            invokeArgs[i] = target;
                        else if(annotation instanceof Result)
                            invokeArgs[i] = result;
                        else{
                            final int pos = hooked.getPosition((Param) annotation);

                            if (pos == -1) {
                                throw new IllegalStateException("can't find " + annotation + " on " + hooked);
                            }

                            invokeArgs[i] = args[pos];
                        }
                    }

                    invoke(invokeArgs);
                }
            }
        }
    }
}
