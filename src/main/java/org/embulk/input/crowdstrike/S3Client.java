package org.embulk.input.crowdstrike;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import org.embulk.config.ConfigException;
import org.embulk.exec.ExecutionInterruptedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class S3Client {
    private final Logger logger = LoggerFactory.getLogger(S3Client.class);

    private AmazonS3 client;
    private String bucket;

    public S3Client(PluginTask task) {
        try {
            logger.info(String.format("[connect S3] %s", task.getS3Bucket()));
            BasicAWSCredentials credentials = new BasicAWSCredentials(task.getAwsAccessKeyId(), task.getAwsSecretAccessKeyId());
            this.client = AmazonS3ClientBuilder
                    .standard()
                    .withRegion(Regions.fromName(task.getRegion()))
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .build();
            this.bucket = task.getS3Bucket();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ConfigException(e);
        }
    }

    public InputStream findS3Object(String keyName) {
        try {
            logger.info(String.format("[get S3 Object] %s", keyName));
            S3Object object = client.getObject(bucket, keyName);
            return object.getObjectContent().getDelegateStream();
        } catch (SdkClientException e) {
            logger.error(e.getMessage(), e);
            throw new ExecutionInterruptedException(e);
        }
    }
}
