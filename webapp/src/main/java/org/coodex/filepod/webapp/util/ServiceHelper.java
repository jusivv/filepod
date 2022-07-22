package org.coodex.filepod.webapp.util;

import org.coodex.filepod.api.IProviderSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.function.Consumer;

public class ServiceHelper {
    private static Logger log = LoggerFactory.getLogger(ServiceHelper.class);

    public static <T extends IProviderSelector> T getProvider(String tag, Class<T> clazz) {
        ServiceLoader<T> serviceLoader = ServiceLoader.load(clazz);
        for (Iterator<T> it = serviceLoader.iterator(); it.hasNext(); ) {
            T provider = it.next();
            if (provider.accept(tag)) {
                log.debug("hit provider: {} for class: {}", provider.getClass().getName(), clazz.getName());
                return provider;
            }
        }
        log.error("no provider accept tag: {} for class: {}", tag, clazz.getName());
        throw new RuntimeException("no provider accept tag: " + tag + " for class: " + clazz.getName());
    }

    public static <T extends IProviderSelector> void iterateProvider(String tag, Class<T> clazz,
                                                                     Consumer<T> consumer) {
        ServiceLoader<T> serviceLoader = ServiceLoader.load(clazz);
        for (Iterator<T> it = serviceLoader.iterator(); it.hasNext(); ) {
            T provider = it.next();
            if (provider.accept(tag)) {
                log.debug("hit provider: {} for class: {}", provider.getClass().getName(), clazz.getName());
                consumer.accept(provider);
            }
        }
    }

}
