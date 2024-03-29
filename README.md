# Apache Flink
# How to run pipeline on cluster

### Step 1 Download and install Flink

### Step 2: Go to apache flink folder and run following command
```
 ./bin/start-cluster.sh 
```
`Note: Above command will start flink cluster.`

`Run this pipeline pipeline/ApacheFlinkPipeline.java`

### Step 3: Submit your jar file to cluster
```
./bin/flink run -c <FCCN> <path-to-jar-file>.jar
```

Example:
```
./bin/flink run -c org.h12.flink.streaming.pipeline.KafkaStreamingPipeline $(pwd)/streaming-examples/build/libs/streaming-examples-0.0.1-SNAPSHOT.jar 
```
* Above command will submit your jar file to the cluster.
### Step 4: Go to Flink Web UI by visiting http://localhost:8081

### Step 5: Stop Cluster
```
 ./bin/stop-cluster.sh 
```


# FOR Kubernetes

## Apache Flink

- Install bitnami/flink helm chart

```agsl
 helm install <deployment-name> -f   helm-values/flink-values.yaml oci://registry-1.docker.io/bitnamicharts/flink
```

- Port Forward

```agsl
kubectl port-forward svc/flink-1703758387-jobmanager 8081:8081
```


### Submit the build jar on Apache Flink Cluster

- Copy jar file to the pod : ```kubectl cp <jar-file-path> <job-manager-pod>:/tmp/pipeline.jar```
- Execute Pod in interactive mode  :  ```kubect exec -i -t <job-manager-pod-name>  -- bash```
    - Ex : ``` kubectl cp $(pwd)/apache-flink/build/pipeline-1.0-SNAPSHOT.jar flink-1703758387-jobmanager:/tmp/pipeline.jar```
```agsl
flink run -c org.flink.pipeline.ApacheFlinkPipeline /tmp/pipeline.jar
```

 