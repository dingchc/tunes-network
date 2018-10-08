package com.tunes.library.wrapper.network;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.tunes.library.wrapper.network.core.TSHttpException;
import com.tunes.library.wrapper.network.core.TSDownloadRangeImpl;
import com.tunes.library.wrapper.network.core.TSHttpService;
import com.tunes.library.wrapper.network.core.TSRetrofitClient;
import com.tunes.library.wrapper.network.listener.TSHttpCallback;
import com.tunes.library.wrapper.network.listener.TSHttpProgressCallback;
import com.tunes.library.wrapper.network.listener.TSUploadProgressListener;
import com.tunes.library.wrapper.network.model.TSHttpTracker;
import com.tunes.library.wrapper.network.model.TSResponse;
import com.tunes.library.wrapper.network.model.TSBaseResponse;
import com.tunes.library.wrapper.network.model.TSUploadFileInfo;
import com.tunes.library.wrapper.network.util.TSAppLogger;
import com.tunes.library.wrapper.network.util.TSMultiPartUtil;
import com.tunes.library.wrapper.network.util.TSUtil;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.functions.Function;
import io.reactivex.observers.ResourceObserver;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * 请求核心类
 * 请先调用下setAppContext方法，下载情况需要读取默认存储
 *
 * @author Ding
 *         Created by Ding on 2/28/17.
 */

public enum TSHttpController {

    /**
     * 实例
     */
    INSTANCE;

    private final String EMPTY_STR = "";
    private ConcurrentHashMap<TSHttpTracker, WeakReference<ResourceObserver>> requestMap;

    TSHttpController() {
        requestMap = new ConcurrentHashMap<>();
    }

    /**
     * 设置全局App上下文
     *
     * @param context 上下文
     */
    public void setAppContext(Context context) {
        TSStaticWrapper.setAppContext(context);
    }


    /**
     * 加载url
     *
     * @param url      地址
     * @param params   参数
     * @param callback 回调
     */
    public TSHttpTracker doGet(final String url, Map<String, String> params, final TSHttpCallback callback) {

        final TSHttpTracker tracker = new TSHttpTracker(url);

        ResourceObserver subscriber = new ResourceObserver<ResponseBody>() {
            @Override
            public void onComplete() {
                // 删除请求的订阅者
                requestMap.remove(tracker);
            }

            @Override
            public void onError(Throwable e) {

                TSAppLogger.e("onError");

                if (callback != null) {
                    callback.onException(e);
                }
                requestMap.remove(tracker);
            }

            @Override
            public void onNext(ResponseBody responseBody) {
                try {

                    String json = responseBody.string();

                    if (callback != null) {
                        callback.onSuccess(null, json);
                    }
                } catch (Exception e) {
                    if (callback != null) {
                        callback.onException(e);
                    }
                    e.printStackTrace();
                }

            }
        };

        requestMap.put(tracker, new WeakReference(subscriber));
        TSRetrofitClient.getInstance().createService(TSHttpService.class).doGet(url, params).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(subscriber);

        return tracker;
    }

    /**
     * 打印Byte数组
     *
     * @param buffer Byte数组
     * @return 字符串
     */
    public static String printByteArray(byte[] buffer) {

        if (buffer != null && buffer.length > 0) {

            StringBuffer sb = new StringBuffer();

            sb.append("length=" + buffer.length + " ");

            sb.append("[");
            for (byte value : buffer) {
                sb.append(value).append(", ");
            }

            sb.delete(sb.length() - 2, sb.length());

            sb.append("]");

            return sb.toString();
        } else {
            return "[]";
        }
    }

    /**
     * 加载url
     *
     * @param url      地址
     * @param params   参数
     * @param callback 回调
     */
    public TSHttpTracker doPost(final String url, Map<String, String> params, final TSHttpCallback callback) {

        final TSHttpTracker tracker = new TSHttpTracker(url);

        ResourceObserver subscriber = new ResourceObserver<ResponseBody>() {
            @Override
            public void onComplete() {
                // 删除请求的订阅者
                requestMap.remove(tracker);
            }

            @Override
            public void onError(Throwable e) {

                TSAppLogger.e("onError");

                if (callback != null) {
                    callback.onException(e);
                }
                requestMap.remove(tracker);
            }

            @Override
            public void onNext(ResponseBody responseBody) {
                try {

                    String json = responseBody.string();

                    TSAppLogger.i("json=" + json);

                    if (callback != null) {
                        callback.onSuccess(null, json);
                    }
                } catch (Exception e) {
                    if (callback != null) {
                        callback.onException(e);
                    }
                    e.printStackTrace();
                }

            }
        };

        requestMap.put(tracker, new WeakReference(subscriber));
        TSRetrofitClient.getInstance().createService(TSHttpService.class).doPost(url, params).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(subscriber);

        return tracker;
    }

    /**
     * 加载url
     *
     * @param url      地址
     * @param params   参数
     * @param clazz    转Json的类
     * @param callback 回调
     */
    public TSHttpTracker doPost(final String url, Map<String, String> params, final Class<? extends TSBaseResponse> clazz, final TSHttpCallback callback) {

        final TSHttpTracker tracker = new TSHttpTracker(url);

        ResourceObserver subscriber = new ResourceObserver<ResponseBody>() {
            @Override
            public void onComplete() {
                // 删除请求的订阅者
                requestMap.remove(tracker);
            }

            @Override
            public void onError(Throwable e) {

                TSAppLogger.e("onError");

                if (callback != null) {
                    callback.onException(e);
                }
                requestMap.remove(tracker);
            }

            @Override
            public void onNext(ResponseBody responseBody) {
                try {

                    Gson gson = new Gson();

                    String json = responseBody.string();

                    TSAppLogger.i("json=" + json);

                    TSBaseResponse t = gson.fromJson(json, clazz);

                    if (callback != null) {
                        callback.onSuccess(t, json);
                    }
                } catch (Exception e) {
                    if (callback != null) {
                        callback.onException(e);
                    }
                    e.printStackTrace();
                }

            }
        };

        requestMap.put(tracker, new WeakReference(subscriber));
        TSRetrofitClient.getInstance().createService(TSHttpService.class).doPost(url, params).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(subscriber);

        return tracker;
    }

    /**
     * 加载url
     *
     * @param url      地址
     * @param params   参数
     * @param type     转Json的TypeToken
     * @param callback 回调
     */
    public TSHttpTracker doPost(final String url, Map<String, String> params, final Type type, final TSHttpCallback callback) {

        final TSHttpTracker tracker = new TSHttpTracker(url);

        ResourceObserver subscriber = new ResourceObserver<ResponseBody>() {
            @Override
            public void onComplete() {
                // 删除请求的订阅者
                requestMap.remove(tracker);
            }

            @Override
            public void onError(Throwable e) {

                TSAppLogger.e("onError");

                if (callback != null) {
                    callback.onException(e);
                }

                requestMap.remove(tracker);
            }

            @Override
            public void onNext(ResponseBody responseBody) {
                try {

                    Gson gson = new Gson();

                    String json = responseBody.string();

                    TSAppLogger.i("json=" + json);

                    TSResponse t = gson.fromJson(json, type);

                    if (callback != null) {
                        callback.onSuccess(t, json);
                    }
                } catch (Exception e) {
                    if (callback != null) {
                        callback.onException(e);
                    }
                    e.printStackTrace();
                }

            }
        };

        requestMap.put(tracker, new WeakReference(subscriber));
        TSRetrofitClient.getInstance().createService(TSHttpService.class).doPost(url, params).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(subscriber);

        return tracker;
    }

    /**
     * Put操作
     *
     * @param url      地址
     * @param params   参数
     * @param callback 回调
     */
    public TSHttpTracker doPut(final String url, Map<String, String> params, final TSHttpCallback callback) {

        final TSHttpTracker tracker = new TSHttpTracker(url);

        ResourceObserver subscriber = new ResourceObserver<ResponseBody>() {
            @Override
            public void onComplete() {
                // 删除请求的订阅者
                requestMap.remove(tracker);
            }

            @Override
            public void onError(Throwable e) {

                TSAppLogger.e("onError");

                if (callback != null) {
                    callback.onException(e);
                }
                requestMap.remove(tracker);
            }

            @Override
            public void onNext(ResponseBody responseBody) {
                try {

                    String json = responseBody.string();

                    if (callback != null) {
                        callback.onSuccess(null, json);
                    }
                } catch (Exception e) {
                    if (callback != null) {
                        callback.onException(e);
                    }
                    e.printStackTrace();
                }

            }
        };

        requestMap.put(tracker, new WeakReference(subscriber));
        TSRetrofitClient.getInstance().createService(TSHttpService.class).doPut(url, params).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(subscriber);

        return tracker;
    }

    /**
     * 删除操作
     *
     * @param url      地址
     * @param params   参数
     * @param callback 回调
     */
    public TSHttpTracker doDelete(final String url, Map<String, String> params, final TSHttpCallback callback) {

        final TSHttpTracker tracker = new TSHttpTracker(url);

        ResourceObserver subscriber = new ResourceObserver<ResponseBody>() {
            @Override
            public void onComplete() {
                // 删除请求的订阅者
                requestMap.remove(tracker);
            }

            @Override
            public void onError(Throwable e) {

                TSAppLogger.e("onError");

                if (callback != null) {
                    callback.onException(e);
                }
                requestMap.remove(tracker);
            }

            @Override
            public void onNext(ResponseBody responseBody) {
                try {

                    String json = responseBody.string();

                    if (callback != null) {
                        callback.onSuccess(null, json);
                    }
                } catch (Exception e) {
                    if (callback != null) {
                        callback.onException(e);
                    }
                    e.printStackTrace();
                }

            }
        };

        requestMap.put(tracker, new WeakReference(subscriber));
        TSRetrofitClient.getInstance().createService(TSHttpService.class).doDelete(url, params).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(subscriber);

        return tracker;
    }

    /**
     * 断点下载
     *
     * @param url      下载地址
     * @param callback 回调
     * @return 跟踪
     */
    public TSHttpTracker downloadFileByRange(final String url, final TSHttpProgressCallback callback) {

        // 检查全局上下文是否已设置
        if (TSUtil.checkObjNotNull(callback) && !TSUtil.checkObjNotNull(TSStaticWrapper.getAppContext())) {
            callback.onException(new Exception("must call method setAppContext in MSStaticWrapper.java"));
            return null;
        }

        // 如果是本地文件，直接返回
        if (!TSUtil.isWebUrl(url) && TSUtil.checkObjNotNull(callback)) {

            TSResponse<String> esResponse = new TSResponse<>();
            esResponse.data = url;
            callback.onSuccess(esResponse, null);
            return null;
        }

        final TSHttpTracker tracker = new TSHttpTracker(url);

        final ResourceObserver subscriber = new ResourceObserver<String>() {
            @Override
            public void onComplete() {
                TSAppLogger.i("onComplete");
                // 删除请求的订阅者
                requestMap.remove(tracker);
            }

            @Override
            public void onError(Throwable e) {

                TSAppLogger.e("onError" + e.getMessage());

                if (callback != null) {
                    callback.onException(e);
                }

                requestMap.remove(tracker);
            }

            @Override
            public void onNext(String path) {

                TSAppLogger.i("onNext " + path);

                TSResponse<String> esResponse = new TSResponse<>();
                esResponse.data = path;

                if (!TextUtils.isEmpty(path)) {
                    if (callback != null) {
                        callback.onSuccess(esResponse, null);
                    }
                } else {
                    if (callback != null) {
                        callback.onException(new TSHttpException("下载中断", TSHttpException.CODE_REQUEST_INTERCEPTED));
                    }
                }
            }
        };

        final TSDownloadRangeImpl downloadImpl = new TSDownloadRangeImpl(url, callback);

        // 文件已存在，直接返回
        if (TSUtil.checkFileExist(downloadImpl.getDestPath())) {

            TSResponse<String> esResponse = new TSResponse<>();
            esResponse.data = downloadImpl.getDestPath();

            if (callback != null) {
                callback.onSuccess(esResponse, null);
            }
            return tracker;
        }

        requestMap.put(tracker, new WeakReference(subscriber));

        TSRetrofitClient.getInstance().createDownloadRangeService(TSHttpService.class, downloadImpl).downloadFile(url)
                .map(new Function<ResponseBody, String>() {

                    @Override
                    public String apply(ResponseBody response) {


                        String destPath = null;

                        String tempPath = downloadImpl.getTempPath();

                        try {
                            boolean isSuccess = writeFile(response, tempPath, downloadImpl.isSupportRange(), downloadImpl);

                            TSAppLogger.i("isSuccess=" + isSuccess);
                            if (!isSuccess) {
                                TSAppLogger.i("return null");
                                return EMPTY_STR;
                            }
                        } catch (Exception e) {
                            return EMPTY_STR;
                        }

                        destPath = downloadImpl.getDestPath();
                        TSUtil.moveFile(tempPath, destPath);

                        return destPath;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);

        return tracker;
    }

    /**
     * 上传文件
     *
     * @param url                地址
     * @param paramMap           参数
     * @param type               json类型
     * @param uploadFileInfoList 文件list
     * @param callback           回调
     */
    public TSHttpTracker doUpload(final String url, TreeMap<String, String> paramMap, final Type type, final List<TSUploadFileInfo> uploadFileInfoList, final TSHttpProgressCallback callback) {

        if (uploadFileInfoList == null || uploadFileInfoList.size() <= 0) {

            if (TSUtil.checkObjNotNull(callback)) {
                callback.onException(new TSHttpException("no input file", TSHttpException.CODE_DEFAULT));
            }

            return null;
        }

        final TSHttpTracker tracker = new TSHttpTracker(url);

        // 上传的回调
        TSUploadProgressListener progressListener = new TSUploadProgressListener() {
            @Override
            public void progress(long current, long total, boolean done) {

                if (callback != null) {

                    callback.progress(current, total, done);
                }
            }
        };

        // 创建调用
        TSHttpService api = TSRetrofitClient.getInstance().createUploadService(TSHttpService.class, progressListener);

        TSUploadFileInfo[] fileInfoArray = new TSUploadFileInfo[uploadFileInfoList.size()];

        uploadFileInfoList.toArray(fileInfoArray);

        MultipartBody.Part[] partArray = createMultipartBodyPartArray(fileInfoArray);

        TreeMap<String, RequestBody> bodyTreeMap = transformParamsMap(paramMap);

        ResourceObserver subscriber = new ResourceObserver<ResponseBody>() {
            @Override
            public void onComplete() {
                // 删除请求的订阅者
                requestMap.remove(tracker);
                TSAppLogger.i("onCompleted");
            }

            @Override
            public void onError(Throwable e) {

                TSAppLogger.i("onError");

                if (callback != null) {
                    callback.onException(e);
                }

                requestMap.remove(tracker);
            }

            @Override
            public void onNext(ResponseBody responseBody) {

                try {

                    Gson gson = new Gson();

                    String json = responseBody.string();

                    TSAppLogger.i("onNext json = " + json);

                    TSResponse t = null;

                    if (TSUtil.checkObjNotNull(type)) {
                        t = gson.fromJson(json, type);
                    }

                    if (callback != null) {
                        callback.onSuccess(t, json);
                    }
                } catch (Exception e) {
                    if (callback != null) {
                        callback.onException(e);
                    }

                    e.printStackTrace();
                }

            }
        };

        requestMap.put(tracker, new WeakReference(subscriber));

        api.uploadFiles(url, bodyTreeMap, partArray).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);

        return tracker;
    }


    /**
     * 创建MultipartBody数组
     *
     * @param fileInfos 文件
     * @return Part数组
     */
    private MultipartBody.Part[] createMultipartBodyPartArray(TSUploadFileInfo... fileInfos) {

        MultipartBody.Part[] parts = null;

        if (fileInfos != null && fileInfos.length > 0) {

            parts = new MultipartBody.Part[fileInfos.length];

            int i = 0;

            for (TSUploadFileInfo fileInfo : fileInfos) {

                String fileName = TSUtil.getFileName(fileInfo.filePath);

                if (!TextUtils.isEmpty(fileName)) {
                    RequestBody requestBody = RequestBody.create(MultipartBody.FORM, new File(fileInfo.filePath));
                    parts[i] = MultipartBody.Part.createFormData(fileInfo.fileKey, fileName, requestBody);
                    i++;
                }
            }

        }

        return parts;
    }

    /**
     * 参数转为FormData
     *
     * @param map 参数
     * @return body map
     */
    private TreeMap<String, RequestBody> transformParamsMap(TreeMap<String, String> map) {

        TreeMap<String, RequestBody> bodyTreeMap = new TreeMap<>();

        if (map != null) {
            for (String key : map.keySet()) {

                bodyTreeMap.put(key, TSMultiPartUtil.createPartFromString(map.get(key)));
            }
        }

        return bodyTreeMap;
    }

    /**
     * 取消请求
     *
     * @param tracker 请求地址
     */
    public void cancelRequest(TSHttpTracker tracker) {

        if (TSUtil.checkObjNotNull(tracker)) {

            if (requestMap.containsKey(tracker)) {
                WeakReference<ResourceObserver> weakReference = requestMap.get(tracker);
                ResourceObserver subscriber = weakReference.get();

                if (TSUtil.checkObjNotNull(subscriber)) {
                    subscriber.onError(new TSHttpException("cancel request", TSHttpException.CODE_REQUEST_CANCELED));
                    subscriber.dispose();
                }
            }

        }
    }

    /**
     * 检查是否正在进行（主要用于判断下载）
     *
     * @param url 请求地址
     * @return true 正在运行、false 未运行
     */
    public boolean checkIsDoing(String url) {

        boolean ret = false;

        for (TSHttpTracker httpTracker : requestMap.keySet()) {
            if (TSUtil.checkObjNotNull(httpTracker) && !TextUtils.isEmpty(httpTracker.getUrl()) && httpTracker.getUrl().equals(url)) {
                ret = true;
                break;
            }
        }

        TSAppLogger.i("ret=" + ret);

        return ret;
    }

    /**
     * 保存文件
     *
     * @param responseBody      响应
     * @param path              输出文件路径
     * @param isSupportRange    是否支持断点
     * @param downloadRangeImpl 回调
     * @return true 保存成功、false 保存失败
     */
    public static boolean writeFile(ResponseBody responseBody, String path, boolean isSupportRange, TSDownloadRangeImpl downloadRangeImpl) throws Exception {

        boolean ret = false;

        File saveFile = new File(path);

        OutputStream outputStream = null;

        InputStream inputStream = null;
        try {
            byte[] buffer = new byte[4096];

            long downloadSize = saveFile.length();

            long availableSize = responseBody.contentLength();

            long total = downloadSize + availableSize;

            long current = 0 + downloadSize;

            inputStream = responseBody.byteStream();

            TSAppLogger.i("downloadSize=" + downloadSize + ",availableSize=" + availableSize + ",total=" + total + ", current=" + current);

            outputStream = new FileOutputStream(saveFile, isSupportRange);

            while (true) {
                int read = inputStream.read(buffer);
                if (read == -1) {
                    break;
                }
                outputStream.write(buffer, 0, read);

                current += read;

                if (TSUtil.checkObjNotNull(downloadRangeImpl)) {
                    downloadRangeImpl.sendProgressMessage(current, total);
                }
            }

            outputStream.flush();

            ret = true;
        } catch (Exception e) {
//            e.printStackTrace();
            TSAppLogger.e(e.getMessage());
//            throw new MSHttpException("download intercept", MSHttpException.CODE_REQUEST_INTERCEPTED);
            ret = false;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return ret;
    }

    /**
     * 获取临时文件路径
     *
     * @param url 文件下载地址
     * @return 临时文件路径
     */
    public String getDownloadTempPath(String url) {

        return TSDownloadRangeImpl.createTempPath(url);
    }

    /**
     * 获取目标文件路径
     *
     * @param url 文件下载地址
     * @return 目标文件路径
     */
    public String getDownloadDestPath(String url) {

        return TSDownloadRangeImpl.createDestPath(url);
    }

}
