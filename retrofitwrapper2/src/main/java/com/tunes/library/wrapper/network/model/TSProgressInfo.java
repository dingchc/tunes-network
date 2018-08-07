package com.tunes.library.wrapper.network.model;

/**
 * @author Ding
 * 进度类
 */
public class TSProgressInfo {

    public long current;
    public long total;

    public TSProgressInfo(long current, long total) {

        this.current = current;
        this.total = total;
    }
}
