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

import java.util.ArrayList;
import java.util.List;

public class SqsClient {
    private final Logger logger = LoggerFactory.getLogger(SqsClient.class);

    private String queueUrl;
    private AmazonSQS client;
    private boolean isPreview;
    private int messageSize;

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
            this.isPreview = task.getPreviewMode();
            this.messageSize = task.getMessageSize();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ConfigException(e);
        }
    }

    public List<Message> getMessages() {
        try {
            if (isPreview) {
                logger.info("[preview] not receive SQS messages.");
                return new ArrayList<>();
            }
            logger.info("receive SQS messages");
            return getMessages(new ArrayList<>());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ExecutionInterruptedException(e);
        }

    }

    private List<Message> getMessages(List<Message> allMessages) {
        if (allMessages.size() >= messageSize ) {
            return allMessages;
        }
        List<Message> messages = client.receiveMessage(queueUrl).getMessages();
        logger.info(String.format("receive SQS messages size %d", messages.size()));
        if (messages.size() == 0) {
            return allMessages;
        } else {
            allMessages.addAll(messages);
            return getMessages(allMessages);
        }
    }

    public void deleteMessages(List<Message> messages) {
        try {
            for (Message message : messages) {
                client.deleteMessage(queueUrl, message.getReceiptHandle());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ExecutionInterruptedException(e);
        }
    }
}
