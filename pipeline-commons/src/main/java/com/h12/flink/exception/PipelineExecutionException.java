package com.h12.flink.exception;

public class PipelineExecutionException extends RuntimeException {
    public PipelineExecutionException() {
        super();
    }

    public PipelineExecutionException(String message) {
        super(message);
    }

    public PipelineExecutionException(String message, Throwable cause) {
        super(message, cause);
    }


    public PipelineExecutionException(Throwable cause) {
        super(cause);
    }

    protected PipelineExecutionException(String message, Throwable cause,
                                         boolean enableSuppression,
                                         boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
