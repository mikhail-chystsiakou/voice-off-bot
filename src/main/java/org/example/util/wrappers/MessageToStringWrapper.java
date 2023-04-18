package org.example.util.wrappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Arrays;

public class MessageToStringWrapper extends ToStringWrapper {
    private Message message;

    public MessageToStringWrapper(Message message) {
        this.message = message;
    }

    @Override
    public String toString() {
        if (message == null) {
            return "null";
        }

        addNotNull("messageId", message.getMessageId());
        addNotNull("messageThreadId", message.getMessageThreadId());
        addNotNull("from", new UserToStringWrapper(message.getFrom()));
        addNotNull("date", message.getDate());
        addNotNull("chat", new ChatToStringWrapper(message.getChat()));
        addNotNull("forwardFrom", new UserToStringWrapper(message.getForwardFrom()));
        addNotNull("forwardFromChat", new ChatToStringWrapper(message.getForwardFromChat()));
        addNotNull("forwardDate", message.getForwardDate());
        addNotNull("text", message.getText());
        if (message.getEntities() != null) {
            addNotNull("entities", Arrays.toString(message.getEntities().toArray()));
        }
        if (message.getEntities() != null) {
            addNotNull("captionEntities", Arrays.toString(message.getCaptionEntities().toArray()));
        }


        return "Message(" + sj + ")";
    }
}
