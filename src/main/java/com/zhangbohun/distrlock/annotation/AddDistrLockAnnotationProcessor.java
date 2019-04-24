package com.zhangbohun.distrlock.annotation;

import com.zhangbohun.common.util.DataUtils;
import com.zhangbohun.distrlock.lock.DistrLock;
import com.zhangbohun.distrlock.lock.DistrLockHelper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.SpelCompilerMode;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringValueResolver;

import java.lang.reflect.Method;

/**
 * @author zhangbohun
 * Create Date 2019/04/16 14:56
 * Modify Date 2019/04/23 19:14
 */
@Aspect
@Component
public class AddDistrLockAnnotationProcessor implements ApplicationContextAware, EmbeddedValueResolverAware {

    private static Logger logger = LoggerFactory.getLogger(AddDistrLockAnnotationProcessor.class);

    @Autowired
    private DistrLockHelper distrLockHelper;

    private ParameterNameDiscoverer parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();
    private SpelExpressionParser spelExpressionParser = new SpelExpressionParser(
        new SpelParserConfiguration(SpelCompilerMode.IMMEDIATE, this.getClass().getClassLoader(), true, true, 100));

    private ApplicationContext applicationContext;
    private BeanFactoryResolver beanFactoryResolver;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        this.beanFactoryResolver = new BeanFactoryResolver(applicationContext);
    }

    private StringValueResolver resolver;

    @Override
    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        this.resolver = resolver;
    }

    @Pointcut("@annotation(com.zhangbohun.distrlock.annotation.AddDistrLock)")
    public void lockAspect() {
    }

    @Around("lockAspect()")
    public Object arround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Object object = null;

        Method method = getMethod(proceedingJoinPoint);
        AddDistrLock lockInfo = getLockInfo(proceedingJoinPoint);

        String lockName = lockInfo.lockName();
        if (DataUtils.equals(lockName, "")) {
            lockName = method.getDeclaringClass() + method.getName();
        } else {
            //解析#{}
            String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);
            StandardEvaluationContext context = new StandardEvaluationContext(this.applicationContext);
            for (int i = 0; i < parameterNames.length; i++) {
                context.setVariable(parameterNames[i], proceedingJoinPoint.getArgs()[i]);
            }
            context.setBeanResolver(beanFactoryResolver);
            Expression expr = spelExpressionParser.parseExpression(lockName, new TemplateParserContext());
            lockName = expr.getValue(context, String.class);
            //解析${}
            lockName = resolver.resolveStringValue(lockName);
        }

        long holdTime = lockInfo.holdTime();
        long waitTime = lockInfo.waitTime();
        DistrLock reentrantDistrLock = distrLockHelper.createDistrLock(lockName);
        if (lockInfo.isBlocking()) {
            //阻塞等待
            if (reentrantDistrLock.lock(holdTime, waitTime)) {
                try {
                    //抢占到锁,执行操作
                    object = proceedingJoinPoint.proceed();
                } finally {
                    //释放锁
                    reentrantDistrLock.unlock();
                }
            }
        } else {
            //非阻塞，抢不到就算了
            if (reentrantDistrLock.tryLock(holdTime)) {
                try {
                    //抢占到锁,执行操作
                    object = proceedingJoinPoint.proceed();
                } finally {
                    //释放锁
                    reentrantDistrLock.unlock();
                }
            }
        }
        return object;
    }

    private AddDistrLock getLockInfo(ProceedingJoinPoint proceedingJoinPoint) {
        return getMethod(proceedingJoinPoint).getAnnotation(AddDistrLock.class);
    }

    @Before("lockAspect()")
    public void before(JoinPoint joinPoint) {
        logger.info(joinPoint.getSignature().getName() + " : start.....");
    }

    @After("lockAspect()")
    public void after(JoinPoint joinPoint) {
        logger.info(joinPoint.getSignature().getName() + " : end.....");
    }

    private Method getMethod(ProceedingJoinPoint proceedingJoinPoint) {
        MethodSignature methodSignature = (MethodSignature)proceedingJoinPoint.getSignature();
        Method method = methodSignature.getMethod();
        if (method.getDeclaringClass().isInterface()) {
            try {
                method = proceedingJoinPoint.getTarget().getClass()
                    .getDeclaredMethod(methodSignature.getName(), method.getParameterTypes());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return method;
    }
}
