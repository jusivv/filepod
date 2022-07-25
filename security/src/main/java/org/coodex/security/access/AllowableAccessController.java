package org.coodex.security.access;

/**
 * Only for test
 */
@Deprecated
public class AllowableAccessController extends ScopeAccessController {

    @Override
    public boolean writable(String clientId, String token) {
        return true;
    }

    @Override
    public boolean readable(String clientId, String token, String fileId) {
        return true;
    }

    @Override
    public boolean deletable(String clientId, String token, String fileId) {
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
