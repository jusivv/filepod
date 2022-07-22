package org.coodex.filepod.api;

import org.coodex.filepod.config.ClientConfigGetter;

/**
 * access controller interface
 */
public interface IAccessController extends IProviderSelector {
    /**
     * load configuration
     * @param clientConfigGetter
     */
    void load(ClientConfigGetter clientConfigGetter);

    /**
     * whether the client can write
     * @param clientId  client identifier
     * @param token     client token
     * @return          allow writing or not
     */
    boolean writable(String clientId, String token);

    /**
     * whether the client can read
     * @param clientId  client identifier
     * @param token     client token
     * @param fileId    file identifier
     * @return          allow reading or not
     */
    boolean readable(String clientId, String token, String fileId);

    /**
     * whether the client can access the file that belong to a specified owner
     * @param clientId  client identifier
     * @param fileOwner specified file owner
     * @return          allow accessing or not
     */
    boolean inScope(String clientId, String fileOwner);

    /**
     * whether the client can delete a file
     * @param clientId  client identifier
     * @param token     client token
     * @param fileId    file identifier
     * @return          allow deleting or not
     */
    boolean deletable(String clientId, String token, String fileId);

    /**
     * notify client when finish uploading
     * @param clientId  client identifier
     * @param token     client token
     * @param fileId    file identifier
     * @return          notify success or not
     */
    boolean notify(String clientId, String token, String fileId);
}
