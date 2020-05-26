package org.baiyi.javaFxTool.utils;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadUtil {

    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(3, 10, 0, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<Runnable>(1024));

    public static ThreadPoolExecutor getThreadPoolExecutor(){
        return threadPoolExecutor;
    }

}
