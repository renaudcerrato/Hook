package com.mypopsy.hook.scenario;

import com.mypopsy.hook.HookedMethod;
import com.mypopsy.hook.annotations.After;
import com.mypopsy.hook.annotations.Hook;
import com.mypopsy.hook.annotations.Hooked;
import com.mypopsy.hook.annotations.Param;
import com.mypopsy.hook.annotations.Result;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class AfterScenario extends BaseScenario<Integer> {

    private int token;

    @After(value = "test", priority = 0)
    void after0() {
        assertThat(token, is(9));
        token+=8;
    }

    @After(value = "test", priority = 1)
    void after1(@Param("int") int i) {
        assertThat(i, is(666));
        assertThat(token, is(4));
        token+=5;
    }

    @After(value = "test", priority = 2)
    void after2(@Param("txt") String text, @Result Integer result) {
        assertThat(text, is("foo"));
        assertThat(token, is(1));
        assertThat(result, is(17));
        token+=3;
    }

    @After(value = "test", priority = 3)
    void after3(String a, int b) {
        assertThat(a, is("foo"));
        assertThat(b, is(666));
        assertThat(token, is(0));
        token+=1;
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
        assertThat(token, is(0));
        return 17;
    }

    @Override
    public Integer expected() {
        return token;
    }
}
