package org.coodex.security.crypto;

import org.coodex.filepod.api.ICryptoStreamWrapper;
import org.coodex.filepod.pojo.FileDepotMetaInf;
import org.coodex.filepod.pojo.FilepodMetaInf;
import org.coodex.filerepository.ext.crypto.CryptoParameter;
import org.coodex.filerepository.ext.crypto.DecryptOutputStream;
import org.coodex.filerepository.ext.crypto.EncryptInputStream;
import org.coodex.util.DigestHelper;

import javax.crypto.spec.IvParameterSpec;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class AesV1CryptoStreamWrapper implements ICryptoStreamWrapper {

    private static final String TRANSFORM = "AES/CFB/NoPadding";

    private static final IvParameterSpec IV = new IvParameterSpec("0123456789ABCDEF".getBytes(StandardCharsets.UTF_8));
    private byte[] key;

    @Override
    public boolean accept(String tag) {
        return "aes.v1".equalsIgnoreCase(tag);
    }

    @Override
    public InputStream getInput(InputStream inputStream) throws Throwable {
        return new EncryptInputStream(inputStream, this.key, CryptoParameter.buildCfbCryptoParameter(
                CryptoParameter.CipherClass.JCE, null
        ));
    }

    @Override
    public OutputStream getOutput(OutputStream outputStream) throws Throwable {
        return new DecryptOutputStream(outputStream, this.key, CryptoParameter.buildCfbCryptoParameter(
                CryptoParameter.CipherClass.JCE, null
        ));
    }

    private byte[] getKey(byte[] serverKey, byte[] salt) {
        byte[] result = new byte[salt.length * 2 + serverKey.length];
        System.arraycopy(salt, 0, result, 0, salt.length);
        System.arraycopy(serverKey, 0, result, salt.length, serverKey.length);
        System.arraycopy(salt, 0, result, salt.length + serverKey.length, salt.length);
        return DigestHelper.digestBuff(result, "MD5");
    }

    @Override
    public void init(byte[] key, FilepodMetaInf metaInf) {
        if (metaInf instanceof FileDepotMetaInf) {
            this.key = getKey(key, ((FileDepotMetaInf) metaInf).getSalt().getBytes(StandardCharsets.UTF_8));
        } else {
            throw new RuntimeException("Illegal type of file meta info, expect "
                    + FileDepotMetaInf.class.getName() + " but " + metaInf.getClass().getName());
        }
    }
}
