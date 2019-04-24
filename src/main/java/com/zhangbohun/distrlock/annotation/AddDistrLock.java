package com.zhangbohun.distrlock.annotation;

import java.lang.annotation.*;

/**
 * 分布式单例锁注解
 * @author zhangbohun
 * Create Date 2019/04/16 14:56
 * Modify Date 2019/04/20 18:17
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface AddDistrLock {
    /**
     * 分布式锁的标识名称，默认为类名+方法名，支持类似@Value的SpEL#{}表达式还可以通过${}获取配置
     */
    String lockName() default "";

    /**
     * 锁最大持有时间
     * @return
     */
    long holdTime() default 1000 * 15;

    /**
     * 是否阻塞
     * @return
     */
    boolean isBlocking() default false;

    /**
     * 阻塞超时时间
     * @return
     */
    long waitTime() default 1000 * 20;
}