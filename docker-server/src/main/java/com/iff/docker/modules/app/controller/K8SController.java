/*******************************************************************************
 * Copyright (c) 2020-05-18 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package com.iff.docker.modules.app.controller;

import com.iff.docker.modules.app.entity.User;
import com.iff.docker.modules.app.service.K8SService;
import com.iff.docker.modules.app.vo.form.CommonQueryListFormVO;
import com.iff.docker.modules.common.BaseController;
import com.iff.docker.modules.common.Constant;
import com.iff.docker.modules.common.ResultBean;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiResponse;
import io.kubernetes.client.openapi.apis.*;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

/**
 * K8SController
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2020-05-18
 */
@Slf4j
@RestController
@RequestMapping(path = "/k8s", produces = Constant.JSON_UTF8)
public class K8SController extends BaseController {
    @Autowired
    K8SService k8SService;

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

    ResultBean execute(Call call) {
        try {
            ApiResponse<String> execute = apiClient.execute(call, String.class);
            return success(execute.getData());
        } catch (Exception e) {
            String message = call == null ? "K8S Call is null" : call.toString();
            log.warn(message, e);
            return error(message, e);
        }
    }

    @GetMapping(path = "/listClusterRole")
    public ResultBean listClusterRole(CommonQueryListFormVO form, @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return success(execute(rbacAuthorizationV1Api
                .listClusterRoleCall(form.getPretty(), form.getAllowWatchBookmarks(), form.get_continue(), form.getFieldSelector(), form.getLabelSelector(), form.getLimit(), form.getResourceVersion(), form.getTimeoutSeconds(), form.getWatch(), null)
        ));
    }

    @GetMapping("/listClusterRole/{name}")
    public ResultBean readClusterRoleRaw(@PathVariable("name") String name, @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return success(execute(rbacAuthorizationV1Api
                .readClusterRoleCall(name, null, null)
        ));
    }

    @GetMapping("/listNamespace")
    public ResultBean listNamespace(CommonQueryListFormVO form, @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return success(execute(coreV1Api
                .listNamespaceCall(form.getPretty(), form.getAllowWatchBookmarks(), form.get_continue(), form.getFieldSelector(), form.getLabelSelector(), form.getLimit(), form.getResourceVersion(), form.getTimeoutSeconds(), form.getWatch(), null)
        ));
    }

    @GetMapping("/listNamespace/{name}")
    public ResultBean readNamespace(@PathVariable("name") String name, @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return success(execute(coreV1Api
                .readNamespaceCall(name, null, null, null, null)
        ));
    }

    @GetMapping("/listNode")
    public ResultBean listNode(CommonQueryListFormVO form, @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return success(execute(coreV1Api
                .listNodeCall(form.getPretty(), form.getAllowWatchBookmarks(), form.get_continue(), form.getFieldSelector(), form.getLabelSelector(), form.getLimit(), form.getResourceVersion(), form.getTimeoutSeconds(), form.getWatch(), null)
        ));
    }

    @GetMapping("/listNode/{name}")
    public ResultBean readNode(@PathVariable("name") String name, @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return success(execute(coreV1Api
                .readNodeCall(name, null, null, null, null)
        ));
    }

    @GetMapping("/listPersistentVolume")
    public ResultBean listPersistentVolume(CommonQueryListFormVO form, @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return success(execute(coreV1Api
                .listPersistentVolumeCall(form.getPretty(), form.getAllowWatchBookmarks(), form.get_continue(), form.getFieldSelector(), form.getLabelSelector(), form.getLimit(), form.getResourceVersion(), form.getTimeoutSeconds(), form.getWatch(), null)
        ));
    }

    @GetMapping("/listPersistentVolume/{name}")
    public ResultBean readPersistentVolume(@PathVariable("name") String name, @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return success(execute(coreV1Api
                .readPersistentVolumeCall(name, null, null, null, null)
        ));
    }

    @GetMapping("/listStorageClass")
    public ResultBean listStorageClass(CommonQueryListFormVO form, @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return success(execute(storageV1Api
                .listStorageClassCall(form.getPretty(), form.getAllowWatchBookmarks(), form.get_continue(), form.getFieldSelector(), form.getLabelSelector(), form.getLimit(), form.getResourceVersion(), form.getTimeoutSeconds(), form.getWatch(), null)
        ));
    }

    @GetMapping("/listStorageClass/{name}")
    public ResultBean readStorageClass(@PathVariable("name") String name, @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return success(execute(storageV1Api
                .readStorageClassCall(name, null, null, null, null)
        ));
    }

    @GetMapping("/listNamespacedCronJob")
    public ResultBean listNamespacedCronJob(CommonQueryListFormVO form, @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return success(execute(batchV1beta1Api
                .listNamespacedCronJobCall(form.getNamespace(), form.getPretty(), form.getAllowWatchBookmarks(), form.get_continue(), form.getFieldSelector(), form.getLabelSelector(), form.getLimit(), form.getResourceVersion(), form.getTimeoutSeconds(), form.getWatch(), null)
        ));
    }

    @GetMapping("/listNamespacedCronJob/{namespace}/{name}")
    public ResultBean readNamespacedCronJob(@PathVariable("namespace") String namespace, @PathVariable("name") String name, @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return success(execute(batchV1beta1Api
                .readNamespacedCronJobCall(name, namespace, null, null, null, null)
        ));
    }

    @GetMapping("/listDaemonSet")
    public ResultBean listDaemonSet(CommonQueryListFormVO form, @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        if (StringUtils.isNotBlank(form.getNamespace())) {
            return success(execute(appsV1Api
                    .listNamespacedDaemonSetCall(form.getNamespace(), form.getPretty(), form.getAllowWatchBookmarks(), form.get_continue(), form.getFieldSelector(), form.getLabelSelector(), form.getLimit(), form.getResourceVersion(), form.getTimeoutSeconds(), form.getWatch(), null)
            ));
        } else {
            return success(execute(appsV1Api
                    .listDaemonSetForAllNamespacesCall(form.getAllowWatchBookmarks(), form.get_continue(), form.getFieldSelector(), form.getLabelSelector(), form.getLimit(), null, null, null, null, null)
            ));
        }
    }

    @GetMapping("/listDaemonSet/{namespace}")
    public ResultBean listDaemonSet(@PathVariable("namespace") String namespace, CommonQueryListFormVO form, @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return success(execute(appsV1Api
                .listNamespacedDaemonSetCall(namespace, form.getPretty(), form.getAllowWatchBookmarks(), form.get_continue(), form.getFieldSelector(), form.getLabelSelector(), form.getLimit(), form.getResourceVersion(), form.getTimeoutSeconds(), form.getWatch(), null)
        ));
    }

    @GetMapping("/listDaemonSet/{namespace}/{name}")
    public ResultBean readDaemonSet(@PathVariable("namespace") String namespace, @PathVariable("name") String name, @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return success(execute(appsV1Api
                .readNamespacedDaemonSetCall(name, namespace, null, null, null, null)
        ));
    }

    @GetMapping("/listDeployment")
    public ResultBean listDeployment(CommonQueryListFormVO form, @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        if (StringUtils.isNotBlank(form.getNamespace())) {
            return success(execute(appsV1Api
                    .listNamespacedDeploymentCall(form.getNamespace(), form.getPretty(), form.getAllowWatchBookmarks(), form.get_continue(), form.getFieldSelector(), form.getLabelSelector(), form.getLimit(), form.getResourceVersion(), form.getTimeoutSeconds(), form.getWatch(), null)
            ));
        } else {
            return success(execute(appsV1Api
                    .listDeploymentForAllNamespacesCall(form.getAllowWatchBookmarks(), form.get_continue(), form.getFieldSelector(), form.getLabelSelector(), form.getLimit(), null, null, null, null, null)
            ));
        }
    }

    @GetMapping("/listDeployment/{namespace}")
    public ResultBean listDeployment(@PathVariable("namespace") String namespace, CommonQueryListFormVO form, @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return success(execute(appsV1Api
                .listNamespacedDeploymentCall(namespace, form.getPretty(), form.getAllowWatchBookmarks(), form.get_continue(), form.getFieldSelector(), form.getLabelSelector(), form.getLimit(), form.getResourceVersion(), form.getTimeoutSeconds(), form.getWatch(), null)
        ));
    }

    @GetMapping("/listDeployment/{namespace}/{name}")
    public ResultBean readDeployment(@PathVariable("namespace") String namespace, @PathVariable("name") String name, @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return success(execute(appsV1Api
                .readNamespacedDeploymentCall(name, namespace, null, null, null, null)
        ));
    }

    @GetMapping("/listJob")
    public ResultBean listJob(CommonQueryListFormVO form, @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        if (StringUtils.isNotBlank(form.getNamespace())) {
            return success(execute(batchV1Api
                    .listNamespacedJobCall(form.getNamespace(), form.getPretty(), form.getAllowWatchBookmarks(), form.get_continue(), form.getFieldSelector(), form.getLabelSelector(), form.getLimit(), form.getResourceVersion(), form.getTimeoutSeconds(), form.getWatch(), null)
            ));
        } else {
            return success(execute(batchV1Api
                    .listJobForAllNamespacesCall(form.getAllowWatchBookmarks(), form.get_continue(), form.getFieldSelector(), form.getLabelSelector(), form.getLimit(), null, null, null, null, null)
            ));
        }
    }

    @GetMapping("/listJob/{namespace}")
    public ResultBean listJob(@PathVariable("namespace") String namespace, CommonQueryListFormVO form, @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return success(execute(batchV1Api
                .listNamespacedJobCall(namespace, form.getPretty(), form.getAllowWatchBookmarks(), form.get_continue(), form.getFieldSelector(), form.getLabelSelector(), form.getLimit(), form.getResourceVersion(), form.getTimeoutSeconds(), form.getWatch(), null)
        ));
    }

    @GetMapping("/listJob/{namespace}/{name}")
    public ResultBean readJob(@PathVariable("namespace") String namespace, @PathVariable("name") String name, @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return success(execute(batchV1Api
                .readNamespacedJobCall(name, namespace, null, null, null, null)
        ));
    }

    @GetMapping("/listPod")
    public ResultBean listPod(CommonQueryListFormVO form, @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        if (StringUtils.isNotBlank(form.getNamespace())) {
            return success(execute(coreV1Api
                    .listNamespacedPodCall(form.getNamespace(), form.getPretty(), form.getAllowWatchBookmarks(), form.get_continue(), form.getFieldSelector(), form.getLabelSelector(), form.getLimit(), form.getResourceVersion(), form.getTimeoutSeconds(), form.getWatch(), null)
            ));
        } else {
            return success(execute(coreV1Api
                    .listPodForAllNamespacesCall(form.getAllowWatchBookmarks(), form.get_continue(), form.getFieldSelector(), form.getLabelSelector(), form.getLimit(), null, null, null, null, null)
            ));
        }
    }

    @GetMapping("/listPod/{namespace}")
    public ResultBean listPod(@PathVariable("namespace") String namespace, CommonQueryListFormVO form, @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return success(execute(coreV1Api
                .listNamespacedPodCall(namespace, form.getPretty(), form.getAllowWatchBookmarks(), form.get_continue(), form.getFieldSelector(), form.getLabelSelector(), form.getLimit(), form.getResourceVersion(), form.getTimeoutSeconds(), form.getWatch(), null)
        ));
    }

    @GetMapping("/listPod/{namespace}/{name}")
    public ResultBean readPod(@PathVariable("namespace") String namespace, @PathVariable("name") String name, @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return success(execute(coreV1Api
                .readNamespacedPodCall(name, namespace, null, null, null, null)
        ));
    }

    @GetMapping("/listReplicaSet")
    public ResultBean listReplicaSet(CommonQueryListFormVO form, @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        if (StringUtils.isNotBlank(form.getNamespace())) {
            return success(execute(appsV1Api
                    .listNamespacedReplicaSetCall(form.getNamespace(), form.getPretty(), form.getAllowWatchBookmarks(), form.get_continue(), form.getFieldSelector(), form.getLabelSelector(), form.getLimit(), form.getResourceVersion(), form.getTimeoutSeconds(), form.getWatch(), null)
            ));
        } else {
            return success(execute(appsV1Api
                    .listReplicaSetForAllNamespacesCall(form.getAllowWatchBookmarks(), form.get_continue(), form.getFieldSelector(), form.getLabelSelector(), form.getLimit(), null, null, null, null, null)
            ));
        }
    }

    @GetMapping("/listReplicaSet/{namespace}")
    public ResultBean listReplicaSet(@PathVariable("namespace") String namespace, CommonQueryListFormVO form, @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return success(execute(appsV1Api
                .listNamespacedReplicaSetCall(namespace, form.getPretty(), form.getAllowWatchBookmarks(), form.get_continue(), form.getFieldSelector(), form.getLabelSelector(), form.getLimit(), form.getResourceVersion(), form.getTimeoutSeconds(), form.getWatch(), null)
        ));
    }

    @GetMapping("/listReplicaSet/{namespace}/{name}")
    public ResultBean readReplicaSet(@PathVariable("namespace") String namespace, @PathVariable("name") String name, @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return success(execute(appsV1Api
                .readNamespacedReplicaSetCall(name, namespace, null, null, null, null)
        ));
    }

    @GetMapping("/listReplicationController")
    public ResultBean listReplicationController(CommonQueryListFormVO form, @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        if (StringUtils.isNotBlank(form.getNamespace())) {
            return success(execute(coreV1Api
                    .listNamespacedReplicationControllerCall(form.getNamespace(), form.getPretty(), form.getAllowWatchBookmarks(), form.get_continue(), form.getFieldSelector(), form.getLabelSelector(), form.getLimit(), form.getResourceVersion(), form.getTimeoutSeconds(), form.getWatch(), null)
            ));
        } else {
            return success(execute(coreV1Api
                    .listReplicationControllerForAllNamespacesCall(form.getAllowWatchBookmarks(), form.get_continue(), form.getFieldSelector(), form.getLabelSelector(), form.getLimit(), null, null, null, null, null)
            ));
        }
    }

    @GetMapping("/listReplicationController/{namespace}")
    public ResultBean listReplicationController(@PathVariable("namespace") String namespace, CommonQueryListFormVO form, @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return success(execute(coreV1Api
                .listNamespacedReplicationControllerCall(namespace, form.getPretty(), form.getAllowWatchBookmarks(), form.get_continue(), form.getFieldSelector(), form.getLabelSelector(), form.getLimit(), form.getResourceVersion(), form.getTimeoutSeconds(), form.getWatch(), null)
        ));
    }

    @GetMapping("/listReplicationController/{namespace}/{name}")
    public ResultBean readReplicationController(@PathVariable("namespace") String namespace, @PathVariable("name") String name, @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return success(execute(coreV1Api
                .readNamespacedReplicationControllerCall(name, namespace, null, null, null, null)
        ));
    }

    @GetMapping("/listStatefulSet")
    public ResultBean listStatefulSet(CommonQueryListFormVO form, @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        if (StringUtils.isNotBlank(form.getNamespace())) {
            return success(execute(appsV1Api
                    .listNamespacedStatefulSetCall(form.getNamespace(), form.getPretty(), form.getAllowWatchBookmarks(), form.get_continue(), form.getFieldSelector(), form.getLabelSelector(), form.getLimit(), form.getResourceVersion(), form.getTimeoutSeconds(), form.getWatch(), null)
            ));
        } else {
            return success(execute(appsV1Api
                    .listStatefulSetForAllNamespacesCall(form.getAllowWatchBookmarks(), form.get_continue(), form.getFieldSelector(), form.getLabelSelector(), form.getLimit(), null, null, null, null, null)
            ));
        }
    }

    @GetMapping("/listStatefulSet/{namespace}")
    public ResultBean listStatefulSet(@PathVariable("namespace") String namespace, CommonQueryListFormVO form, @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return success(execute(appsV1Api
                .listNamespacedStatefulSetCall(namespace, form.getPretty(), form.getAllowWatchBookmarks(), form.get_continue(), form.getFieldSelector(), form.getLabelSelector(), form.getLimit(), form.getResourceVersion(), form.getTimeoutSeconds(), form.getWatch(), null)
        ));
    }

    @GetMapping("/listStatefulSet/{namespace}/{name}")
    public ResultBean readStatefulSet(@PathVariable("namespace") String namespace, @PathVariable("name") String name, @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return success(execute(appsV1Api
                .readNamespacedStatefulSetCall(name, namespace, null, null, null, null)
        ));
    }

    @GetMapping("/listIngress")
    public ResultBean listIngress(CommonQueryListFormVO form, @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        if (StringUtils.isNotBlank(form.getNamespace())) {
            return success(execute(extensionsV1beta1Api
                    .listNamespacedIngressCall(form.getNamespace(), form.getPretty(), form.getAllowWatchBookmarks(), form.get_continue(), form.getFieldSelector(), form.getLabelSelector(), form.getLimit(), form.getResourceVersion(), form.getTimeoutSeconds(), form.getWatch(), null)
            ));
        } else {
            return success(execute(extensionsV1beta1Api
                    .listIngressForAllNamespacesCall(form.getAllowWatchBookmarks(), form.get_continue(), form.getFieldSelector(), form.getLabelSelector(), form.getLimit(), null, null, null, null, null)
            ));
        }
    }

    @GetMapping("/listIngress/{namespace}")
    public ResultBean listIngress(@PathVariable("namespace") String namespace, CommonQueryListFormVO form, @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return success(execute(extensionsV1beta1Api
                .listNamespacedIngressCall(namespace, form.getPretty(), form.getAllowWatchBookmarks(), form.get_continue(), form.getFieldSelector(), form.getLabelSelector(), form.getLimit(), form.getResourceVersion(), form.getTimeoutSeconds(), form.getWatch(), null)
        ));
    }

    @GetMapping("/listIngress/{namespace}/{name}")
    public ResultBean readIngress(@PathVariable("namespace") String namespace, @PathVariable("name") String name, @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return success(execute(extensionsV1beta1Api
                .readNamespacedIngressCall(name, namespace, null, null, null, null)
        ));
    }

    @GetMapping("/listService")
    public ResultBean listService(CommonQueryListFormVO form, @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        if (StringUtils.isNotBlank(form.getNamespace())) {
            return success(execute(coreV1Api
                    .listNamespacedServiceCall(form.getNamespace(), form.getPretty(), form.getAllowWatchBookmarks(), form.get_continue(), form.getFieldSelector(), form.getLabelSelector(), form.getLimit(), form.getResourceVersion(), form.getTimeoutSeconds(), form.getWatch(), null)
            ));
        } else {
            return success(execute(coreV1Api
                    .listServiceForAllNamespacesCall(form.getAllowWatchBookmarks(), form.get_continue(), form.getFieldSelector(), form.getLabelSelector(), form.getLimit(), null, null, null, null, null)
            ));
        }
    }

    @GetMapping("/listService/{namespace}")
    public ResultBean listService(@PathVariable("namespace") String namespace, CommonQueryListFormVO form, @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return success(execute(coreV1Api
                .listNamespacedServiceCall(namespace, form.getPretty(), form.getAllowWatchBookmarks(), form.get_continue(), form.getFieldSelector(), form.getLabelSelector(), form.getLimit(), form.getResourceVersion(), form.getTimeoutSeconds(), form.getWatch(), null)
        ));
    }

    @GetMapping("/listService/{namespace}/{name}")
    public ResultBean readService(@PathVariable("namespace") String namespace, @PathVariable("name") String name, @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return success(execute(coreV1Api
                .readNamespacedServiceCall(name, namespace, null, null, null, null)
        ));
    }

    @GetMapping("/listConfigMap")
    public ResultBean listConfigMap(CommonQueryListFormVO form, @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        if (StringUtils.isNotBlank(form.getNamespace())) {
            return success(execute(coreV1Api
                    .listNamespacedConfigMapCall(form.getNamespace(), form.getPretty(), form.getAllowWatchBookmarks(), form.get_continue(), form.getFieldSelector(), form.getLabelSelector(), form.getLimit(), form.getResourceVersion(), form.getTimeoutSeconds(), form.getWatch(), null)
            ));
        } else {
            return success(execute(coreV1Api
                    .listConfigMapForAllNamespacesCall(form.getAllowWatchBookmarks(), form.get_continue(), form.getFieldSelector(), form.getLabelSelector(), form.getLimit(), null, null, null, null, null)
            ));
        }
    }

    @GetMapping("/listConfigMap/{namespace}")
    public ResultBean listConfigMap(@PathVariable("namespace") String namespace, CommonQueryListFormVO form, @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return success(execute(coreV1Api
                .listNamespacedConfigMapCall(namespace, form.getPretty(), form.getAllowWatchBookmarks(), form.get_continue(), form.getFieldSelector(), form.getLabelSelector(), form.getLimit(), form.getResourceVersion(), form.getTimeoutSeconds(), form.getWatch(), null)
        ));
    }

    @GetMapping("/listConfigMap/{namespace}/{name}")
    public ResultBean readConfigMap(@PathVariable("namespace") String namespace, @PathVariable("name") String name, @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return success(execute(coreV1Api
                .readNamespacedConfigMapCall(name, namespace, null, null, null, null)
        ));
    }

    @GetMapping("/listPersistentVolumeClaim")
    public ResultBean listPersistentVolumeClaim(CommonQueryListFormVO form, @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        if (StringUtils.isNotBlank(form.getNamespace())) {
            return success(execute(coreV1Api
                    .listNamespacedPersistentVolumeClaimCall(form.getNamespace(), form.getPretty(), form.getAllowWatchBookmarks(), form.get_continue(), form.getFieldSelector(), form.getLabelSelector(), form.getLimit(), form.getResourceVersion(), form.getTimeoutSeconds(), form.getWatch(), null)
            ));
        } else {
            return success(execute(coreV1Api
                    .listPersistentVolumeClaimForAllNamespacesCall(form.getAllowWatchBookmarks(), form.get_continue(), form.getFieldSelector(), form.getLabelSelector(), form.getLimit(), null, null, null, null, null)
            ));
        }
    }

    @GetMapping("/listPersistentVolumeClaim/{namespace}")
    public ResultBean listPersistentVolumeClaim(@PathVariable("namespace") String namespace, CommonQueryListFormVO form, @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return success(execute(coreV1Api
                .listNamespacedPersistentVolumeClaimCall(namespace, form.getPretty(), form.getAllowWatchBookmarks(), form.get_continue(), form.getFieldSelector(), form.getLabelSelector(), form.getLimit(), form.getResourceVersion(), form.getTimeoutSeconds(), form.getWatch(), null)
        ));
    }

    @GetMapping("/listPersistentVolumeClaim/{namespace}/{name}")
    public ResultBean readPersistentVolumeClaim(@PathVariable("namespace") String namespace, @PathVariable("name") String name, @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return success(execute(coreV1Api
                .readNamespacedPersistentVolumeClaimCall(name, namespace, null, null, null, null)
        ));
    }

    @GetMapping("/listSecret")
    public ResultBean listSecret(CommonQueryListFormVO form, @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        if (StringUtils.isNotBlank(form.getNamespace())) {
            return success(execute(coreV1Api
                    .listNamespacedSecretCall(form.getNamespace(), form.getPretty(), form.getAllowWatchBookmarks(), form.get_continue(), form.getFieldSelector(), form.getLabelSelector(), form.getLimit(), form.getResourceVersion(), form.getTimeoutSeconds(), form.getWatch(), null)
            ));
        } else {
            return success(execute(coreV1Api
                    .listSecretForAllNamespacesCall(form.getAllowWatchBookmarks(), form.get_continue(), form.getFieldSelector(), form.getLabelSelector(), form.getLimit(), null, null, null, null, null)
            ));
        }
    }

    @GetMapping("/listSecret/{namespace}")
    public ResultBean listSecret(@PathVariable("namespace") String namespace, CommonQueryListFormVO form, @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return success(execute(coreV1Api
                .listNamespacedSecretCall(namespace, form.getPretty(), form.getAllowWatchBookmarks(), form.get_continue(), form.getFieldSelector(), form.getLabelSelector(), form.getLimit(), form.getResourceVersion(), form.getTimeoutSeconds(), form.getWatch(), null)
        ));
    }

    @GetMapping("/listSecret/{namespace}/{name}")
    public ResultBean readSecret(@PathVariable("namespace") String namespace, @PathVariable("name") String name, @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return success(execute(coreV1Api
                .readNamespacedSecretCall(name, namespace, null, null, null, null)
        ));
    }

    @GetMapping("/listCustomResourceDefinition")
    public ResultBean listCustomResourceDefinition(CommonQueryListFormVO form, @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return success(execute(apiextensionsV1Api
                .listCustomResourceDefinitionCall(form.getPretty(), form.getAllowWatchBookmarks(), form.get_continue(), form.getFieldSelector(), form.getLabelSelector(), form.getLimit(), form.getResourceVersion(), form.getTimeoutSeconds(), form.getWatch(), null)
        ));
    }

    @GetMapping("/listCustomResourceDefinition/{namespace}/{name}")
    public ResultBean readCustomResourceDefinition(@PathVariable("name") String name, @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return success(execute(apiextensionsV1Api
                .readCustomResourceDefinitionCall(name, null, null, null, null)
        ));
    }
}
