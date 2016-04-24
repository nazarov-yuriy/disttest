FROM java:8-jre
WORKDIR /root
RUN apt-get update
RUN apt-get install -y git build-essential
COPY build/libs/disttest.jar /root
COPY kernelConfigs kernelConfigs
COPY kernelPatches kernelPatches
COPY fstab /etc/fstab
RUN mkdir -p artifacts downloads kernelPatches kernelRepo kernelSources/empty mounts
ENTRYPOINT ["java", "-jar", "/root/disttest.jar"]