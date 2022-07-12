package org.coodex.security.access;

/**
 * Controller that refused access from any client
 */
public class ForbiddenAccessController extends ScopeAccessController {

    @Override
    public boolean canWrite(String clientId, String token) {
        return false;
    }

    @Override
    public boolean canRead(String clientId, String token, String fileId) {
        return false;
    }

    @Override
    public boolean canDelete(String clientId, String token, String fileId) {
        return false;
    }

    @Override
    public boolean notify(String clientId, String token, String fileId) {
        return false;
    }

    @Override
    public boolean accept(String tag) {
        return "forbidden".equalsIgnoreCase(tag);
    }
}
