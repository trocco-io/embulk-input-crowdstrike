package org.embulk.input.crowdstrike;

import org.embulk.config.Config;
import org.embulk.config.ConfigDefault;
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

    @Config("preview_mode")
    @ConfigDefault("false")
    Boolean getPreviewMode();

    @Config("region")
    String getRegion();

    @Config("preview_s3_path")
    @ConfigDefault("\"sample\"")
    String getPreviewS3Path();

    @ConfigInject
    BufferAllocator getBufferAllocator();
}
