package com.zhangbohun.bloomfilter;

import java.util.Collection;

/**
 * @author zhangbohun
 * Create Date 2019/04/24 19:31
 * Modify Date 2019/04/25 02:06
 */
public interface BloomFilter {
    long size();

    void clear();

    boolean contains(Object element);

    boolean containsAll(Collection elements);

    boolean containsAll(Object[] elements);

    boolean add(Object element);

    void addAll(Collection elements);

    void addAll(Object[] elements);
}
