# Use this image as a base
FROM 18581865951/docker-gradle
MAINTAINER konyi <xxxkonyi@gmail.com>

VOLUME /tmp

ADD / /tmp/build

#构建应用
RUN cd /tmp/build && gradle shadowJar \
        #拷贝编译结果到指定目录
        && mv query-side/build//libs/*.jar /app.jar \
        #清理编译痕迹
        && cd / && rm -rf /tmp/build

EXPOSE 8000

#RUN bash -c 'touch /app.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
