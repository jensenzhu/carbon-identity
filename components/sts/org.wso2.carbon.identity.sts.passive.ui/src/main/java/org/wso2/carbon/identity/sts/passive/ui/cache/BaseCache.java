/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.sts.passive.ui.cache;

import javax.cache.Cache;
import javax.cache.CacheBuilder;
import javax.cache.CacheConfiguration;
import javax.cache.CacheManager;
import javax.cache.Caching;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * A base class for all cache implementations in SAML SSO Module.
 */
public class BaseCache<K extends Serializable, V extends Serializable> {

    private static final String PASSIVESTS_CACHE_MANAGER = "PassiveSTSCacheManager";
    private CacheBuilder<K, V> cacheBuilder;
    private String cacheName;
    private int cacheTimeout;

    public BaseCache(String cacheName) {
        this.cacheName = cacheName;
        this.cacheTimeout = -1;
    }

    public BaseCache(String cacheName, int timeout) {
        this.cacheName = cacheName;

        if (timeout > 0) {
            this.cacheTimeout = timeout;
        } else {
            this.cacheTimeout = -1;
        }
    }

    private Cache<K, V> getBaseCache() {

        Cache<K, V> cache = null;
        CacheManager cacheManager = Caching.getCacheManagerFactory().getCacheManager(PASSIVESTS_CACHE_MANAGER);

        if (cacheTimeout > 0) {

            if (cacheBuilder == null) {
                cacheManager.removeCache(cacheName);
                cacheBuilder = cacheManager.<K, V>createCacheBuilder(cacheName).
                        setExpiry(CacheConfiguration.ExpiryType.ACCESSED,
                                  new CacheConfiguration.Duration(TimeUnit.SECONDS, cacheTimeout)).
                                                   setStoreByValue(false);
                cache = cacheBuilder.build();
            } else {
                cache = cacheManager.getCache(cacheName);
            }
        } else {
            cache = cacheManager.getCache(cacheName);
        }

        return cache;
    }

    /**
     * Add a cache entry.
     *
     * @param key   Key which cache entry is indexed.
     * @param entry Actual object where cache entry is placed.
     */
    public void addToCache(K key, V entry) {
        // Element already in the cache. Remove it first
        clearCacheEntry(key);

        Cache<K, V> cache = getBaseCache();
        if (cache != null) {
            cache.put(key, entry);
        }
    }

    /**
     * Retrieves a cache entry.
     *
     * @param key CacheKey
     * @return Cached entry.
     */
    public V getValueFromCache(K key) {
        Cache<K, V> cache = getBaseCache();
        if (cache != null && cache.containsKey(key)) {
            return (V) cache.get(key);
        }
        return null;
    }

    /**
     * Clears a cache entry.
     *
     * @param key Key to clear cache.
     */
    public void clearCacheEntry(K key) {
        Cache<K, V> cache = getBaseCache();
        if (cache != null && cache.containsKey(key)) {
            cache.remove(key);
        }
    }

    /**
     * Remove everything in the cache.
     */
    public void clear() {
        Cache<K, V> cache = getBaseCache();
        if (cache != null) {
            cache.removeAll();
        }
    }
}
