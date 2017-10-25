/**
 * Copyright © 2014成都理想境界科技有限公司. All rights reserved.
 * <p>
 * 项目名称: Idealsee-Common
 * 类名称: FilesUtil
 * 类描述:
 * 创建人: ly
 * 创建时间: 2014年11月25日 下午8:20:51
 * 修改人:
 * 修改时间:
 * 备注:
 *
 * @version
 */

package com.yixun.sdk.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.yixun.sdk.game.GameHttpClientHelper;

/**
 * Create folder.
 *
 * @author hongen
 */
public class ISARFilesUtil {

    private ISARFilesUtil() {
    }

    /**
     * 生成文件夹.
     *
     * @param path path
     */
    public static void mkdirs(String path) {
        if (isSDCardExist()) {
            File tmp = new File(path);
            if (!tmp.exists()) {
                tmp.mkdirs();
            } else {
                if (!tmp.isDirectory()) {
                    tmp.delete();
                    tmp.mkdirs();
                }
            }
        }
    }

    /**
     * 检查sdcard是否存在.
     *
     * @return true exist
     */
    public static boolean isSDCardExist() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return true;
        }
        return false;
    }

    /**
     * get file name from full file path.
     *
     * @param filePath file path
     * @param hasExtra true file name with extra, false not
     * @return file name
     */
    public static String getFileNameFromPath(String filePath, boolean hasExtra) {
        String nName = "";
        if (hasExtra) {
            nName = filePath.substring(filePath.lastIndexOf("/") + 1);
        } else {
            nName = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.lastIndexOf("."));
        }
        return nName;
    }

    /**
     * DeCompress the ZIP to the path
     *
     * @param zipFileString name of ZIP
     * @param outPathString path to be unZIP
     * @throws Exception
     */
    public static void UnZipFolder(String zipFileString, String outPathString) throws Exception {
        ZipInputStream inZip = new ZipInputStream(new FileInputStream(zipFileString));
        ZipEntry zipEntry;
        String szName = "";
        while ((zipEntry = inZip.getNextEntry()) != null) {
            szName = zipEntry.getName();
            if (zipEntry.isDirectory()) {
                // get the folder name of the widget
                szName = szName.substring(0, szName.length() - 1);
                File folder = new File(outPathString + File.separator + szName);
                folder.mkdirs();
            } else {
                File file = new File(outPathString + File.separator + szName);
                file.createNewFile();
                // get the output stream of the file
                FileOutputStream out = new FileOutputStream(file);
                int len;
                byte[] buffer = new byte[1024];
                // read (len) bytes into buffer
                while ((len = inZip.read(buffer)) != -1) {
                    // write (len) byte from buffer at the position 0
                    out.write(buffer, 0, len);
                    out.flush();
                }
                out.close();
                //delete zip file
                new File(zipFileString).delete();
            }
        }
        inZip.close();
    }

    public static String FILE_PATH = "";
    public static float VIDEO_WIDTH = 0;
    public static float VIDEO_HEIGHT = 0;

    public static float getVIDEO_WIDTH() {
        return VIDEO_WIDTH;
    }

    public static void setVIDEO_WIDTH(float vIDEO_WIDTH) {
        VIDEO_WIDTH = vIDEO_WIDTH;
    }

    public static float getVIDEO_HEIGHT() {
        return VIDEO_HEIGHT;
    }

    public static void setVIDEO_HEIGHT(float vIDEO_HEIGHT) {
        VIDEO_HEIGHT = vIDEO_HEIGHT;
    }

    public static void setVideoFilePath(String path) {
        FILE_PATH = path;
    }

    public static String getVideoFilePath() {
        return FILE_PATH;
    }

    /**
     * 计算AR缓存大小：M
     *
     * @return M
     */
    public static float getISARCacheSize() {
//        final File unityCache = new File(ISARConstants.UNITY_RESOURCES_DIRETCORY);
        final File sdkCache = new File(ISARConstants.APP_PARENT_PATH);
        long size = getFileLength(sdkCache);
        float cSize = (float) size / (float) (1024 * 1024);
        return cSize;
    }

    /**
     * 清楚AR缓存数据
     *
     * @return
     */
    public static boolean clearISARCache() {
//        final File unityCache = new File(ISARConstants.UNITY_RESOURCES_DIRETCORY);
        final File sdkCache = new File(ISARConstants.APP_PARENT_PATH);
        if (sdkCache.exists()) {
            File[] sdkFiles = sdkCache.listFiles();
            for (File file : sdkFiles) {
                deleteAllFiles(file);
            }
        }
        /*if (unityCache.exists()) {
            deleteAllFiles(unityCache);
        }*/
        return true;
    }

    private static long getFileLength(final File file) {
        long total = 0;
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            for (File child : children) {
                if (child.isDirectory()) {
                    total += getFileLength(child);
                } else {
                    total += child.length();
                }
            }
            Logger.LOGD("[getFileLength] file=" + file.getAbsolutePath() + ", getFileLength:" + total);
            return total;
        } else {
            return file.length();
        }
    }

    /**
     * delete all files in file
     *
     * @param file
     */
    public static void deleteAllFiles(File file) {
        Logger.LOGD(" deleteAllFiles : " + file.getAbsolutePath());
        File files[] = file.listFiles();
        if (files != null && files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) { // 判断是否为文件夹
                    deleteAllFiles(files[i]);
                } else {
                    deleteFile(files[i]);
                    if (i == files.length - 1) //删完子文件，删除空文件夹
                        deleteAllFiles(file);
                }
            }
        } else {
            deleteFile(file);
        }
    }

    private static void deleteFile(File f) {
        if (f.exists()) { // 判断是否存在
            try {
                // Logger.LOGD("[deleteFile] file=" + f.getAbsolutePath());
                f.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static class FileInfo {

        public String name;
        public String path;
        public long lastModified;
    }

    public static class FileComparator implements Comparator<FileInfo> {
        @Override
        public int compare(FileInfo file1, FileInfo file2) {
            if (file1.lastModified > file2.lastModified) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    public static FileFilter fileFilter = new FileFilter() {
        @Override
        public boolean accept(File file) {
            String tmp = file.getName().toLowerCase();
            if (tmp.endsWith(".mp4")) {
                return true;
            }
            return false;
        }
    };

    public synchronized static final String getRecordedFilePath(File rootFile) {
        if (ISARSdCardUtil.checkSdCardExist()) {
            if (rootFile.isDirectory()) {
                //                String filePath;
                File[] files = rootFile.listFiles(fileFilter);
                ArrayList<FileInfo> fileList = new ArrayList<FileInfo>();//将需要的子文件信息存入到FileInfo里面
                for (int i = 0; i < files.length; i++) {
                    File file = files[i];
                    FileInfo fileInfo = new FileInfo();
                    fileInfo.name = file.getName();
                    fileInfo.path = file.getPath();
                    fileInfo.lastModified = file.lastModified();
                    fileList.add(fileInfo);
                }
                Collections.sort(fileList, new FileComparator());
                return fileList.get(0).path;
            } else {
                return rootFile.getAbsolutePath();
            }
        }
        return null;
    }

    //读取指定目录下的所有TXT文件的文件内容
    public static String getTxtFileContent(File file) {
        String content = "";
        if (file.isDirectory()) {
            Logger.LOGI(" getTxtFileContent()" + "The File doesn't not exist " + file.getName().toString() + file.getPath().toString());
        } else {
            if (file.getName().endsWith(".txt")) {
                try {
                    InputStream instream = new FileInputStream(file);
                    if (instream != null) {
                        InputStreamReader inputreader
                                = new InputStreamReader(instream, "GBK");
                        BufferedReader buffreader = new BufferedReader(inputreader);
                        String line = "";
                        //分行读取
                        while ((line = buffreader.readLine()) != null) {
                            content += line + "\n";
                        }
                        instream.close();       //关闭输入流
                    }
                } catch (java.io.FileNotFoundException e) {
                    Log.d("TestFile", "The File doesn't not exist.");
                } catch (IOException e) {
                    Log.d("TestFile", e.getMessage());
                }
            }
        }
        return content;
    }

    // 检查用户日志文件.
    public static void createRestoreFile() {
        if (ISARSdCardUtil.checkSdCardExist()) {
            try {
                File file = new File(ISARConstants.APP_BEHAVIOR_LOG_FILE);
                if (file.length() == 0) {
                    file.createNewFile();
                } else {
                    if (file.length() > 1024) {
                        uploadToServer();
                        return;
                    }
                }
                FileWriter writer = new FileWriter(file, true);
                if (file.length() == 0) {
                    StringBuffer bf = new StringBuffer();
                    bf.append("imei:" + ISARConstants.APP_IMEI + ",");
                    bf.append("device:" + android.os.Build.MANUFACTURER
                            + " " + android.os.Build.MODEL + ",");
                    bf.append("system:android,");
                    bf.append("os:" + android.os.Build.VERSION.RELEASE
                            + "(" + android.os.Build.VERSION.SDK_INT + "),");
                    bf.append("App:" + ISARConstants.APP_PACKAGE_NAME
                            + ",software:" + ISARConstants.SDK_VERSION
                            + ",app-from:" + ISARConstants.APP_PACKAGE_NAME
                            + ";\n");
                    writer.write(bf.toString());
                }
                writer.write(String.valueOf(System.currentTimeMillis()) + ":");
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void uploadToServer() {
        GameHttpClientHelper.getInstance().doUploadUserLog(new GameHttpClientHelper.HttpCallBack() {
            @Override
            public void onSuccess() {
                createUserLogFile();
            }

            @Override
            public void onFailed() {

            }
        });
    }

    // 创建用户日志文件.
    private static void createUserLogFile() {
        if (ISARSdCardUtil.checkSdCardExist()) {
            try {
                File file = new File(ISARConstants.APP_BEHAVIOR_LOG_FILE);
                if (file.exists()) {
                    file.delete();
                }
                file.createNewFile();
                FileWriter writer = new FileWriter(file, true);
                if (file.length() == 0) {
                    StringBuffer bf = new StringBuffer();
                    bf.append("imei:" + ISARConstants.APP_IMEI + ",");
                    bf.append("device:" + android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL + ",");
                    bf.append("system:android,");
                    bf.append("os:" + android.os.Build.VERSION.RELEASE + "(" + android.os.Build.VERSION.SDK_INT + "),");
                    bf.append("App:" + ISARConstants.APP_PACKAGE_NAME
                            + ",software:" + ISARConstants.SDK_VERSION
                            + ",app-from:" + ISARConstants.APP_PACKAGE_NAME
                            + ";\n");
                    writer.write(bf.toString());
                }
                writer.write(String.valueOf(System.currentTimeMillis()) + ":");
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static synchronized boolean copyFile(String fromPath, File destFile) {
        File formFile = new File(fromPath);
        if (!formFile.exists()) {
            return false;
        }

        if (destFile.exists()) {
            return false;
        }

        int readSize = 0;
        InputStream in = null;
        OutputStream out = null;

        try {
            in = new FileInputStream(formFile);
            out = new FileOutputStream(destFile);
            byte[] buffer = new byte[1024];
            while ((readSize = in.read(buffer)) != -1) {
                out.write(buffer, 0, readSize);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * save video to album
     * add by zongkaili
     */
    public static void saveVideoFileToAlbum(Context mContext, String filePath) {
        File appDir = new File(ISARConstants.APP_SCREEN_VIDEO_DIRECTORY);
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".3gp";
        File file = new File(appDir, fileName);
        copyFile(filePath, file);
        ContentResolver localContentResolver = mContext.getContentResolver();
        ContentValues locaContentValues = getVideoContentValues(mContext, file, System.currentTimeMillis());
        Uri localUri = localContentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, locaContentValues);
        mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, localUri));
        Toast.makeText(mContext, "视频已保存到您相册", Toast.LENGTH_SHORT).show();
    }

    private static ContentValues getVideoContentValues(Context paramContext, File paramFile, long paramLong) {
        ContentValues localContentValues = new ContentValues();
        localContentValues.put("title", paramFile.getName());
        localContentValues.put("_display_name", paramFile.getName());
        localContentValues.put("mime_type", "video/3gp");
        localContentValues.put("datetaken", Long.valueOf(paramLong));
        localContentValues.put("date_modified", Long.valueOf(paramLong));
        localContentValues.put("date_added", Long.valueOf(paramLong));
        localContentValues.put("_data", paramFile.getAbsolutePath());
        localContentValues.put("_size", Long.valueOf(paramFile.length()));
        return localContentValues;
    }
}
