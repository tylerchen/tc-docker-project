/*******************************************************************************
 * Copyright (c) 2020-05-18 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package com.iff.docker.modules.app.service;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * K8SService
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2020-05-18
 */
@Slf4j
@Service
public class K8SService {

    @Autowired
    ApiClient apiClient;

    @Autowired
    CoreV1Api coreV1Api;

    @Autowired
    RbacAuthorizationV1Api rbacAuthorizationV1Api;

    @Autowired
    StorageV1Api storageV1Api;

    @Autowired
    BatchV1beta1Api batchV1beta1Api;

    @Autowired
    AppsV1Api appsV1Api;

    @Autowired
    BatchV1Api batchV1Api;

    @Autowired
    ExtensionsV1beta1Api extensionsV1beta1Api;

    @Autowired
    ApiextensionsV1Api apiextensionsV1Api;

}
