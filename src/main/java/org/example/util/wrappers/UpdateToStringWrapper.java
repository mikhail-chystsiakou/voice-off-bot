package org.example.util.wrappers;


import org.telegram.telegrambots.meta.api.objects.Update;

public class UpdateToStringWrapper extends ToStringWrapper {
    private Update update;

    public UpdateToStringWrapper(Update update) {
        this.update = update;
    }

    @Override
    public String toString() {
        sj.add("updateId=" + update.getUpdateId());
        if (update.getMessage() != null) {
            sj.add("updateId=" + new MessageToStringWrapper(update.getMessage()));
        }
        addNotNull("inlineQuery", update.getInlineQuery());
        addNotNull("chosenInlineQuery", update.getChosenInlineQuery());
        addNotNull("callbackQuery", update.getCallbackQuery());
        addNotNull("editedMessage", update.getEditedMessage());
        addNotNull("channelPost", update.getChannelPost());
        addNotNull("editedChannelPost", update.getEditedChannelPost());
        addNotNull("shippingQuery", update.getShippingQuery());
        addNotNull("preCheckoutQuery", update.getPreCheckoutQuery());
        addNotNull("poll", update.getPoll());
        addNotNull("pollAnswer", update.getPollAnswer());
        addNotNull("myChatMember", update.getChatMember());
        addNotNull("chatMember", update.getChatMember());
        addNotNull("chatJoinRequest", update.getChatJoinRequest());

        return "Update(" + sj + ")";
    }
}
