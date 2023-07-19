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
        return super.getFileName();
    }
}
