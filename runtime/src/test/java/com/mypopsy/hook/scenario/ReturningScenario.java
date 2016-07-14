package com.mypopsy.hook.scenario;

import com.mypopsy.hook.HookedMethod;
import com.mypopsy.hook.annotations.Hook;
import com.mypopsy.hook.annotations.Hooked;
import com.mypopsy.hook.annotations.Param;
import com.mypopsy.hook.annotations.Result;
import com.mypopsy.hook.annotations.Returning;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class ReturningScenario extends BaseScenario<Integer> {

    private int token;

    @Returning(value = "test", priority = 0)
    void after0() {
        assertThat(token, is(9));
        token+=8;
    }

    @Returning(value = "test", priority = 1)
    void after1(@Param("int") int i) {
        assertThat(i, is(17));
        assertThat(token, is(4));
        token+=5;
    }

    @Returning(value = "test", priority = 2)
    void after2(@Param("txt") String text, @Result Integer result) {
        assertThat(text, is("foo"));
        assertThat(token, is(1));
        assertThat(result, is(17));
        token+=3;
    }

    @Returning(value = "test", priority = 3)
    void after3(String a, int b) {
        assertThat(a, is("foo"));
        assertThat(b, is(17));
        assertThat(token, is(0));
        token+=1;
    }

    @Hook("test")
    Integer hook(HookedMethod<Integer> method) throws Throwable {
        assertThat(token, is(0));
        return method.proceed("foo", 17);
    }

    @Override
    public Integer result() {
        return call("abc", 42);
    }

    @Hooked("test")
    private Integer call(@Param("txt") String text, @Param("int") int i) {
        assertThat(text, is("foo"));
        assertThat(i, is(17));
        assertThat(token, is(0));
        return i;
    }

    @Override
    public Integer expected() {
        return token;
    }
}
