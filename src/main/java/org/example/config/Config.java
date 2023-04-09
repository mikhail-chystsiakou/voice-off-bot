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
public class Config {
    @Value("${bot.storage.voice.linux}")
    String voiceStoragePathLinux;

    @Value("${bot.storage.voice.windows}")
    String voiceStoragePathWindows;
}

