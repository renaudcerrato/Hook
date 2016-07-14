# Hook

Minimalist, annotation based, hook framework for Java/Android built on top of [AspectJ](https://eclipse.org/aspectj/).

# Basic Usage

```java

public class Math {

    @Hooked("my_hook")
    public int add(int a,int b) {
        return a+b;
    }
}

public class IncrementHook {

    @Hook("my_hook")
    int hook(HookedMethod<Integer> method) throws Throwable {
        return method.proceed() + 1; // add one 
    }
}
    
final Math math = new Math();
final IncrementHook hook = new IncrementHook();

Hooker.instance().register(hook);
assertThat(math.add(5, 3), is(9)); // 5 + 3 = 9
Hooker.instance().unregister(hook);
assertThat(math.add(5, 3), is(8)); // 5 + 3 = 8    

```

# Advanced Usage

```java

public class Math {

    @Hooked("my_hook")
    public int add(@Param("a") int a, @Param("b") int b) {
        System.out.println("    calling add("+a+", "+b+")");
        return a+b;
    }
}

public class Hooks {

	@Before("my_hook")
    void before(int a, int b) {
        System.out.println("  @Before(a="+a+", b="+b+")");
    }

    @Call("my_hook")
    void call(int a, int b) {
        System.out.println("@Call(a="+a+", b="+b+")");
    }

    @After("my_hook")
    void after(@Param("a") int a, @Param("b") int b, @Target Math math, @Result int result) {
        System.out.println("  @After(a="+a+", b="+b+", result="+result+")");
    }

    @Returning("my_hook")
    void returning(@Param("a") int a, @Param("b") int b, @Result int result) {
        System.out.println("@Returning(a="+a+", b="+b+", result="+result+")");
    }

    @Hook("my_hook")
    int hook(HookedMethod<Integer> method, @Param("a") int a, @Param("b") int b, @Target Math math) throws Throwable {
        System.out.println("entering @Hook(a="+a+", b="+b+")");
        int ret = method.proceed(2*a, 2*b) + 1; // double operands and increment result
        System.out.println("exiting @Hook (return "+ret+")");
        return ret;
    }
}

final Math math = new Math();
final Hooks hook = new Hooks();

Hooker.instance().register(hook);
assertThat(math.add(5, 3), is(17));
Hooker.instance().unregister(hook);
```

For clarity, here's the output of the snippet above :

```
@Call(a=5, b=3)
entering @Hook(a=5, b=3)
  @Before(a=10, b=6)
    calling add(10, 6)
  @After(a=10, b=6, result=16)
exiting @Hook (return 17)
@Returning(a=10, b=6, result=17)
```

# Installation

## Android Studio (Gradle)
TODO
 




