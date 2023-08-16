package org.coodex.filepod.webapp.util;

import com.alibaba.fastjson.JSON;
import org.coodex.filepod.webapp.config.EnvSettingsGetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AsyncServletBuilder {
    public static final String ARG_ASYNC_TIMEOUT = "AsyncTimeout";
    private static Logger log = LoggerFactory.getLogger(AsyncServletBuilder.class);
    public static Runnable build(RunnableHttpServlet servlet, AsyncContext asyncContext) {
        String asyncTimeout = EnvSettingsGetter.getValue(ARG_ASYNC_TIMEOUT);
        if (asyncTimeout != null) {
            try {
                asyncContext.setTimeout(Integer.parseInt(asyncTimeout) * 1000);
            } catch (NumberFormatException e) {
                // do nothing
            }
        }
        return new Runnable() {
            @Override
            public void run() {
                HttpServletRequest request = (HttpServletRequest) asyncContext.getRequest();
                HttpServletResponse response = (HttpServletResponse) asyncContext.getResponse();
                response.setContentType("application/json; charset=UTF8");
                try {
                    servlet.run(request, response);
                } catch (Throwable t) {
                    log.error(t.getLocalizedMessage(), t);
                    CommonResponse commonResponse = new CommonResponse();
                    if (t instanceof FilepodServletException) {
                        commonResponse.setCode(((FilepodServletException) t).getCode());
                    } else {
                        commonResponse.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    }
                    commonResponse.setMessage(t.getLocalizedMessage());
                    response.setStatus(commonResponse.getCode());
                    try {
                        response.getOutputStream().println(JSON.toJSONString(commonResponse));
                    } catch (IOException ex) {
                        log.error(ex.getLocalizedMessage(), ex);
                    }
                }
                asyncContext.complete();
            }
        };
    }
}
