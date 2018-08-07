package com.tunes.library.wrapper.network.model;

/**
 * 请求追踪者
 *
 * @author Ding
 *         Created by ding on 3/9/17.
 */

public class TSHttpTracker {

    private String url;

    public TSHttpTracker(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
