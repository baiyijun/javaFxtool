package org.baiyi.javaFxTool.utils;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Copyright:http://www.boc.com.cn All rights reserved.
 *
 * @author:huangshuren
 * @date:2012-8-28
 * @Description: 保留applicationContext实例的引用
 * 实现 ApplicationContextAware，可设置context上下文用于应用中使用Spring
 * 实现BeanPostProcessor，可改变Bean的加载顺序，优先加载实现该接口的类
 */
@Component
public class ApplicationContextHolder implements ApplicationContextAware, BeanPostProcessor {

    /**
     * Spring上下文
     */
    private static ApplicationContext applicationContext;

    /**
     * @return
     * @Description: 获取 ApplicationContext
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * @param <T>
     * @param name
     * @return
     * @Description: 获取Bean
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) {
        return (T) applicationContext.getBean(name);
    }

    /**
     * @param <T>
     * @return
     * @Description: 获取Bean
     */
    public static <T> T getBean(Class<T> clazz) {
        return (T) applicationContext.getBean(clazz);
    }

    /**
     * @param <T>
     * @param name
     * @return
     * @Description: 获取Bean
     */
    public static <T> T getBean(Class<T> clazz, String name) {
        return (T) applicationContext.getBean(name, clazz);
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }

    @Override
    public Object postProcessAfterInitialization(Object arg0, String arg1)
            throws BeansException {
        //
        return arg0;
    }

    @Override
    public Object postProcessBeforeInitialization(Object arg0, String arg1)
            throws BeansException {
        //
        return arg0;
    }
}
