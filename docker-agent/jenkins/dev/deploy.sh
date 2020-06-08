#!/bin/sh
let_time=$(date "+%Y%m%d")

imageTag="msmp"
projectName="docker-agent"
envName="dev"
#配置中心命令空间，如果没有配置中心，可把这个配置设置为空，如下
#nacosConfigNamespace=""
nacosConfigNamespace=" -e NACOS_CONFIG_NAMESPACE=30c27f99-67ad-4b96-ab6d-335886a85e87 "

cd /root/dockerfile/${envName}/${projectName}/

containerName=$(docker ps -a|grep ${projectName}-${envName}|awk '{print $1}')
if [ "$containerName" != "" ] ; then
echo 删除容器 $containerName ...
docker rm -f $containerName
fi
echo 删除镜像
docker rmi -f ${imageTag}/${projectName}:${envName}
echo 运行新的容器 ...

docker build -t ${imageTag}/${projectName}:${envName} .

# 后端
docker run -d --net=host -p 8087:8087 -e PROFILE_ACTIVE=${envName} -e NACOS_HOST=172.18.222.60 ${nacosConfigNamespace} -e REDIS_HOST=172.18.222.60 -e REDIS_DB="11" -e REDIS_PASS=800-820-8820 -e MYSQL_URL="jdbc:mysql://120.78.147.130:3306/udp?characterEncoding=UTF-8&useUnicode=true&useSSL=false&serverTimezone=Asia/Shanghai" -e MYSQL_PASS='foreveross@123#@!' --name ${projectName}-${envName} ${imageTag}/${projectName}:${envName}
