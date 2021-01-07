package org.embulk.input.crowdstrike;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import org.embulk.config.ConfigException;
import org.embulk.exec.ExecutionInterruptedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SqsClient {
    private final Logger logger = LoggerFactory.getLogger(SqsClient.class);

    private String queueUrl;
    private AmazonSQS client;

    public SqsClient(PluginTask task) {
        try {
            logger.info(String.format("[connect SQS] %s", task.getSqsUrl()));
            BasicAWSCredentials credentials = new BasicAWSCredentials(task.getAwsAccessKeyId(), task.getAwsSecretAccessKeyId());
            this.queueUrl = task.getSqsUrl();
            this.client = AmazonSQSClientBuilder
                    .standard()
                    .withRegion(Regions.fromName(task.getRegion()))
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .build();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ConfigException(e);
        }
    }

    public List<Message> getMessages() {
        try {
            logger.info("receive SQS messages");
            return client.receiveMessage(queueUrl).getMessages();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ExecutionInterruptedException(e);
        }

    }

    public void deleteMessages(List<Message> messages) {
        try {
            for (Message message : messages) {
                logger.info(String.format("[delete SQS message] %s", message.getBody()));
                client.deleteMessage(queueUrl, message.getReceiptHandle());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ExecutionInterruptedException(e);
        }
    }
}
