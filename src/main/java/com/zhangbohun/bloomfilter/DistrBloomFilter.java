package com.zhangbohun.bloomfilter;

import com.zhangbohun.distrlock.lock.DistrLock;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * @author zhangbohun
 * Create Date 2019/04/25 01:36
 * Modify Date 2019/04/25 03:02
 */
public class DistrBloomFilter implements BloomFilter {
    protected String bitsetName;
    protected int hashCount;
    private StringRedisTemplate strRedisTemplate;
    private DistrLock distrLock;

    private int size;

    /**
     * hashCount用于保证正确率
     * 最优容量范围m= ln2(约0.7)*size/hashCount，size约为m*hashCount*2
     * @param size
     * @param hashCount
     * @throws Exception
     */
    public DistrBloomFilter(StringRedisTemplate strRedisTemplate, DistrLock distrLock, String bitsetName, int size,
        int hashCount) {
        this.strRedisTemplate = strRedisTemplate;
        this.distrLock = distrLock;
        this.bitsetName = bitsetName;
        this.size = size;
        this.hashCount = hashCount;
    }

    @Override
    public long size() {
        return size;
    }

    @Override
    public void clear() {
        if (this.distrLock != null) {
            this.distrLock.lock();
        }
        strRedisTemplate.delete(this.bitsetName);
        if (this.distrLock != null) {
            this.distrLock.unlock();
        }
    }

    @Override
    public boolean contains(Object item) {
        return containsAll(Collections.singletonList(item));
    }

    @Override
    public boolean containsAll(Collection elements) {
        if (this.distrLock != null) {
            this.distrLock.lock();
        }
        for (Object element : elements) {
            for (int i = 0; i < this.hashCount; i++) {
                int index = Math.abs(xxHash32.hash(element.toString().getBytes(), i) % this.size);
                if (!strRedisTemplate.opsForValue().getBit(this.bitsetName, index))//如果有一维是false说明不存在，返回false
                {
                    if (this.distrLock != null) {
                        this.distrLock.unlock();
                    }
                    return false;
                }
            }
        }
        if (this.distrLock != null) {
            this.distrLock.unlock();
        }
        return true;
    }

    @Override
    public boolean containsAll(Object[] elements) {
        return containsAll(Arrays.asList(elements));
    }

    @Override
    public boolean add(Object element) {
        if (this.distrLock != null) {
            this.distrLock.lock();
        }
        for (int i = 0; i < this.hashCount; i++) {
            int index = Math.abs(xxHash32.hash(element.toString().getBytes(), i) % this.size);
            if (!strRedisTemplate.opsForValue().getBit(this.bitsetName, index))// 如果有一维是false说明之前不存在，添加新记录并返回true
            {
                for (int j = 0; j < this.hashCount; j++) {
                    int index1 = Math.abs(xxHash32.hash(element.toString().getBytes(), j) % this.size);
                    strRedisTemplate.opsForValue().setBit(this.bitsetName, index1, true);
                }
                if (this.distrLock != null) {
                    this.distrLock.unlock();
                }
                return true;
            }
        }

        if (this.distrLock != null) {
            this.distrLock.unlock();
        }
        return false;
    }

    @Override
    public void addAll(Collection elements) {
        if (this.distrLock != null) {
            this.distrLock.lock();
        }
        for (Object element : elements) {
            for (int i = 0; i < this.hashCount; i++) {
                int index = Math.abs(xxHash32.hash(element.toString().getBytes(), i) % this.size);
                if (!strRedisTemplate.opsForValue().getBit(this.bitsetName, index))// 如果有一维是false说明之前不存在，添加新记录并返回true
                {
                    for (int j = 0; j < this.hashCount; j++) {
                        int index1 = Math.abs(xxHash32.hash(element.toString().getBytes(), j) % this.size);
                        strRedisTemplate.opsForValue().setBit(this.bitsetName, index1, true);
                    }
                    if (this.distrLock != null) {
                        this.distrLock.unlock();
                    }
                    return;
                }
            }
        }
        if (this.distrLock != null) {
            this.distrLock.unlock();
        }
    }

    @Override
    public void addAll(Object[] elements) {
        addAll(Arrays.asList(elements));
    }
}
