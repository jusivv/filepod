package org.coodex.filepod.api;

import org.coodex.filepod.pojo.FilepodMetaInf;

public interface ICryptoStreamWrapper extends IStreamWrapper {
    void init(byte[] key, FilepodMetaInf metaInf);
}
