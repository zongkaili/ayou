/**
 * Copyright © 2015成都理想境界科技有限公司. All rights reserved.
 * 项目名称: Idealsee-SDK
 * 类名称: ISearchResult
 *
 * @version 
 */

package com.yixun.sdk.model;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * ImageSearchResult.
 * 
 * @author ly
 * 
 */
public class ISARImageSearchResult implements Serializable {

    /**
     * serial.
     */
    private static final long serialVersionUID = -3110945905572971625L;
    private static final String TEMPLATE_XML_SRC = "template_xml_src";
    private static final String TEMPLATE_DAT_SRC = "template_dat_src";
    private static final String TEMPLATE_BATCH_XML_SRC = "template_batch_xml_src";
    private static final String TEMPLATE_BATCH_DATA_SRC = "template_batch_data_src";
    private static final String IF_AR = "if_ar";
    private static final String MD5 = "md5";
    private static final String SIMIALR_MD5 = "similar_md5";
    private static final String WIDTH = "width";
    private static final String HEIGHT = "height";
    private static final String TEMPLATE_ZIP_SRC = "template_zip_src";

    public String xmlSrc;
    public String batchXmlSrc;
    public String datSrc;
    public String batchDatSrc;
    public String md5;
    public String similarMd5;
    public boolean isAr;
    public int width;
    public int height;
    public int errorCode;
    private String templateZipSrc;

    /**
     * Default constructor.
     */
    public ISARImageSearchResult() {
    }

    /**
     * Constructor.
     *
     * @param json
     *            json string
     */
    public ISARImageSearchResult(JSONObject json) {
        this.errorCode = 0;
        try {
            if (json.has(TEMPLATE_XML_SRC)) {
                this.xmlSrc = json.getString(TEMPLATE_XML_SRC);
            }

            if (json.has(TEMPLATE_DAT_SRC)) {
                this.datSrc = json.getString(TEMPLATE_DAT_SRC);
            }

            if (json.has(IF_AR)) {
                this.isAr = json.getBoolean(IF_AR);
            }

            if (json.has(MD5)) {
                this.md5 = json.getString(MD5);
            }

            if (json.has(SIMIALR_MD5)) {
                this.similarMd5 = json.getString(SIMIALR_MD5);
            }

            if (json.has(WIDTH)) {
                this.width = json.getInt(WIDTH);
            }

            if (json.has(HEIGHT)) {
                this.height = json.getInt(HEIGHT);
            }

            if (json.has(TEMPLATE_BATCH_XML_SRC)) {
                this.batchXmlSrc = json.getString(TEMPLATE_BATCH_XML_SRC);
            }

            if (json.has(TEMPLATE_BATCH_DATA_SRC)) {
                this.batchDatSrc = json.getString(TEMPLATE_BATCH_DATA_SRC);
            }
            if (json.has(TEMPLATE_ZIP_SRC)) {
                this.setTemplateZipSrc(json.getString(TEMPLATE_ZIP_SRC));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * copy json data to objct.
     *
     * @param json
     */
    public void cloneData(JSONObject json) {
        this.errorCode = 0;
        try {
            if (json.has(TEMPLATE_XML_SRC)) {
                this.xmlSrc = json.getString(TEMPLATE_XML_SRC);
            }

            if (json.has(TEMPLATE_DAT_SRC)) {
                this.datSrc = json.getString(TEMPLATE_DAT_SRC);
            }

            if (json.has(IF_AR)) {
                this.isAr = json.getBoolean(IF_AR);
            }

            if (json.has(MD5)) {
                this.md5 = json.getString(MD5);
            }

            if (json.has(SIMIALR_MD5)) {
                this.similarMd5 = json.getString(SIMIALR_MD5);
            }

            if (json.has(WIDTH)) {
                this.width = json.getInt(WIDTH);
            }

            if (json.has(HEIGHT)) {
                this.height = json.getInt(HEIGHT);
            }

            if (json.has(TEMPLATE_BATCH_XML_SRC)) {
                this.batchXmlSrc = json.getString(TEMPLATE_BATCH_XML_SRC);
            }

            if (json.has(TEMPLATE_BATCH_DATA_SRC)) {
                this.batchDatSrc = json.getString(TEMPLATE_BATCH_DATA_SRC);
            }
            if (json.has(TEMPLATE_ZIP_SRC)) {
                this.setTemplateZipSrc(json.getString(TEMPLATE_ZIP_SRC));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * clear data of object
     */
    public void clearData() {
        this.xmlSrc = null;
        this.datSrc = null;
        this.isAr = false;
        this.md5 = null;
        this.width = 0;
        this.height = 0;
        this.batchXmlSrc = null;
        this.batchDatSrc = null;
        this.setTemplateZipSrc(null);
    }

    /**
     * @return the xmlSrc
     */
    public String getXmlSrc() {
        return xmlSrc;
    }

    /**
     * @param xmlSrc
     *            the xmlSrc to set
     */
    public void setXmlSrc(String xmlSrc) {
        this.xmlSrc = xmlSrc;
    }

    /**
     * @return the batchXmlSrc
     */
    public String getBatchXmlSrc() {
        return batchXmlSrc;
    }

    /**
     * @param batchXmlSrc
     *            the batchXmlSrc to set
     */
    public void setBatchXmlSrc(String batchXmlSrc) {
        this.batchXmlSrc = batchXmlSrc;
    }

    /**
     * @return the datSrc
     */
    public String getDatSrc() {
        return datSrc;
    }

    /**
     * @param datSrc
     *            the datSrc to set
     */
    public void setDatSrc(String datSrc) {
        this.datSrc = datSrc;
    }

    /**
     * @return the batchDatSrc
     */
    public String getBatchDatSrc() {
        return batchDatSrc;
    }

    /**
     * @param batchDatSrc
     *            the batchDatSrc to set
     */
    public void setBatchDatSrc(String batchDatSrc) {
        this.batchDatSrc = batchDatSrc;
    }

    /**
     * @return the md5
     */
    public String getMd5() {
        return md5;
    }

    /**
     * @param md5
     *            the md5 to set
     */
    public void setMd5(String md5) {
        this.md5 = md5;
    }

    /**
     * @return the isAr
     */
    public boolean isAr() {
        return isAr;
    }

    /**
     * @param isAr
     *            the isAr to set
     */
    public void setAr(boolean isAr) {
        this.isAr = isAr;
    }

    /**
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * @param width
     *            the width to set
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    /**
     * @param height
     *            the height to set
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * @return the errorCode
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * @param errorCode
     *            the errorCode to set
     */
    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getTemplateZipSrc() {
        return templateZipSrc;
    }

    public void setTemplateZipSrc(String templateZipSrc) {
        this.templateZipSrc = templateZipSrc;
    }
}
