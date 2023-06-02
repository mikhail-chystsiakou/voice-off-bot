package org.example.listeners;

import lombok.extern.slf4j.Slf4j;
import org.example.config.BotConfig;
import org.example.enums.MessageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SpringContextListener {
    @Autowired
    BotConfig botConfig;

    @EventListener(classes = { ContextRefreshedEvent.class })
    public void contextRefreshed() {
        log.info("Context refreshed");
        log.debug("Reloading bot config for message types");
        MessageType.setConfig(botConfig);
    }
}
