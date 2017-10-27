package com.idealsee.sdk.idhttp;


/**
 * Created by yaolei on 2017/3/22.
 */

class IdFileGetRequest extends IdRequest {
    private String targetPath;
    private long maxFileLength;

    public String getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }

    public long getMaxFileLength() {
        return maxFileLength;
    }

    public void setMaxFileLength(long maxFileLength) {
        this.maxFileLength = maxFileLength;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(super.toString());
        builder.append("targetPath:").append(targetPath).append(";");
        builder.append("maxFileLength:").append(maxFileLength).append(";");
        return builder.toString();
    }
}
