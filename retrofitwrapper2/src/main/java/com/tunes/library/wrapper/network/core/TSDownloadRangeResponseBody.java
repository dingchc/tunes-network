package com.tunes.library.wrapper.network.core;

import android.os.Looper;
import android.os.Message;


import com.tunes.library.wrapper.network.model.TSProgressInfo;
import com.tunes.library.wrapper.network.listener.TSDownloadProgressListener;
import com.tunes.library.wrapper.network.util.TSUtil;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * 支持断点下载的ResponseBody
 * @author Ding
 * Created by ding on 2017/4/10.
 */

public class TSDownloadRangeResponseBody extends ResponseBody {

    private ResponseBody responseBody;
    private TSDownloadProgressListener listener;
    private BufferedSource bufferedSource;
    private DownloadHandler downloadHandler;

    TSDownloadRangeResponseBody(ResponseBody responseBody, TSDownloadProgressListener listener, boolean isSupportRange) {
        this.responseBody = responseBody;
        this.listener = listener;

        if (listener instanceof TSDownloadRangeImpl) {
            TSDownloadRangeImpl download = (TSDownloadRangeImpl) listener;
            download.setSupportRange(isSupportRange);
        }

        downloadHandler = new DownloadHandler(Looper.getMainLooper());
    }

    @Override
    public MediaType contentType() {
        return responseBody.contentType();
    }

    @Override
    public long contentLength() {
        return responseBody.contentLength();
    }

    @Override
    public BufferedSource source() {

        if (bufferedSource == null) {
            bufferedSource = Okio.buffer(source(responseBody.source()));
        }
        return bufferedSource;
    }

    private Source source(Source source) {

        return new ForwardingSource(source) {

            long current = 0L;
            long total = 0L;

            @Override
            public long read(Buffer sink, long byteCount) throws IOException {

                long byteRead = super.read(sink, byteCount);

                if (listener != null) {

                    if (total == 0) {
                        total = responseBody.contentLength();
                    }

                    current += byteRead != -1 ? byteRead : 0;
                }

                return byteRead;
            }
        };
    }


    /**
     * 发送进度更新消息
     *
     * @param current 当前下载量
     * @param total   总量
     */
    private void sendProgressMessage(long current, long total) {

        TSProgressInfo progressInfo = new TSProgressInfo(current, total);

        Message msg = Message.obtain();
        msg.what = DownloadHandler.WHAT_UPDATE;
        msg.obj = progressInfo;

        downloadHandler.sendMessage(msg);
    }

    /**
     * 下载Handler
     */
    class DownloadHandler extends android.os.Handler {

        static final int WHAT_UPDATE = 0;

        DownloadHandler(Looper looper) {

            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {

            try {

                if (msg.what == WHAT_UPDATE) {

                    if (TSUtil.checkObjNotNull(msg.obj)) {

                        TSProgressInfo progressInfo = (TSProgressInfo) msg.obj;

                        long total = progressInfo.total;
                        long current = progressInfo.current;


                        if (listener != null) {
                            listener.progress(current, total, current == total);
                        }

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
