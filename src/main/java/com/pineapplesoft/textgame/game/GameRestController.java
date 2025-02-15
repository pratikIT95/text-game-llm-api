package com.pineapplesoft.textgame.game;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
public class GameRestController {

    @Autowired
    private GeminiService geminiService;

    @PostMapping("/start/{userId}")
    public String startGame(@PathVariable String userId) {
        return geminiService.initiateConversation(userId);
    }

    @PostMapping("/prompt/{userId}")
    public String continueGame(@PathVariable String userId, @RequestBody String prompt) {
        return geminiService.getGeneratedText(userId, prompt);
    }
}