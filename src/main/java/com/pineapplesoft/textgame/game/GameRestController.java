package com.pineapplesoft.textgame.game;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.pineapplesoft.textgame.game.api.StoryResponse;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
public class GameRestController {

    @Autowired
    private GeminiService geminiService;

    @CrossOrigin(origins = "http://localhost:4200")
    @PostMapping("/start/{userId}")
    public StoryResponse startGame(@PathVariable String userId) {
        return geminiService.initiateConversation(userId);
    }
    @CrossOrigin(origins = "http://localhost:4200")
    @PostMapping("/prompt/{userId}")
    public StoryResponse continueGame(@PathVariable String userId, @RequestBody String prompt) {
        return geminiService.getGeneratedText(userId, prompt);
    }
}