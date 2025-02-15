package com.pineapplesoft.textgame.game.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class GameWebClientConfiguration {

    private final GeminiConfig geminiConfig;

    @Bean
    public WebClient geminiClient(){
        return WebClient.builder()
        .baseUrl(String.format(geminiConfig.getUrl(),geminiConfig.getKey()))
        .build();
    }
}
