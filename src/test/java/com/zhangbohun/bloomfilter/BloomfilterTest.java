package com.zhangbohun.bloomfilter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BloomfilterTest {

    private static Logger logger = LoggerFactory.getLogger(BloomfilterTest.class);

    @Autowired
    DistrBloomFilterHelper distrBloomFilterHelper;

    private int itemCount = 50000;
    private int hashCount = 5;

    public void printStat(long start, long end, double accuracy) {
        double diff = (end - start) / 1000.0;
        System.out.println("耗时" + diff + "s,速度" + (itemCount / diff) + " elements/s，正确率 " + accuracy * 100 + "%");
    }

    @Test
    public void benchmarkTest() {

        //生成添加测试数据
        List<String> existingElements = new ArrayList(itemCount);
        for (int i = 0; i < itemCount; i++) {
            existingElements.add(String.valueOf(i) + "添加测试数");
        }
        //生成不存在判断测试数据
        List<String> nonExistingElements = new ArrayList(itemCount);
        for (int i = itemCount; i < itemCount * 2; i++) {
            nonExistingElements.add(String.valueOf(i) + "不存在判断测试数据");
        }

        BloomFilter bf =  distrBloomFilterHelper.createDistrBloomFilter("testbitset", false,itemCount * 2 * hashCount, hashCount);
        bf.clear();

        System.out.println("  元素个数：" + itemCount);
        System.out.println("  哈希次数：" + hashCount);

        System.out.print("  添加测试: ");
        long start_add = System.currentTimeMillis();
        long addCorrectCount = 0;
        for (int i = 0; i < itemCount; i++) {
            if (bf.add(existingElements.get(i))) {
                addCorrectCount++;
            }
            ;
        }
        long end_add = System.currentTimeMillis();
        printStat(start_add, end_add, addCorrectCount / (double)itemCount);

        System.out.print("  存在判断: ");
        long start_contains = System.currentTimeMillis();
        long containCorrectCount = 0;
        for (int i = 0; i < itemCount; i++) {
            if (bf.contains(existingElements.get(i))) {
                containCorrectCount++;
            }
        }
        long end_contains = System.currentTimeMillis();
        printStat(start_contains, end_contains, containCorrectCount / (double)itemCount);

        System.out.print("不存在判断: ");
        long start_ncontains = System.currentTimeMillis();
        long notcontainCorrectCount = 0;
        for (int i = 0; i < itemCount; i++) {
            if (!bf.contains(nonExistingElements.get(i))) {
                notcontainCorrectCount++;
            }
        }
        long end_ncontains = System.currentTimeMillis();
        printStat(start_ncontains, end_ncontains, notcontainCorrectCount / (double)itemCount);
    }
}
