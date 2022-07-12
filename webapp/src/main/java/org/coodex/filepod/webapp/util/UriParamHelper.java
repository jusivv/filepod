package org.coodex.filepod.webapp.util;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

/**
 * uri parameter helper
 * get path parameter or matrix parameter
 */
public class UriParamHelper {
    private static final String URI_ENCODING = "UTF-8";
    private static final String PATH_SPLITTER = "/";
    private static final String MATRIX_SPLITTER = ";";

    /**
     * get path parameters
     * @param request   HttpServletRequest
     * @return          path array
     * @throws UnsupportedEncodingException
     */
    public static String[] getPathParameters(HttpServletRequest request) throws UnsupportedEncodingException {
        String pathInfo = URLDecoder.decode(request.getPathInfo(), URI_ENCODING);
        return pathInfo.split(PATH_SPLITTER);
    }

    /**
     * path iterator
     * @param request   HttpServletRequest
     * @param handler   uri parameter handler
     * @throws UnsupportedEncodingException
     */
    public static void pathIterator(HttpServletRequest request, UriParameterHandler handler)
            throws UnsupportedEncodingException {
        String[] paths = getPathParameters(request);
        for (int i = 0; i < paths.length; i++) {
            if (i > 0) {
                handler.handle(i, paths[i]);
            }
        }
    }

    /**
     * get path parameter
     * @param index     path index start from 1
     * @param request   HttpServletRequest
     * @return          value, null when outbound
     * @throws UnsupportedEncodingException
     */
    public static String getPathParameter(int index, HttpServletRequest request) throws UnsupportedEncodingException {
        String[] paths = getPathParameters(request);
        if (paths.length > index) {
            return paths[index];
        } else {
            return null;
        }
    }

    /**
     * iterate matrix parameters by path
     * @param request   HttpServletRequest
     * @param path      path of matrix parameters, use the last path if null
     * @param handler   MatrixParameterHandler
     * @throws UnsupportedEncodingException
     */
    public static void matrixParameterIterator(HttpServletRequest request, String path, UriParameterHandler handler)
            throws UnsupportedEncodingException {
        List<String> matrixList = new ArrayList<>();
        String reqUri = URLDecoder.decode(request.getRequestURI(), URI_ENCODING);
        String[] pathArray = reqUri.split(PATH_SPLITTER);
        if (path == null) {
            if (pathArray.length > 0) {
                String matrixString = pathArray[pathArray.length - 1];
                int begin = matrixString.indexOf(MATRIX_SPLITTER);
                if (begin != -1) {
                    matrixList.addAll(Arrays.asList(matrixString.substring(begin + 1)
                            .split(MATRIX_SPLITTER)));
                }
            }
        } else {
            final String prefix = path + MATRIX_SPLITTER;
            for (String pathItem : pathArray) {
                if (pathItem.startsWith(prefix)) {
                    matrixList.addAll(Arrays.asList(pathItem.substring(prefix.length()).split(MATRIX_SPLITTER)));
                    break;
                }
            }
        }
        int count = 1;
        for (String item : matrixList) {
            if (!handler.handle(count++, item)) {
                break;
            }
        }
    }

    /**
     *
     * @param path      path of matrix parameters, use the last path if null
     * @param request   HttpServletRequest
     * @return          matrix parameter map of the given path
     * @throws UnsupportedEncodingException
     */
    public static Map<String, String[]> getMatrixParamMap(String path, HttpServletRequest request)
            throws UnsupportedEncodingException {
        final Map<String, String[]> map = new HashMap<>();
        matrixParameterIterator(request, path, (i, item) -> {
            String key = "";
            String value = "";
            int index = item.indexOf('=');
            if (index != -1) {
                key = item.substring(0, index);
                value = item.substring(index + 1);
            } else {
                key = item;
            }
            String[] values = map.get(key);
            if (values == null) {
                values = new String[]{value};
                map.put(key, values);
            } else if (!value.equals("")){
                String[] newValues = Arrays.copyOf(values, values.length + 1);
                newValues[values.length] = value;
                map.put(key, newValues);
            }
            return true;
        });
        return map;
    }

    /**
     * get matrix parameter map
     * @param request   HttpServletRequest
     * @return          matrix parameter map of the last path
     * @throws UnsupportedEncodingException
     */
    public static Map<String, String[]> getMatrixParamMap(HttpServletRequest request)
            throws UnsupportedEncodingException {
        return getMatrixParamMap(null, request);
    }

    /**
     * get matrix parameter value in the given path
     * @param name      parameter name
     * @param path      path
     * @param request   HttpServletRequest
     * @return          parameter value
     * @throws UnsupportedEncodingException
     */
    public static String getMatrixParameterValue(String name, String path, HttpServletRequest request)
            throws UnsupportedEncodingException {
        final String[] value = new String[1];
        String prefix = name + "=";
        matrixParameterIterator(request, path, (index, item) -> {
            if (item.startsWith(prefix)) {
                value[0] = item.replaceFirst(prefix, "");
                return false;
            } else {
                return true;
            }
        });
        return value[0];
    }

    /**
     * get matrix parameter value in the last path
     * @param name      parameter name
     * @param request   HttpServletRequest
     * @return          parameter value
     * @throws UnsupportedEncodingException
     */
    public static String getMatrixParameterValue(String name, HttpServletRequest request)
            throws UnsupportedEncodingException {
        return getMatrixParameterValue(name, null, request);
    }

    /**
     * get matrix parameter values in the given path
     * @param name      parameter name
     * @param path      path
     * @param request   HttpServletRequest
     * @return          parameter values
     * @throws UnsupportedEncodingException
     */
    public static String[] getMatrixParameterValues(String name, String path, HttpServletRequest request)
            throws UnsupportedEncodingException {
        final List<String> list = new ArrayList<>();
        String prefix = name + "=";
        matrixParameterIterator(request, path, (index, item) -> {
            if (item.startsWith(prefix)) {
                list.add(item.replaceFirst(prefix, ""));
            }
            return true;
        });
        return list.toArray(new String[0]);
    }

    /**
     * get matrix parameter values in the last path
     * @param name      parameter name
     * @param request   HttpServletRequest
     * @return          parameter values
     * @throws UnsupportedEncodingException
     */
    public static String[] getMatrixParameterValues(String name, HttpServletRequest request)
            throws UnsupportedEncodingException {
        return getMatrixParameterValues(name, null, request);
    }

}
