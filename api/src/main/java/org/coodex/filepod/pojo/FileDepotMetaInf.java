package org.coodex.filepod.pojo;

/**
 * for compatibility of file-depot
 */
public class FileDepotMetaInf extends FilepodMetaInf {
    /**
     * salt for encryption key
     */
    private String salt;

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }
}
