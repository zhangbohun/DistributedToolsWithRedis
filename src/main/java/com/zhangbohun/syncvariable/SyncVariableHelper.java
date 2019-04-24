package com.zhangbohun.syncvariable;

import com.zhangbohun.distrlock.lock.DistrLockHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 同步变量助手
 * @author zhangbohun
 * Create Date 2019/04/16 14:56
 * Modify Date 2019/04/25 02:28
 */
@Component
public class SyncVariableHelper {

    private static Logger logger = LoggerFactory.getLogger(SyncVariableHelper.class);

    @Autowired
    StringRedisTemplate strRedisTemplate;
    @Autowired
    DistrLockHelper distrLockHelper;

    //用于区分不同应用
    @Value("${redis.distributed.tools.base.prefix:}")
    private String basePrefix;

    public <T> SyncVariable declareSyncVariable(String variableName, Class<T> clazz) {
        variableName = basePrefix + variableName;
        return new SyncVariable(distrLockHelper.createDistrLock(variableName), strRedisTemplate, variableName, clazz);
    }

    public <T> SyncVariable declareSyncVariable(String variableName, Class<T> clazz, T defaultValue) {
        variableName = basePrefix + variableName;
        return new SyncVariable(distrLockHelper.createDistrLock(variableName), strRedisTemplate, variableName, clazz,
            defaultValue);
    }

    public <T> SyncVariable declareSyncVariable(String variableName, Class clazz, Long optTimeout) {
        variableName = basePrefix + variableName;
        return new SyncVariable(distrLockHelper.createDistrLock(variableName), strRedisTemplate, variableName, clazz,
            optTimeout);
    }

    public <T> SyncVariable declareSyncVariable(String variableName, Class clazz, T defaultValue, Long optTimeout) {
        variableName = basePrefix + variableName;
        return new SyncVariable(distrLockHelper.createDistrLock(variableName), strRedisTemplate, variableName, clazz,
            defaultValue, optTimeout);
    }
}
