package com.tunes.library.wrapper.network.model;

/**
 * 上传文件时，文件的信息
 * Created by ding on 9/26/16.
 */
public class TSUploadFileInfo {

    public String filePath;

    public String fileKey;

    public TSUploadFileInfo(String filePath, String fileKey) {

        this.filePath = filePath;
        this.fileKey = fileKey;
    }


}
