package org.coodex.security.crypto;

import org.coodex.filepod.api.ICryptoStreamWrapper;
import org.coodex.filepod.pojo.FilepodMetaInf;
import org.coodex.filerepository.ext.crypto.CryptoParameter;
import org.coodex.filerepository.ext.crypto.DecryptOutputStream;
import org.coodex.filerepository.ext.crypto.EncryptInputStream;
import org.coodex.util.DigestHelper;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class AesV2CryptoStreamWrapper implements ICryptoStreamWrapper {
    private byte[] key;
    @Override
    public void init(byte[] key, FilepodMetaInf metaInf) {
        byte[] salt = metaInf.getClientId().getBytes(StandardCharsets.UTF_8);
        byte[] clientKey = Arrays.copyOf(key, key.length + salt.length);
        System.arraycopy(salt, 0, clientKey, key.length, salt.length);
        this.key = DigestHelper.digestBuff(clientKey, "md5");
    }

    @Override
    public boolean accept(String tag) {
        return "aes.v2".equalsIgnoreCase(tag);
    }

    @Override
    public InputStream getInput(InputStream inputStream) throws Throwable {
        return new EncryptInputStream(inputStream, key, CryptoParameter.buildCtrCryptoParameter(
                CryptoParameter.CipherClass.JCE, null));
    }

    @Override
    public OutputStream getOutput(OutputStream outputStream) throws Throwable {
        return new DecryptOutputStream(outputStream, key, CryptoParameter.buildCtrCryptoParameter(
                CryptoParameter.CipherClass.JCE, null
        ));
    }
}
