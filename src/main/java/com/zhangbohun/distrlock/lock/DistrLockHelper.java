package com.zhangbohun.distrlock.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 可重入分布式锁助手
 * @author zhangbohun
 * Create Date 2019/04/16 14:56
 * Modify Date 2019/04/25 03:07
 */
@Component
public class DistrLockHelper {

    private static Logger logger = LoggerFactory.getLogger(DistrLockHelper.class);

    @Autowired
    private StringRedisTemplate strRedisTemplate;

    //用于区分不同应用
    @Value("${lock.base.prefix:}")
    private String basePrefix;

    @Value("${lock.default.hold.time:15000}")
    private Long defaultHoldTime;

    public DistrLock createDistrLock(String lockName) {
        lockName = basePrefix + lockName;
        return new DistrLock(strRedisTemplate, lockName, defaultHoldTime);
    }
}
