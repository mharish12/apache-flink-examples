package com.h12.flink.pipeline;

import java.util.Properties;

public abstract class KafkaBasePipeline extends AbstractBasePipeline {
    public abstract Properties getKafkaProps();
}
