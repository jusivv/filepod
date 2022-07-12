package org.coodex.security.access;

import org.coodex.filepod.api.IAccessController;
import org.coodex.filepod.config.ClientConfigGetter;

public abstract class ConfigurableAccessController implements IAccessController {
    protected ClientConfigGetter clientConfigGetter;

    @Override
    public void load(ClientConfigGetter clientConfigGetter) {
        this.clientConfigGetter = clientConfigGetter;
    }
}
