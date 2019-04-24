package com.zhangbohun.distrlock.lock;

import com.zhangbohun.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 基于redis实现可重入分布式锁
 * @author zhangbohun
 * Create Date 2019/04/16 14:56
 * Modify Date 2019/04/20 18:42
 */
public class DistrLock implements AutoCloseable {

    private static Logger logger = LoggerFactory.getLogger(DistrLock.class);

    //用于保证本地线程间逻辑
    private ReentrantLock localLock;

    private StringRedisTemplate strRedisTemplate;

    private static final String LOCK_PREFIX = "DistrLock_";
    private static final String LOCK_SUCCESS = "OK";
    private static final Long RELEASE_SUCCESS = 1L;

    private String lockName;

    private long defaultHoldTime;

    //保证锁由同一把钥匙操作
    private String key = StringUtils.getNewUUIDString();

    public DistrLock(StringRedisTemplate strRedisTemplate, String lockName, Long defaultHoldTime) {
        this.defaultHoldTime = defaultHoldTime;
        this.localLock = new ReentrantLock();
        this.lockName = LOCK_PREFIX + lockName;
        this.strRedisTemplate = strRedisTemplate;
    }

    public boolean isLocked() {
        //本地被锁一定被锁，否则看reids上是否被锁
        return localLock.isLocked() || strRedisTemplate.opsForValue().get(lockName) != null;
    }

    public boolean isHeldByCurrentThread() {
        //是否是当前线程
        return localLock.isHeldByCurrentThread() && isLocked();
    }

    public boolean tryLock() {
        return tryLock(defaultHoldTime);
    }

    public boolean tryLock(Long holdTime) {
        //先保证本地加锁成功
        if (localLock.tryLock()) {
            // 重入判断
            if (localLock.getHoldCount() > 1) {
                return true;
            }
            //判断redis加锁是否成功
            if (tryAcquire(holdTime)) {
                return true;
            } else {
                localLock.unlock();
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean lock() {
        return lock(defaultHoldTime);
    }

    /**
     * block,competitive,Nonfair
     * @param holdTime
     * @return
     */
    public boolean lock(Long holdTime) {
        localLock.lock();
        // 重入判断
        if (localLock.getHoldCount() > 1) {
            return true;
        }
        do {
            if (tryAcquire(holdTime)) {
                return true;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (true);
    }

    public boolean lock(Long holdTime, Long waitTime) {
        do {
            if (tryLock(holdTime)) {
                return true;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while ((waitTime -= 100) > 0);

        return false;
    }

    public boolean unlock() {
        if (localLock.getHoldCount() <= 0) {
            throw new IllegalStateException("this thread does not get lock");
        } else {
            if (localLock.getHoldCount() > 1) {
                localLock.unlock();
                return true;
            } else {
                if (release()) {
                    localLock.unlock();
                    return true;
                } else {
                    return false;
                }
            }
        }
    }

    /**
     * try-with-resources
     * @throws Exception
     */
    @Override
    public void close() throws Exception {
        if (isLocked()) {
            unlock();
        }
    }

    private boolean tryAcquire(Long holdTime) {
        String script
            = "if (redis.call('get', KEYS[1]) == ARGV[1]) then redis.call('expire', KEYS[1], ARGV[2]) return 'OK' else return redis.call('set', KEYS[1], ARGV[1], 'EX', ARGV[2], 'NX') end";
        DefaultRedisScript<String> lockRedisScript = new DefaultRedisScript<>();
        lockRedisScript.setResultType(String.class);
        lockRedisScript.setScriptText(script);
        String result = strRedisTemplate.execute(lockRedisScript, Collections.singletonList(lockName),
            Arrays.asList(key, StringUtils.toString(holdTime / 1000)).toArray());

        return LOCK_SUCCESS.equals(result);
    }

    private boolean release() {
        String script
            = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        DefaultRedisScript<Long> unlockRedisScript = new DefaultRedisScript<>();
        unlockRedisScript.setResultType(Long.class);
        unlockRedisScript.setScriptText(script);
        Long result = strRedisTemplate
            .execute(unlockRedisScript, Collections.singletonList(lockName), Arrays.asList(key).toArray());

        return RELEASE_SUCCESS.equals(result);
    }
}
