package com.tunes.library.wrapper.network.core;

import android.text.TextUtils;


import com.tunes.library.wrapper.network.listener.TSDownloadProgressListener;
import com.tunes.library.wrapper.network.util.TSAppLogger;
import com.tunes.library.wrapper.network.util.TSUtil;

import java.io.File;
import java.io.IOException;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 支持断点下载的拦截器
 *
 * @author Ding
 *         Created by ding on 2017/4/10.
 */

public class TSDownloadRangeInterceptor implements Interceptor {

    protected TSDownloadProgressListener listener;

    TSDownloadRangeInterceptor(TSDownloadProgressListener listener) {
        this.listener = listener;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {

        Request originRequest = chain.request();

        long downloadSize = getDownloadedSize();

        Response response = chain.proceed(originRequest.newBuilder().build());

        boolean isSupportRange = false;

        Headers headers = response.headers();

        // 支持断点下载
        if (!TextUtils.isEmpty(headers.get("Accept-Ranges"))) {
            isSupportRange = true;
        }

        // 设置需要断点续传，及上次下次下载的Size
        if (isSupportRange && downloadSize > 0) {

            TSAppLogger.i("downloadSize=" + downloadSize);

            // 关闭当前请求
            response.close();

            // 重新构建一个响应
            response = chain.proceed(originRequest.newBuilder().addHeader("RANGE", "bytes=" + downloadSize + "-").build());
        }

        headers = response.headers();

        // 经过测试发现，即使服务器返回头包含"Accept-Ranges"字段，服务器也未必支持断点下载
        // 友好的方式是判断Content-Range字段中，返回内容的开始位置是不是跟请求的downloadSize大小一致
        // 例如：(bytes 1017492-8165173/8165174)
        if (isSupportRange && downloadSize > 0) {
            String contentRange = headers.get("Content-Range");
            long rangeStart = TSUtil.getRangeStartSize(contentRange);
            if (rangeStart != downloadSize) {
                isSupportRange = false;
            }
        }

        // 设置已下载大小
        if (TSUtil.checkObjNotNull(listener) && listener instanceof TSDownloadRangeImpl && isSupportRange) {
            TSDownloadRangeImpl downloadImpl = (TSDownloadRangeImpl) listener;
            downloadImpl.setDownloadSize(downloadSize);
        }

        return response
                .newBuilder()
                .body(new TSDownloadRangeResponseBody(response.body(), listener, isSupportRange))
                .build();
    }

    /**
     * 获取已下载的文件大小
     *
     * @return 大小
     */
    private long getDownloadedSize() {

        long downloadSize = 0;

        if (TSUtil.checkObjNotNull(listener) && listener instanceof TSDownloadRangeImpl) {
            TSDownloadRangeImpl downloadImpl = (TSDownloadRangeImpl) listener;

            String filePath = downloadImpl.getTempPath();

            File file = new File(filePath);

            if (file.exists()) {
                downloadSize = file.length();
            }
        }

        return downloadSize;

    }

    /**
     * 打印header
     *
     * @param headers 头
     */
    private void printHeader(Headers headers) {
        if (TSUtil.checkObjNotNull(headers)) {

            for (String key : headers.names()) {
                TSAppLogger.i("key=" + key + ", value=" + headers.get(key));
            }
        }
    }
}
