package org.coodex.filepod.pojo;

import org.coodex.filerepository.api.FileMetaInf;

public class FilepodMetaInf extends FileMetaInf {
    /**
     * MIME type
     */
    private String contentType;
    /**
     * Encryption method, null or empty if the file stored without encryption
     */
    private String cipherModel;
    /**
     * file identify
     */
    private String fileId;
    /**
     * alias of fileName
     */
    private String name;
    /**
     * file size (for old json)
     */
    private long size;

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getCipherModel() {
        return cipherModel;
    }

    public void setCipherModel(String cipherModel) {
        this.cipherModel = cipherModel;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getId() {
        return fileId;
    }

    public String getName() {
        return name != null && !name.equals("") ? name : getFileName();
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
