package com.yixun.sdk.game;

import java.util.List;

/**
 * Created by yaolei on 17-3-24.
 */

public class ARPackageRefreshInfo {
    private List<ARPackageInfo> refreshList;
    private List<String> deleteList;

    public List<ARPackageInfo> getRefreshList() {
        return refreshList;
    }

    public List<String> getDeleteList() {
        return deleteList;
    }

    public ARPackageRefreshInfo(List<ARPackageInfo> refreshList, List<String> deleteList) {
        this.refreshList = refreshList;
        this.deleteList = deleteList;
    }
}
