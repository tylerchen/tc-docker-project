/*******************************************************************************
 * Copyright (c) 2020-05-18 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package com.iff.docker.config;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.*;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.InputStreamReader;

/**
 * K8SConfig
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2020-05-18
 */
@Slf4j
@Configuration
public class K8SConfig {

    @Autowired
    ResourceLoader resourceLoader;

    @Bean
    public ApiClient apiClient() throws Exception {
        Resource resource = resourceLoader.getResource("classpath:k8sconfig");
        //加载k8s, config
        ApiClient client = ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new InputStreamReader(resource.getInputStream()))).build();
        //将加载config的client设置为默认的client
        io.kubernetes.client.openapi.Configuration.setDefaultApiClient(client);
        return client;
    }

    @Bean
    public CoreV1Api coreV1Api(@Autowired ApiClient apiClient) {
        return new CoreV1Api(apiClient);
    }

    @Bean
    public RbacAuthorizationV1Api rbacAuthorizationV1Api(@Autowired ApiClient apiClient) {
        return new RbacAuthorizationV1Api(apiClient);
    }

    @Bean
    public StorageV1Api storageV1Api(@Autowired ApiClient apiClient) {
        return new StorageV1Api(apiClient);
    }

    @Bean
    public BatchV1beta1Api batchV1beta1Api(@Autowired ApiClient apiClient) {
        return new BatchV1beta1Api(apiClient);
    }

    @Bean
    public AppsV1Api appsV1Api(@Autowired ApiClient apiClient) {
        return new AppsV1Api(apiClient);
    }

    @Bean
    public BatchV1Api batchV1Api(@Autowired ApiClient apiClient) {
        return new BatchV1Api(apiClient);
    }

    @Bean
    public ExtensionsV1beta1Api extensionsV1beta1Api(@Autowired ApiClient apiClient) {
        return new ExtensionsV1beta1Api(apiClient);
    }

    @Bean
    public ApiextensionsV1Api apiextensionsV1Api(@Autowired ApiClient apiClient) {
        return new ApiextensionsV1Api(apiClient);
    }
}
