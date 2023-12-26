


# Session Mode

[Session mode docker](https://nightlies.apache.org/flink/flink-docs-master/docs/deployment/resource-providers/standalone/docker/#session-cluster-yml)


## Build the cluster
```shell
cd k8s-flink \
docker-compose up
```

## Submit pipeline to cluster
```shell
./bin/flink run \
      --detached \
      ./streaming-examples/build/libs/streaming-examples-0.0.1-SNAPSHOT.jar
```