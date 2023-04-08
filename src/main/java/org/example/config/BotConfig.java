package org.example.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@Data
@PropertySource("classpath:secrets.properties")
public class BotConfig {

    @Value("${bot.name}")
    String botName;

    @Value("${TG_BOT_TOKEN}")
    String token;

    @Value("${bot.owner}")
    Long ownerId;

    @Value("${bot.storage.voice}")
    String voiceStoragePath;

    @Value("${bot.vfs}")
    String vfsHost;

    @Value("${api.url}")
    String apiUrl;
}
