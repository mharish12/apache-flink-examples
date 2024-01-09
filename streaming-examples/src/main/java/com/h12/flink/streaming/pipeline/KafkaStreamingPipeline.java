package com.h12.flink.streaming.pipeline;

import com.h12.flink.pipeline.KafkaBasePipeline;
import com.h12.flink.streaming.kafka.model.KafkaInput;
import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class KafkaStreamingPipeline extends KafkaBasePipeline {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaStreamingPipeline.class);

    public static void main(String[] args) {
        KafkaBasePipeline kafkaBasePipeline = new KafkaStreamingPipeline();
        kafkaBasePipeline.run();
    }

    @Override
    public void run() {
        try {
            final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
            env.setRuntimeMode(RuntimeExecutionMode.STREAMING);
            env.enableCheckpointing(5000);
            //Retries
            env.setRestartStrategy(RestartStrategies.fixedDelayRestart(3, Time.of(30, TimeUnit.SECONDS)));

            ParameterTool parameterTool = ParameterTool.fromPropertiesFile(KafkaStreamingPipeline.class.getResourceAsStream("application.properties"));
            parameterTool.mergeWith(ParameterTool.fromSystemProperties());

            KafkaSource<KafkaInput> source = KafkaSource.<KafkaInput>builder()
                    .setBootstrapServers("localhost:9092")
                    .setProperties(this.getKafkaProps())
                    .setTopics("input-topic")
                    .setGroupId("my-group")
                    .setStartingOffsets(OffsetsInitializer.earliest())
                    .setProperty("security.protocol", "SASL_PLAINTEXT")
                    .setDeserializer(new KafkaRecordDeserializationSchema<KafkaInput>() {
                        @Override
                        public void deserialize(ConsumerRecord<byte[], byte[]> consumerRecord, Collector<KafkaInput> collector) throws IOException {
                            long offset = consumerRecord.offset();
                            int partition = consumerRecord.partition();
                            Headers headers = consumerRecord.headers();
                            String key = new String(consumerRecord.key());
                            String value = new String(consumerRecord.value());
                            collector.collect(new KafkaInput(headers, key, value, partition, offset));
                        }

                        @Override
                        public TypeInformation<KafkaInput> getProducedType() {
                            return TypeInformation.of(KafkaInput.class);
                        }
                    })
                    .setProperty("sasl.mechanism", "PLAIN")
                    .setProperty("sasl.jaas.config", "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"username\" password=\"password\";")
                    .build();

//        WatermarkStrategy<KafkaInput> watermarkStrategy =
//                WatermarkStrategy
//                        .<KafkaInput>forMonotonousTimestamps()
//                        .withTimestampAssigner(
//                                (record, timestamp) -> timestamp
//                        );

            DataStreamSource<KafkaInput> stream = env.fromSource(source, WatermarkStrategy.noWatermarks(), "ingress-stream");


            DataStream<String> savedToDbStream = stream.name("kafka-input")
                    .process(new ProcessFunction<KafkaInput, String>() {
                        @Override
                        public void processElement(KafkaInput kafkaInput, ProcessFunction<KafkaInput, String>.Context context, Collector<String> collector) throws Exception {
                            LOG.info("Saving to database: {}.", kafkaInput);
                            collector.collect(kafkaInput.getValue());
                        }
                    }).setParallelism(16)
                    .setMaxParallelism(24);

            DataStream<String> transformedStream = savedToDbStream.process(new ProcessFunction<String, String>() {
                        @Override
                        public void processElement(String s, ProcessFunction<String, String>.Context context, Collector<String> collector) throws Exception {
                            LOG.info("Transformation: {}.", s);
                            collector.collect(s);
                        }
                    }).setParallelism(16)
                    .setMaxParallelism(24);

            DataStream<String> restStream = transformedStream.process(new ProcessFunction<String, String>() {
                        @Override
                        public void processElement(String s, ProcessFunction<String, String>.Context context, Collector<String> collector) throws Exception {
                            LOG.info("REST call: {}.", s);
                            collector.collect(s);
                        }
                    }).setParallelism(16)
                    .setMaxParallelism(24);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }

    }

    @Override
    public Properties getKafkaProps() {
        Properties properties = new Properties();
        properties.putIfAbsent("", "");
        return properties;
    }
}
