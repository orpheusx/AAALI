FROM eclipse-temurin:23.0.1_11-jre-alpine
LABEL authors="Mark W. Stewart <zouzousdad@gmail.com>"

EXPOSE 8080

ENV JAR_FILE=AAALIClaimsMgmtAPI-1.0-SNAPSHOT-jar-with-dependencies.jar
ENV DEST_DIR=/opt/app
RUN mkdir $DEST_DIR

COPY target/$JAR_FILE $DEST_DIR

ENTRYPOINT ["java", "-jar", "/opt/app/AAALIClaimsMgmtAPI-1.0-SNAPSHOT-jar-with-dependencies.jar"]
