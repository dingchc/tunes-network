package com.tunes.library.wrapper.network.core;


import com.tunes.library.wrapper.network.listener.TSDownloadProgressListener;
import com.tunes.library.wrapper.network.listener.TSUploadProgressListener;
import com.tunes.library.wrapper.network.util.TSAppLogger;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * OKHttpClient生成类
 * @author Ding
 */

public class TSHttpClientHelper {

    private static final int CONNECT_TIMEOUT = 30000;
    private static final int READ_TIMEOUT = 30000;
    private static OkHttpClient.Builder builder = new OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS);

    private static HttpLoggingInterceptor logger = new HttpLoggingInterceptor();

    /**
     * 普通请求
     *
     * @return OkHttpClient
     */
    static OkHttpClient getHttpClient() {


        builder.interceptors().clear();

        if (TSAppLogger.BuildConfig.DEBUG) {

            logger.setLevel(HttpLoggingInterceptor.Level.BODY);
            if (!builder.interceptors().contains(logger)) {
                builder.addInterceptor(logger);
            }
        }

        return builder.build();
    }

    /**
     * 下载文件, 支持断点续传
     *
     * @param listener 回调
     * @return OkHttpClient
     */
    static OkHttpClient getDownloadHttpClient(TSDownloadProgressListener listener) {

        builder.interceptors().clear();

        return builder
                .addInterceptor(new TSDownloadRangeInterceptor(listener))
                .build();


    }

    /**
     * 上传文件
     *
     * @param listener 回调
     * @return OkHttpClient
     */
    public static OkHttpClient getUploadHttpClient(TSUploadProgressListener listener) {


        builder.interceptors().clear();

        return builder
                .addInterceptor(new TSUploadInterceptor(listener))
                .build();
    }
}
