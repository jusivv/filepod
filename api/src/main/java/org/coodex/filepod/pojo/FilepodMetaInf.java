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
}
