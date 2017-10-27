package com.idealsee.sdk.idhttp;

import java.util.Map;

/**
 * Created by yaolei on 17-3-23.
 */

class IdFilePostRequest extends IdRequest {
    private Map<String, String> bodyParams;
    private Map<String, String> fileParams;

    public Map<String, String> getBodyParams() {
        return bodyParams;
    }

    public void setBodyParams(Map<String, String> bodyParams) {
        this.bodyParams = bodyParams;
    }

    public Map<String, String> getFileParams() {
        return fileParams;
    }

    public void setFileParams(Map<String, String> fileParams) {
        this.fileParams = fileParams;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(super.toString());
        builder.append("body params size:").append(bodyParams != null ? bodyParams.size() : "null").append(";");
        builder.append("file params size:").append(fileParams != null ? fileParams.size() : "null").append(";");
        return builder.toString();
    }
}
