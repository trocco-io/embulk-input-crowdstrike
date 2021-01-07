package org.embulk.input.crowdstrike;

import com.amazonaws.services.sqs.model.Message;
import org.embulk.config.TaskReport;
import org.embulk.spi.Exec;
import org.embulk.spi.TransactionalFileInput;
import org.embulk.spi.util.InputStreamFileInput;

import java.util.List;

public class FileInput extends InputStreamFileInput implements TransactionalFileInput {
    public FileInput(PluginTask task, List<Message> messages, SqsClient sqsClient) {
        super(task.getBufferAllocator(), new SingleFileProvider(task, messages, sqsClient));
    }

    @Override
    public void abort() {

    }

    @Override
    public TaskReport commit() {
        return Exec.newTaskReport();
    }
}
