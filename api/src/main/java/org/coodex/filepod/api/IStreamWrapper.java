package org.coodex.filepod.api;

import java.io.InputStream;
import java.io.OutputStream;

public interface IStreamWrapper extends IProviderSelector {
    InputStream getInput(InputStream inputStream);

    OutputStream getOutput(OutputStream outputStream);
}
