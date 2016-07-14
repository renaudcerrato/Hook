package com.mypopsy.hook;

import com.mypopsy.hook.scenario.AfterScenario;
import com.mypopsy.hook.scenario.BaseScenario;
import com.mypopsy.hook.scenario.BeforeAfterScenario;
import com.mypopsy.hook.scenario.BeforeScenario;
import com.mypopsy.hook.scenario.CallScenario;
import com.mypopsy.hook.scenario.DoNotProceedScenario;
import com.mypopsy.hook.scenario.ModifyArgsScenario;
import com.mypopsy.hook.scenario.OrderingScenario;
import com.mypopsy.hook.scenario.ProceedScenario;
import com.mypopsy.hook.scenario.ProceedWithArgsScenario;
import com.mypopsy.hook.scenario.ReturningScenario;
import com.mypopsy.hook.scenario.SimpleParamScenario;
import com.mypopsy.hook.scenario.SimpleScenario;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(Parameterized.class)
public class TestHook {

    private final BaseScenario data;

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> configs() {
        return Arrays.asList(new Object[][] {
                {new SimpleScenario()},
                {new ProceedScenario()},
                {new ProceedWithArgsScenario()},
                {new DoNotProceedScenario()},
                {new OrderingScenario()},
                {new ModifyArgsScenario()},
                {new SimpleParamScenario()},
                {new BeforeScenario()},
                {new CallScenario()},
                {new AfterScenario()},
                {new ReturningScenario()},
                {new BeforeAfterScenario()},
        });
    }

    public TestHook(BaseScenario data) {
        this.data = data;
    }

    @Test
    public void test() {
        try {
            data.register();
            assertThat(data.result(), is(data.expected()));
        }finally {
            data.unregister();
        }
    }
}

