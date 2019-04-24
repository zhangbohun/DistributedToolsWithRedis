package com.zhangbohun.syncvariable;

/**
 * @author zhangbohun
 * Create Date 2019/04/17 18:15
 * Modify Date 2019/04/17 18:22
 */
public class TimeoutException extends Exception {
    //只有带参数的构造函数，必须要填写异常信息
    public TimeoutException(String msg) {
        super(msg);
    }
}