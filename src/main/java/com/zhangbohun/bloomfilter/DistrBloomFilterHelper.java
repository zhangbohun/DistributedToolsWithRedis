package com.zhangbohun.bloomfilter;

import com.zhangbohun.distrlock.lock.DistrLockHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 分布式布隆过滤器（）
 * @author zhangbohun
 * Create Date 2019/04/16 14:56
 * Modify Date 2019/04/25 02:44
 */
@Component
public class DistrBloomFilterHelper {

    private static Logger logger = LoggerFactory.getLogger(DistrBloomFilterHelper.class);

    @Autowired
    private StringRedisTemplate strRedisTemplate;

    @Autowired
    DistrLockHelper distrLockHelper;

    //用于区分不同应用
    @Value("${redis.distributed.tools.base.prefix:}")
    private String basePrefix;

    public DistrBloomFilter createDistrBloomFilter(String bitSetName, boolean sync, int size, int hashCount) {
        bitSetName = basePrefix + bitSetName;
        if (sync) {
            return new DistrBloomFilter(strRedisTemplate, distrLockHelper.createDistrLock(bitSetName), bitSetName, size,
                hashCount);
        } else {
            return new DistrBloomFilter(strRedisTemplate, null, bitSetName, size, hashCount);
        }
    }
}
