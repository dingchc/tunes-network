package com.tunes.library.wrapper.network.core;

import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.tunes.library.wrapper.network.model.TSProgressInfo;
import com.tunes.library.wrapper.network.listener.TSDownloadProgressListener;
import com.tunes.library.wrapper.network.listener.TSHttpProgressCallback;
import com.tunes.library.wrapper.network.util.TSDirUtil;
import com.tunes.library.wrapper.network.util.TSUtil;


/**
 * 断点下载辅助类
 *
 * @author Ding
 *         Created by ding on 4/20/17.
 */

public class TSDownloadRangeImpl implements TSDownloadProgressListener {

    /**
     * 更新进度的最小间隔时间
     */
    private final static int INTERVAL_TIME_MILLIS = 200;

    /**
     * 回调
     */
    private TSHttpProgressCallback callback;

    /**
     * 是否支持断点续传
     */
    private boolean isSupportRange;

    /**
     * 存储的文件名
     */
    private String destPath;

    /**
     * 临时文件名
     */
    private String tempPath;

    /**
     * 已下载的大小
     */
    private long downloadSize = 0;

    /**
     * handler
     */
    private DownloadHandler downloadHandler;

    /**
     * 上次通知回调的时间
     */
    private long lastNotifyTime;

    public TSDownloadRangeImpl(String url, TSHttpProgressCallback callback) {
        this.callback = callback;

        destPath = createDestPath(url);
        tempPath = createTempPath(url);

        downloadHandler = new DownloadHandler(Looper.getMainLooper());
    }

    /**
     * 生成输出文件路径
     *
     * @param url 文件下载地址
     * @return 输出文件路径
     */
    public static String createDestPath(String url) {

        String filePath = "";

        if (TextUtils.isEmpty(url)) {
            return filePath;
        }

        String fileName = TSUtil.getFileName(url);
        filePath = TSDirUtil.getValidPath(TSDirUtil.getDownloadDir(), fileName);

        return filePath;
    }

    /**
     * 生成临时文件路径
     *
     * @param url 文件下载地址
     * @return 临时文件路径
     */
    public static String createTempPath(String url) {

        String filePath = "";

        if (TextUtils.isEmpty(url)) {
            return filePath;
        }

        String fileName = TSUtil.createTempFileName(TSUtil.getFileName(url));
        filePath = TSDirUtil.getValidPath(TSDirUtil.getDownloadDir(), fileName);

        return filePath;
    }

    @Override
    public void progress(long current, long total, boolean done) {

        notifyCallbackDelay(current, total);
    }

    /**
     * 通知回调
     *
     * @param current 当前
     * @param total   全部
     */
    private void notifyCallbackDelay(long current, long total) {

        long currentTime = System.currentTimeMillis();

        long duration = currentTime - lastNotifyTime;

        if (lastNotifyTime <= 0 || duration >= INTERVAL_TIME_MILLIS || current == total) {

            if (TSUtil.checkObjNotNull(callback)) {
                callback.progress(current, total, current == total);
            }
            lastNotifyTime = System.currentTimeMillis();
        }
    }

    /**
     * 是否支持断点续传
     *
     * @return true 支持、false 不支持
     */
    public synchronized boolean isSupportRange() {
        return isSupportRange;
    }

    /**
     * 设置是否支持
     *
     * @param supportRange 是、否
     */
    synchronized void setSupportRange(boolean supportRange) {
        isSupportRange = supportRange;
    }

    /**
     * 获取保存的文件地址
     *
     * @return 保存的文件
     */
    public String getDestPath() {
        return destPath;
    }

    /**
     * 获取临时文件地址
     *
     * @return 路径
     */
    public String getTempPath() {
        return tempPath;
    }

    /**
     * 已下载的大小
     *
     * @return 下载的大小
     */
    long getDownloadSize() {
        return downloadSize;
    }

    /**
     * 设置已下载的大小
     *
     * @param downloadSize 文件大小
     */
    void setDownloadSize(long downloadSize) {
        this.downloadSize = downloadSize;
    }

    /**
     * 发送进度更新消息
     *
     * @param current 当前下载量
     * @param total   总量
     */
    public void sendProgressMessage(long current, long total) {

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

                        progress(current, total, current == total);

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
