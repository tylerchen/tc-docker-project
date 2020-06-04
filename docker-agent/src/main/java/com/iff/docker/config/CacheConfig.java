/*******************************************************************************
 * Copyright (c) 2019-10-30 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package com.iff.docker.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * CacheConfig
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-10-30
 * auto generate by qdp.
 */
@Configuration
@EnableCaching
public class CacheConfig {
    @Bean("_init_cache_setTransactionAware_")
    public String cacheManager(@Autowired CacheManager cacheManager) {
        if (cacheManager instanceof JCacheCacheManager) {
            ((JCacheCacheManager) cacheManager).setTransactionAware(true);
        }
        return "_init_cache_setTransactionAware_";
    }

}
