package org.embulk.input.crowdstrike;

import com.amazonaws.services.sqs.model.Message;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.embulk.config.ConfigException;
import org.embulk.exec.NoSampleException;
import org.embulk.spi.util.InputStreamFileInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SingleFileProvider implements InputStreamFileInput.Provider {
    private final Logger logger = LoggerFactory.getLogger(SingleFileProvider.class);

    private Iterator<String> iterator;
    private S3Client s3Client;
    private SqsClient sqsClient;
    private List<Message> messages;

    public SingleFileProvider(PluginTask task, List<Message> messages, SqsClient sqsClient) {
        logger.info(String.format("[message size] %d", messages.size()));

        if(messages.size() == 0){
            throw new NoSampleException("message is empty");
        }

        List<String> paths = new ArrayList<>();
        for (Message message : messages) {
            try {
                String body = message.getBody();
                logger.info(String.format("[message] %s", body));
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(body);
                JsonNode files = node.get("files");
                for (JsonNode fileInfo : files) {
                    paths.add(fileInfo.get("path").asText());
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        this.iterator = paths.iterator();
        this.s3Client = new S3Client(task);
        this.sqsClient = sqsClient;
        this.messages = messages;
    }

    @Override
    public InputStream openNext() {
        if (!iterator.hasNext()) {
            return null;
        }
        return s3Client.findS3Object(iterator.next());
    }

    @Override
    public void close() {
        sqsClient.deleteMessages(messages);
    }
}
