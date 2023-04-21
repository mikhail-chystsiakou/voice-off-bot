package org.example.listeners;

import org.example.config.BotConfig;
import org.example.enums.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class SpringContextListener {
    private static final Logger logger = LoggerFactory.getLogger(SpringContextListener.class);
    @Autowired
    BotConfig botConfig;

    @EventListener(classes = { ContextRefreshedEvent.class })
    public void contextRefreshed() {
        logger.info("Context refreshed");
        logger.debug("Reloading bot config for message types");
        MessageType.setConfig(botConfig);
    }
}
