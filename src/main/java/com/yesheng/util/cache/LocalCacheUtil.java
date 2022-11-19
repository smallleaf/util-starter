package com.yesheng.util.cache;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @Author:
 * @Date:
 * guavacache创建工具
 */
public class LocalCacheUtil {


    private final static int DEFAULT_THREADS = 8;

    private static final Executor DEFAULT_EXECUTOR = Executors.newFixedThreadPool(DEFAULT_THREADS);


    /**
     * 创建本地缓存
     */
    public static <K, V> LoadingCache<K, V> create(Function<K, V> function, long refreshSec, long expireSec) {
        return CacheBuilder.newBuilder()
                .refreshAfterWrite(refreshSec, TimeUnit.SECONDS)
                .expireAfterWrite(expireSec, TimeUnit.SECONDS)
                .build(createCacheLoader(function, getDefaultExecutor()));
    }

    /**
     * 使用传入的线程池创建本地缓存
     */
    public static <K, V> LoadingCache<K, V> create(Function<K, V> function, long refreshSec, long expireSec, Executor executor) {
        return CacheBuilder.newBuilder()
                .refreshAfterWrite(refreshSec, TimeUnit.SECONDS)
                .expireAfterWrite(expireSec, TimeUnit.SECONDS)
                .build(createCacheLoader(function, executor));
    }

    /**
     * 创建本地缓存
     */
    public static <K, V> LoadingCache<K, V> create(Function<K, V> function, long maxSize, long refreshSec, long expireSec) {
        return CacheBuilder.newBuilder()
                .refreshAfterWrite(refreshSec, TimeUnit.SECONDS)
                .expireAfterWrite(expireSec, TimeUnit.SECONDS)
                .maximumSize(maxSize)
                .build(createCacheLoader(function, getDefaultExecutor()));
    }

    /**
     * 使用传入的线程池创建本地缓存
     */
    public static <K, V> LoadingCache<K, V> create(Function<K, V> function, long maxSize, long refreshSec, long expireSec, Executor executor) {
        return CacheBuilder.newBuilder()
                .refreshAfterWrite(refreshSec, TimeUnit.SECONDS)
                .expireAfterWrite(expireSec, TimeUnit.SECONDS)
                .maximumSize(maxSize)
                .build(createCacheLoader(function, executor));
    }

    private static <K, V> CacheLoader<K, V> createCacheLoader(final Function<K, V> function, Executor executor) {
        return CacheLoader.asyncReloading(new CacheLoader<K, V>() {
            @Override
            public V load(K k) {
                return function.apply(k);
            }

            /**
             * 如果返回了空列表，则不更新本地缓存
             */
            @Override
            public ListenableFuture<V> reload(final K key, final V oldValue) throws Exception {
                Preconditions.checkNotNull(key);
                Preconditions.checkNotNull(oldValue);
                V newvalue = this.load(key);
                if (newvalue == null) {
                    newvalue = oldValue;
                }
                return Futures.immediateFuture(newvalue);
            }
        }, executor);
    }

    /**
     * 公用本地缓存异步刷新线程，避免每个缓存新开线程，导致空闲
     * <p>更新频次很高的本地缓存，建议单独使用一个线程</p>
     */
    public static Executor getDefaultExecutor() {
        return DEFAULT_EXECUTOR;
    }
}
