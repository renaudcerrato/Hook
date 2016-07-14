# Hook

Minimalist, annotation based, hook framework for Android built on top of [AspectJ](https://eclipse.org/aspectj/).

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

# Available Annotations

## @Hooked

In order to be able to hook a method, you must annotate them as `@Hooked`, and give them an **unique** name. If you defines more than one `@Hooked` method with the same name, _reaaaally bad things can happen_. 


## @Hook

By using `@Hook` annotations, you're given a chance to alter arguments and/or the return value of the original hooked method. `@Hook` annotated methods have a mandatory first arguments of type [`HookedMethod`](https://github.com/renaudcerrato/Hook/blob/master/runtime/src/main/java/com/mypopsy/hook/HookedMethod.java).

You can capture arguments by using `@Param` annotations on them, and you can capture the object on which the method is executing using the `@Target` annotation:

```
	@Hook("my_hook")
    int hook(HookedMethod<Integer> method, @Param("a") int a, @Param("b") int b, @Target Math math) throws Throwable {
        return method.proceed();
    }
```


## @Call

If you don't mind altering arguments or the return value, you can use `@Call` annotations. Since `@Call` methods will be invoked first (i.e before `@Hook` annotated method(s)), you can be sure that arguments have not been altered.

## @Before

`@Before` annotated methods are called right **before** the original method is called - that mean that both `@Call` and `@Hook` method(s) were called already, possibly altering arguments. Of course, `@Before` methods are **not called** if a single `@Hook` method didn't called `HookedMethod::proceed()`.

## @After

`@After` annotated methods are called right **after** the original method is called - but before `@Hook` annotated methods returns. Of course, `@After` methods are **not called** if a single `@Hook` method didn't called `HookedMethod::proceed()`.

You can capture the return value using the `@Result` annotation.

* `@Returning`

`@Returning` annotated methods are call right **before** returning to the original caller. 
You can capture the return value using the `@Result` annotation.

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
final Hooks hooks = new Hooks();

Hooker.instance().register(hooks);
assertThat(math.add(5, 3), is(17));
Hooker.instance().unregister(hooks);
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


# Android Studio

The project binaries are hosted on [JitPack](https://jitpack.io): the Android integration is made easy thanks to the use of a custom gradle plugin.

**Step 1** Import the plugin in your root build.gradle at the `buildscript` closure:

```
buildscript {
    repositories {
        ...
        maven { url "https://jitpack.io" }
    }
    dependencies {
        ...
        classpath 'com.github.renaudcerrato.Hook:plugin:1.0.0'
    }
}
```

**Step 2** Add it in your root build.gradle at the end of repositories:
```
allprojects {
	repositories {
		...
		maven { url "https://jitpack.io" }
	}
}
```

**Step 3** Apply the plugin in your app build.gradle:

```
apply plugin: 'com.android.application'
apply plugin: 'hook'
...
```




