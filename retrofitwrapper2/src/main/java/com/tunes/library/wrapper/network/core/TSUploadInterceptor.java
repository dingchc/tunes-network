package com.tunes.library.wrapper.network.core;


import com.tunes.library.wrapper.network.listener.TSUploadProgressListener;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 上传拦截器
 * @author Ding
 */

public class TSUploadInterceptor implements Interceptor {

    private TSUploadProgressListener listener;

    TSUploadInterceptor(TSUploadProgressListener listener) {
        this.listener = listener;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        Request request = originalRequest.newBuilder()
                .method(originalRequest.method(), new TSUploadRequestBody(originalRequest.body(), listener))
                .build();

        return chain.proceed(request);
    }
}
