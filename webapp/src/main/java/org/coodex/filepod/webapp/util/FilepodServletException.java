package org.coodex.filepod.webapp.util;

public class FilepodServletException extends Exception {
    private int code;

    public FilepodServletException(int code, String message) {
        super(message);
        this.code = code;
    }

    public FilepodServletException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
