package com.tunes.library.wrapper.network.core;

/**
 * 下载异常
 *
 * @author Ding
 *         Created by ding on 4/21/17.
 */

public class TSHttpException extends Exception {

    public final static int CODE_DEFAULT = 0;

    /**
     * 请求取消
     */
    public final static int CODE_REQUEST_CANCELED = 1001;

    /**
     * 请求中断
     */
    public final static int CODE_REQUEST_INTERCEPTED = 1002;


    private int code = CODE_DEFAULT;

    public TSHttpException(String msg) {
        super(msg);
    }

    public TSHttpException(String msg, int code) {
        super(msg);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
