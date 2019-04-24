package com.zhangbohun.syncvariable;

import com.zhangbohun.common.util.JSONUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author zhangbohun
 * Create Date 2019/04/17 17:04
 * Modify Date 2019/04/22 02:36
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class SyncVariableTest {

    private static Logger logger = LoggerFactory.getLogger(SyncVariableTest.class);

    @Autowired
    SyncVariableHelper syncVariableHelper;

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testSyncVariable() throws InterruptedException {
        int total = 250;
        SyncVariable<Map> counterMap = syncVariableHelper
            .declareSyncVariable("counterMap", Map.class, new LinkedHashMap());
        CountDownLatch countDownLatch = new CountDownLatch(total);
        for (int i = 0; i < total / 10; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        if ((counterMap.getValue().size() < total)) {
                            if (counterMap.tryOccupy()) {
                                Map temp = counterMap.getValue();
                                temp.put(counterMap.getValue().size() + 1, Thread.currentThread().getId());
                                counterMap.changeLocally(temp);
                                try {
                                    counterMap.commitLocalChange();
                                    countDownLatch.countDown();
                                    Thread.sleep(100);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            break;
                        }
                    }
                }
            }).start();
        }
        countDownLatch.await(50, TimeUnit.MINUTES);
        logger.info(JSONUtils.toJSONString(counterMap.getValue()));
        assert (counterMap.getValue().size() == total);
    }
}