package org.coodex.filepod.webapp.util;

import org.coodex.filepod.api.IProviderSelector;

public interface IServiceIterator<T extends IProviderSelector> {
    void iterate(T service);
}
