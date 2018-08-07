package com.tunes.library.wrapper.network.core;


import com.tunes.library.wrapper.network.listener.TSDownloadProgressListener;
import com.tunes.library.wrapper.network.listener.TSUploadProgressListener;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Retrofit封装类
 *
 * @author Ding
 *         Created by ding on 12/23/16.
 */

public class TSRetrofitClient {

    private Retrofit.Builder builder;

    private TSRetrofitClient() {
        builder = new Retrofit.Builder()
                .baseUrl("http://www.baidu.com")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create());
    }

    public static TSRetrofitClient getInstance() {
        return new TSRetrofitClient();
    }

    /**
     * 通用
     *
     * @param tClass retrofit
     * @return service
     */
    public <T> T createService(Class<T> tClass) {
        return builder
                .client(TSHttpClientHelper.getHttpClient())
                .build()
                .create(tClass);
    }

    /**
     * 下载
     *
     * @param tClass   retrofit
     * @param listener 监听
     * @return service
     */
    public <T> T createDownloadRangeService(Class<T> tClass, TSDownloadProgressListener listener) {
        return builder
                .client(TSHttpClientHelper.getDownloadHttpClient(listener))
                .build()
                .create(tClass);
    }

    /**
     * 上传
     *
     * @param tClass   retrofit
     * @param listener 监听
     * @return service
     */
    public <T> T createUploadService(Class<T> tClass, TSUploadProgressListener listener) {
        return builder
                .client(TSHttpClientHelper.getUploadHttpClient(listener))
                .build()
                .create(tClass);
    }

}
