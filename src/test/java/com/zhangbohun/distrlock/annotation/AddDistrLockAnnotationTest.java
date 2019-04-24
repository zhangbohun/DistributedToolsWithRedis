package com.zhangbohun.distrlock.annotation;

import com.zhangbohun.common.util.JSONUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author zhangbohun
 * Create Date 2019/04/17 17:04
 * Modify Date 2019/04/21 23:36
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class AddDistrLockAnnotationTest {

    private static Logger logger = LoggerFactory.getLogger(AddDistrLockAnnotationTest.class);

    @Before
    public void setUp() throws Exception {

    }

    @Autowired
    ConterManager conterManager;

    @Test
    public void testAddDistrLockAnnotation1() throws InterruptedException {
        conterManager.resetCounter();
        int total = 100;
        CountDownLatch countDownLatch = new CountDownLatch(total);
        for (int j = 0; j < total; j++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    conterManager.blockCount(total, countDownLatch);
                }
            }).start();
        }

        countDownLatch.await(50, TimeUnit.MINUTES);
        System.err.println(JSONUtils.toJSONString(conterManager.getCounterMap()));

        assert(conterManager.getCounterMap().size()==total);
    }

    @Test
    public void testAddDistrLockAnnotation2() throws InterruptedException {
        conterManager.resetCounter();
        int total = 1000;
        CountDownLatch countDownLatch = new CountDownLatch(total);
        for (int j = 0; j < total/5; j++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        if ((conterManager.getCounterMap().size() < total)) {
                            conterManager.unblockCount(total, countDownLatch);
                        } else {
                            break;
                        }
                    }
                }
            }).start();
        }

        countDownLatch.await(50, TimeUnit.MINUTES);
        logger.info(JSONUtils.toJSONString(conterManager.getCounterMap()));
        logger.info(conterManager.getCounterMap().size()+"");

        assert(conterManager.getCounterMap().size()==total);
    }
}