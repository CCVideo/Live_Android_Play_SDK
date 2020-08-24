package com.bokecc.dwlivedemo.download;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPoolUtils {
    private static final String TAG = "ThreadPoolManager";

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = (CPU_COUNT + 1)>3?3:(CPU_COUNT + 1);
    //    private static final int CORE_POOL_SIZE = 3;
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final int KEEP_ALIVE = 1;

    private static final BlockingQueue<Runnable> sPoolWorkQueue =
            new LinkedBlockingQueue<>(128);

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        //一个线程安全的Int类
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "ThreadPoolManager #" + mCount.getAndIncrement());
        }
    };
    /**
     * 单例模式
     */
    private static ThreadPoolUtils manager;
    /**
     * 线程池对象
     */
    private ThreadPoolExecutor executor;

    /**
     * 私有构造，防止被初始化
     */
    private ThreadPoolUtils() {
    }

    /**
     * 对外单例
     *
     * @return manager 操作类
     */
    public static ThreadPoolUtils getInstance() {
        if (manager == null) {
            synchronized (ThreadPoolUtils.class) {
                if (manager == null) {
                    manager = new ThreadPoolUtils();
                }
            }
        }
        return manager;
    }

    /**
     * 执行
     *
     * @param r 执行对象
     */
    public void execute(Runnable r) {
        //为null时创建executor
        if (executor == null) {
            executor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE,
                    TimeUnit.MINUTES, sPoolWorkQueue, sThreadFactory) {
            };
        }
        //执行
        executor.submit(r);
    }

    /**
     * 把线程从线程池中移除
     *
     * @param r 移除的对象
     */
    public void cancel(Runnable r) {
        if (executor != null) {
            executor.getQueue().remove(r);
        }
    }

    /**
     * 对外提供 ThreadPoolExecutor
     * @return executor
     */
    public ThreadPoolExecutor getThreadPool() {
        return executor;
    }
    public void destroy(){
        executor.shutdownNow();
        executor = null;
    }
}
