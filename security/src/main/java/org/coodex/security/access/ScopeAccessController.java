package org.coodex.security.access;

public abstract class ScopeAccessController extends ConfigurableAccessController {
    @Override
    public boolean inScope(String clientId, String fileOwner) {
        if (clientId.equals(fileOwner)) {
            return true;
        }
        String scopes = clientConfigGetter.getParameterValue(clientId, "scope", null);
        if (scopes != null) {
            for (String scope : scopes.split(",")) {
                scope = scopes.trim();
                if (scope.equals("*") || scope.equals(fileOwner)) {
                    return true;
                }
            }
        }
        return false;
    }
}
