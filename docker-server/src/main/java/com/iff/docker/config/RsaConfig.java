/*******************************************************************************
 * Copyright (c) 2020-06-05 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package com.iff.docker.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * RsaConfig
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2020-06-05
 */
@Slf4j
@Data
@Configuration
public class RsaConfig {
    @Value("${config.docker.rsa.pri-key:}")
    private String priKey;
    @Value("${config.docker.rsa.pub-key}")
    private String pubKey;
}
