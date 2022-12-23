package com.unitechstudio.voicenotification.utils.image;


import androidx.collection.LruCache;

/**
 * Created by Long Uni on 4/14/2017.
 */

public class Cache {

    private static Cache instance;
    private LruCache<Object, Object> lru;

    //------------------------------------------------------------------------//
    private Cache() {
        lru = new LruCache<>(5 * 1024 * 1024); //Max is 5MB;
    }

    //------------------------------------------------------------------------//
    public static Cache getInstance() {
        if (instance == null) {
            instance = new Cache();
        }
        return instance;
    }

    //------------------------------------------------------------------------//
    public LruCache<Object, Object> getLru() {
        return lru;
    }
}