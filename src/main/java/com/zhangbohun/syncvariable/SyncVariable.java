package com.zhangbohun.syncvariable;

import com.zhangbohun.common.util.JSONUtils;
import com.zhangbohun.distrlock.lock.DistrLock;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 分布式同步变量
 * @author zhangbohun
 * Create Date 2019/04/16 14:56
 * Modify Date 2019/04/22 02:36
 */
public class SyncVariable<T> {

    private static final String SYNC_VARIABLE_PREFIX = "SyncVariable_";

    private StringRedisTemplate strRedisTemplate;
    private DistrLock distrLock;
    private String name;
    private T value;
    private Class<T> clazz;

    public T getValue() {
        return value;
    }

    //默认每次最长操作时间5分钟 1000 * 60 * 5L
    private long optTimeout;

    public SyncVariable(DistrLock distrLock, StringRedisTemplate strRedisTemplate, String name, Class<T> clazz) {
        this(distrLock, strRedisTemplate, name, clazz, null, 1000 * 60 * 5L);
    }

    public SyncVariable(DistrLock distrLock, StringRedisTemplate strRedisTemplate, String name, Class<T> clazz,
        T defaultValue) {
        this(distrLock, strRedisTemplate, name, clazz, defaultValue, 1000 * 60 * 5L);
    }

    public SyncVariable(DistrLock distrLock, StringRedisTemplate strRedisTemplate, String name, Class<T> clazz,
        Long optTimeout) {
        this(distrLock, strRedisTemplate, name, clazz, null, optTimeout);
    }

    public SyncVariable(DistrLock distrLock, StringRedisTemplate strRedisTemplate, String name, Class<T> clazz,
        T defaultValue, Long optTimeout) {
        this.distrLock = distrLock;
        this.strRedisTemplate = strRedisTemplate;
        this.name = SYNC_VARIABLE_PREFIX + name;
        this.value = defaultValue;
        this.clazz = clazz;
        this.optTimeout = optTimeout;
    }

    public boolean tryOccupy() {
        if (distrLock.lock(optTimeout)) {
            String result = strRedisTemplate.opsForValue().get(name);
            if (value != null) {
                strRedisTemplate.opsForValue().set(name, JSONUtils.toJSONString(value));
            } else if (result != null) {
                changeLocally(JSONUtils.parseObject(result, clazz));
            }
            return true;
        }
        return false;
    }

    public void changeLocally(T value) {
        if (distrLock.isHeldByCurrentThread()) {
            this.value = value;
        }
    }

    public void commitLocalChange() throws TimeoutException {
        if (distrLock.isHeldByCurrentThread()) {
            strRedisTemplate.opsForValue().set(name, JSONUtils.toJSONString(value));
            if (!distrLock.unlock()) {
                throw new TimeoutException("operation timeout");
            }
        }
    }

    public void change(T value) {
        if (distrLock.isHeldByCurrentThread()) {
            strRedisTemplate.opsForValue().set(name, JSONUtils.toJSONString(value));
        }
    }

    public void remove() {
        if (distrLock.isHeldByCurrentThread()) {
            this.value = null;
            strRedisTemplate.delete(name);
        }
    }
}
