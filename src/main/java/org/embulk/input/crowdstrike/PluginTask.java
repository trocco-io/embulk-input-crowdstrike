package org.embulk.input.crowdstrike;

import org.embulk.config.Config;
import org.embulk.config.Task;
import org.embulk.config.ConfigInject;
import org.embulk.spi.BufferAllocator;

public interface PluginTask extends Task {
    @Config("aws_access_key_id")
    String getAwsAccessKeyId();

    @Config("aws_secret_access_key_id")
    String getAwsSecretAccessKeyId();

    @Config("sqs_url")
    String getSqsUrl();

    @Config("s3_bucket")
    String getS3Bucket();

    @Config("region")
    String getRegion();

    @ConfigInject
    BufferAllocator getBufferAllocator();
}
