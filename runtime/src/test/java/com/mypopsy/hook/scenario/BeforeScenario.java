package com.mypopsy.hook.scenario;

import com.mypopsy.hook.HookedMethod;
import com.mypopsy.hook.annotations.Before;
import com.mypopsy.hook.annotations.Hook;
import com.mypopsy.hook.annotations.Hooked;
import com.mypopsy.hook.annotations.Param;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class BeforeScenario extends BaseScenario<Integer> {

    private int token;

    @Before(value = "test", priority = 0)
    void before0() {
        assertThat(token, is(9));
        token+=8;
    }

    @Before(value = "test", priority = 1)
    void before1(@Param("int") int i) {
        assertThat(i, is(666));
        assertThat(token, is(4));
        token+=5;
    }

    @Before(value = "test", priority = 2)
    void before2(@Param("txt") String text) {
        assertThat(text, is("foo"));
        assertThat(token, is(1));
        token+=3;
    }

    @Before(value = "test", priority = 3)
    void before3(String a, int b) {
        assertThat(a, is("foo"));
        assertThat(b, is(666));
        assertThat(token, is(0));
        token = 1;
    }

    @Hook("test")
    Integer hook(HookedMethod<Integer> method) throws Throwable {
        assertThat(token, is(0));
        return method.proceed("foo", 666);
    }

    @Override
    public Integer result() {
        return call("abc", 42);
    }

    @Hooked("test")
    private Integer call(@Param("txt") String text, @Param("int") int foo) {
        return token;
    }

    @Override
    public Integer expected() {
        return 17;
    }
}
