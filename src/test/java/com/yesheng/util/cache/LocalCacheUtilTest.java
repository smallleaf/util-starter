package com.yesheng.util.cache;

import com.google.common.cache.LoadingCache;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

/**
 * @Description
 * @Date 2022年11月19日
 * @Created by yesheng
 */
public class LocalCacheUtilTest {


    /**
     * 本例子可以看出，查询数据的时候报了InvalidCacheLoadException异常
     * 如果我们在使用的时候异常cache类没有处理好，查不到数据会报错，从而影响业务
     */
    @Test
    public void testInvalidCacheLoadException(){
        LoadingCache<Integer,Integer> loadingCache = LocalCacheUtil.create((key)-> null,10,10);
        try {
            loadingCache.get(0);
        } catch (ExecutionException e) {
            e.printStackTrace();
            System.out.println("ExecutionException异常");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception");
        }

    }
}
