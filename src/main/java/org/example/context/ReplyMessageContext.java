package org.example.context;

import org.example.util.ThreadLocalMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import static org.example.util.ThreadLocalMap.KEY_REPLY_MESSAGE;

@Component
public class ReplyMessageContext {
    @Autowired
    ThreadLocalMap tlm;

    public void setMessage(Message message) {
        tlm.put(KEY_REPLY_MESSAGE, message);
    }

    public String getCurrentMessageText() {
        Message message = getReplyMessage();
        return message.getText();
    }

    public void setEditMessage(Integer messageId) {

    }

    public Message getReplyMessage() {
        return tlm.get(KEY_REPLY_MESSAGE);
    }
}
