package com.gomu.gomustock.ui.simulation;

import static com.gun0912.tedpermission.provider.TedPermissionProvider.context;

import com.gomu.gomustock.portfolio.CacheDBData;

import java.util.List;

public class SCache {
    List<CacheDBData> cacheList;
    SCacheDB cache_db;
    public SCache() {
        cache_db = SCacheDB.getInstance(context);
        cacheList = cache_db.cacheDao().getAll();
    }

    public void clear_cache() {
        cache_db = SCacheDB.getInstance(context);
        cacheList = cache_db.cacheDao().getAll();
        cache_db.cacheDao().clear(cacheList);
    }

    public void update_cache(int input_cache) {
        CacheDBData cachebox = cacheList.get(0);
        cachebox.setRemain(cachebox.remain_cache + input_cache);
        cache_db.cacheDao().update(cachebox);
        cacheList = cache_db.cacheDao().getAll();
    }

    public int getRemainCache() {
        CacheDBData onecache = cacheList.get(0);
        return onecache.remain_cache;
    }
    public int getFirstCache() {
        CacheDBData onecache = cacheList.get(0);
        return onecache.first_cache;
    }

    public void initialize() {
        // 통장에 1억을 넣는다
        //if(cacheList.size() == 0) {
            clear_cache();
            CacheDBData first_cache = new CacheDBData();
            first_cache.setRemain(0); // 매수, 매도에 따라 변하는 액수
            first_cache.setFirstcache(15000000); // 초기투자금이며 update되지 않는다
            cache_db.cacheDao().insert(first_cache);
            cacheList = cache_db.cacheDao().getAll();
            int i=0;
        //}
    }
}
