FROM openjdk:11.0.11-jre-slim
RUN mkdir -p /app/conf/
RUN mkdir -p /app/log/
VOLUME /app/log
ENTRYPOINT
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
RUN echo 'Asia/Shanghai' >/etc/timezone
WORKDIR /app
COPY target/ids-master.jar app.jar
COPY conf /app/conf
#COPY web /app/web
EXPOSE 8020
CMD ["java","-Dserver.type=master","-Dlog.home=/app/log/","-jar", "/app/app.jar"]