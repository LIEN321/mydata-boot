FROM lien321/alpine-java:openjdk17_cn_slim

MAINTAINER mydata_work@163.com

RUN mkdir -p /mydata

WORKDIR /mydata

EXPOSE 8800

ADD ./target/mydata-boot.jar ./app.jar

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]

CMD ["--spring.profiles.active=prod"]
