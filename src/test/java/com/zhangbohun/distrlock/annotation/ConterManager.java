package com.zhangbohun.distrlock.annotation;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

@Component
public class ConterManager {

    public int testInt = 0;

    public int counter = 0;
    public Map counterMap = new LinkedHashMap();

    public int getCounter() {
        return counter;
    }

    public void resetCounter() {
        counter = 0;
        counterMap = new LinkedHashMap();
    }

    public Map getCounterMap() {
        return counterMap;
    }

    @AddDistrLock(lockName = "test#{@conterManager.testInt+''+#total}#{'${spring.redis.port}'}", isBlocking = true)
    public void blockCount(int total, CountDownLatch countDownLatch) {
        counter += 1;
        counterMap.put(counter, Thread.currentThread().getId());
        countDownLatch.countDown();
    }

    @AddDistrLock(lockName = "test#{@conterManager.testInt+''+#total}#{'${spring.redis.port}'}")
    public void unblockCount(int total, CountDownLatch countDownLatch) {
        if (counter < total) {
            counter += 1;
            counterMap.put(counter, Thread.currentThread().getId());
            countDownLatch.countDown();
        }
    }
}
