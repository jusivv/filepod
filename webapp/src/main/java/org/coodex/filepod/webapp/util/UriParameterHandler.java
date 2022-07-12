package org.coodex.filepod.webapp.util;

/**
 * MatrixParameterHandler
 */
@FunctionalInterface
public interface UriParameterHandler {
    /**
     * method to deal with uri parameter
     * @param index     parameter index, start from 1
     * @param parameter url parameter, matrix like 'key=value' or path like 'value'
     * @return          continue or break
     */
    boolean handle(int index, String parameter);
}
