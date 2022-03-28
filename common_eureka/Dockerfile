# 指定基础镜像 在其上面进行定制
FROM openjdk:8-jre
#挂载目录
VOLUME /usr/local/docker
#复制target/common_eureka-1.0-SNAPSHOT.jar 到容器里
COPY target/common_eureka-1.0-SNAPSHOT.jar eureka.jar

#声明运行时容器提供服务端口
EXPOSE 8761

#指定容器启动程序及参数
ENTRYPOINT ["java","-jar","eureka.jar"]