package org.coodex.security.access;

public class AllowableAccessController extends ScopeAccessController {

    @Override
    public boolean canWrite(String clientId, String token) {
        return true;
    }

    @Override
    public boolean canRead(String clientId, String token, String fileId) {
        return true;
    }

    @Override
    public boolean canDelete(String clientId, String token, String fileId) {
        return true;
    }

    @Override
    public boolean notify(String clientId, String token, String fileId) {
        return true;
    }

    @Override
    public boolean accept(String tag) {
        return "allowable".equalsIgnoreCase(tag);
    }
}
