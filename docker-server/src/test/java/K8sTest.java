/*******************************************************************************
 * Copyright (c) 2020-05-08 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiResponse;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.*;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import io.kubernetes.client.util.Yaml;
import okhttp3.Call;
import org.apache.commons.collections4.MapUtils;

import java.io.FileReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * K8sTest
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2020-05-08
 */
public class K8sTest {
    public static void main(String[] args) throws Exception {
        //直接写config path
        String kubeConfigPath = "/Users/zhaochen/dev/workspace/idea/tc-template2-project/src/main/resources/k8sconfig";

        //加载k8s, config
        ApiClient client = ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();

        //将加载config的client设置为默认的client
        Configuration.setDefaultApiClient(client);

        //创建一个api
        CoreV1Api api = new CoreV1Api();
        {//打印所有的 Cluster Roles
            System.out.println("打印所有的 Cluster Roles");
            RbacAuthorizationV1Api authApi = new RbacAuthorizationV1Api();
            V1ClusterRoleList list = authApi.listClusterRole(null, null, null, null, null, null, null, null, null);
            for (V1ClusterRole item : list.getItems()) {
                System.out.println(item.getMetadata().getName());
            }
        }
        {//打印所有的namespace
            System.out.println("打印所有的namespace");
            V1NamespaceList list = api.listNamespace(null, null, null, null, null, null, null,
                    null, null);
            for (V1Namespace item : list.getItems()) {
                System.out.println(item.getMetadata().getName());
            }
        }
        {//打印所有的 node
            System.out.println("打印所有的node");
            V1NodeList list = api.listNode(null, null, null, null, null, null, null, null, null);
            for (V1Node item : list.getItems()) {
                System.out.println(item.getMetadata().getName());
            }
        }
        {//打印所有的 Persistent Volumes
            System.out.println("打印所有的 Persistent Volumes");
            V1PersistentVolumeList list = api.listPersistentVolume(null, null, null, null, null, null, null, null, null);
            for (V1PersistentVolume item : list.getItems()) {
                System.out.println(item);
            }
        }
        {//打印所有的 Storage Classes
            System.out.println("打印所有的 Storage Classes");
            StorageV1Api storageV1Api = new StorageV1Api();
            V1StorageClassList list = storageV1Api.listStorageClass(null, null, null, null, null, null, null, null, null);
            for (V1StorageClass item : list.getItems()) {
                System.out.println(item);
            }
        }
        {//打印所有的 Cron Jobs
            System.out.println("打印所有的 Cron Jobs");
            BatchV1beta1Api batchV1beta1Api = new BatchV1beta1Api();
            V1beta1CronJobList list = batchV1beta1Api.listNamespacedCronJob("default", null, null, null, null, null, null, null, null, null);
            for (V1beta1CronJob item : list.getItems()) {
                System.out.println(item);
            }
        }
        {//打印所有的 Daemon Sets
            System.out.println("打印所有的 Daemon Sets");
            AppsV1Api appsV1Api = new AppsV1Api();
            V1DaemonSetList list = appsV1Api.listNamespacedDaemonSet("kube-system", null, null, null, null, null, null, null, null, null);
            for (V1DaemonSet item : list.getItems()) {
                System.out.println(item.getMetadata().getName());
            }
        }
        {//打印所有的 Deployments
            System.out.println("打印所有的 Deployments");
            AppsV1Api appsV1Api = new AppsV1Api();
            V1DeploymentList list = appsV1Api.listNamespacedDeployment("kube-system", "true", null, null, null, null, null, null, null, null);
            for (V1Deployment item : list.getItems()) {
                System.out.println(item.getMetadata().getName());
            }
        }
        {//打印所有的 Jobs
            System.out.println("打印所有的 Jobs");
            BatchV1Api batchV1Api = new BatchV1Api();
            V1JobList list = batchV1Api.listNamespacedJob("default", null, null, null, null, null, null, null, null, null);
            for (V1Job item : list.getItems()) {
                System.out.println(item);
            }
        }
        {//打印所有的 pods
            System.out.println("打印所有的 pods");
            V1PodList list = api.listNamespacedPod("default"/*kube-system*/, null, null, null, null, null, null, null, null, null);
            for (V1Pod item : list.getItems()) {
                System.out.println(item.getMetadata().getName());
                System.out.println(Yaml.dump(item));
            }
        }
        {//打印所有的 Replica Sets
            System.out.println("打印所有的 Replica Sets");
            AppsV1Api appsV1Api = new AppsV1Api();
            V1ReplicaSetList list = appsV1Api.listNamespacedReplicaSet("kube-system", null, null, null, null, null, null, null, null, null);
            for (V1ReplicaSet item : list.getItems()) {
                System.out.println(item.getMetadata().getName());
            }
        }
        {//打印所有的 Replications Controllers
            System.out.println("打印所有的 Replications Controllers");
            V1ReplicationControllerList list = api.listNamespacedReplicationController("kube-system", null, null, null, null, null, null, null, null, null);
            for (V1ReplicationController item : list.getItems()) {
                System.out.println(item.getMetadata().getName());
            }
        }
        {//打印所有的 Stateful Sets
            System.out.println("打印所有的 Stateful Sets");
            AppsV1Api appsV1Api = new AppsV1Api();
            V1StatefulSetList list = appsV1Api.listNamespacedStatefulSet("kube-system", null, null, null, null, null, null, null, null, null);
            for (V1StatefulSet item : list.getItems()) {
                System.out.println(item.getMetadata().getName());
            }
        }
        {//打印所有的 Ingresses
            System.out.println("打印所有的 Ingresses");
            ExtensionsV1beta1Api beta1Api = new ExtensionsV1beta1Api();
            ExtensionsV1beta1IngressList list = beta1Api.listNamespacedIngress("kube-system", null, null, null, null, null, null, null, null, null);
            for (ExtensionsV1beta1Ingress item : list.getItems()) {
                System.out.println(item.getMetadata().getName());
            }
        }
        {//打印所有的 Services
            System.out.println("打印所有的 Services");
            V1ServiceList list = api.listNamespacedService("kube-system", null, null, null, null, null, null, null, null, null);
            for (V1Service item : list.getItems()) {
                System.out.println(item.getMetadata().getName());
            }
        }
        {//打印所有的 Config Maps
            System.out.println("打印所有的 Config Maps");
            V1ConfigMapList list = api.listNamespacedConfigMap("kube-system", null, null, null, null, null, null, null, null, null);
            for (V1ConfigMap item : list.getItems()) {
                System.out.println(item.getMetadata().getName());
            }
        }
        {//打印所有的 Persistent Volume Claims
            System.out.println("打印所有的 Persistent Volume Claims");
            V1PersistentVolumeClaimList list = api.listNamespacedPersistentVolumeClaim("kube-system", null, null, null, null, null, null, null, null, null);
            for (V1PersistentVolumeClaim item : list.getItems()) {
                System.out.println(item.getMetadata().getName());
            }
        }
        {//打印所有的 Secrets
            System.out.println("打印所有的 Secrets");
            V1SecretList list = api.listNamespacedSecret("kube-system", null, null, null, null, null, null, null, null, null);
            for (V1Secret item : list.getItems()) {
                System.out.println(item.getMetadata().getName());
            }
        }
        {//打印所有的 Custom Resource Definitions
            System.out.println("打印所有的 Custom Resource Definitions");
            ApiextensionsV1Api apiextensionsV1Api = new ApiextensionsV1Api();
            V1CustomResourceDefinitionList list = apiextensionsV1Api.listCustomResourceDefinition(null, null, null, null, null, null, null, null, null);
            for (V1CustomResourceDefinition item : list.getItems()) {
                System.out.println(item.getMetadata().getName());
            }
        }
        {//打印所有的pod
            V1PodList list = api.listPodForAllNamespaces(null, null, null, null, null, null, null,
                    null, null);

            for (V1Pod item : list.getItems()) {
                System.out.println(item.getMetadata().getName() + "    " + item.getStatus().getPhase() + "    " + item.getStatus().getHostIP() + "    " + item.getStatus().getConditions().get(0).getStatus());
            }
        }
        {
            Call call = api.readNamespacedPodCall("apod","default","true",null,null,null);
            ApiResponse<String> execute = client.execute(call, String.class);
            System.out.println(execute.getData());
        }

        if (1 - 1 == 1) {
            Map<String, String> map = MapUtils.putAll(new HashMap<>(), new String[]{"app", "test"});
            V1Pod pod = new V1PodBuilder()
                    .withNewMetadata()
                    .withName("apod")
                    .withLabels(map)
                    .endMetadata()
                    .withNewSpec()
                    .addNewContainer()
                    .withName("www")
                    .withImage("nginx")
                    .endContainer()
                    .endSpec()
                    .build();

            api.createNamespacedPod("default", pod, null, null, null);

            V1Pod pod2 = new V1Pod()
                    .metadata(new V1ObjectMeta().name("anotherpod").labels(map))
                    .spec(new V1PodSpec()
                            .containers(Arrays.asList(new V1Container().name("www").image("nginx"))));

            api.createNamespacedPod("default", pod2, null, null, null);

            V1PodList list =
                    api.listNamespacedPod("default", null, null, null, null, null, null, null, null, null);
            for (V1Pod item : list.getItems()) {
                System.out.println(item.getMetadata().getName());
            }
        }
    }
}
