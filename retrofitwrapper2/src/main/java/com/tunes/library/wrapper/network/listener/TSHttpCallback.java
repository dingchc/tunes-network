package com.tunes.library.wrapper.network.listener;

import com.tunes.library.wrapper.network.model.TSBaseResponse;

/**
 * 接口
 *
 * @author ding
 */
public interface TSHttpCallback {

    /**
     * 成功
     *
     * @param response 解析的结构
     * @param json     json字符串
     */
    void onSuccess(TSBaseResponse response, String json);

    /**
     * 异常
     *
     * @param exception 异常
     */
    void onException(Throwable exception);
}
