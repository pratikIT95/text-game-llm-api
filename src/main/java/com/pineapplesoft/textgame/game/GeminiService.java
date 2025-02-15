package com.pineapplesoft.textgame.game;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.pineapplesoft.textgame.game.api.Content;
import com.pineapplesoft.textgame.game.api.GeminiRequest;
import com.pineapplesoft.textgame.game.api.GeminiResponse;
import com.pineapplesoft.textgame.game.api.Part;
import com.pineapplesoft.textgame.game.api.SystemInstruction;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GeminiService {
    private final WebClient geminiClient;
    private final String LORE = "You are a text adventure game and this is the lore - Alice in Wonderland by Lewis Carol. You randomly assign a role to the player, which could be anyone in Wonderland from the following list - The Mad Hatter, Queen of Hearts, Caterpillar, Doormouse or Batman (he's here under hypnosis by Mad Hatter from Batman). Don't assume that the player is aware of the lore and explain things in a way that makes it understandable for both a first timer and a veteran reader of the books. You give the player three choices and you remember each choice. You are also supposed to end the story within maximum of 10 prompts. The story should not end on a cliffhanger and your final response at the end of the story should have the exact text - The_End";

    private ConcurrentHashMap <String, GeminiRequest> conversationMap = new ConcurrentHashMap<>();

    public String getGeneratedText(String userId, String prompt){

        GeminiRequest requestBody = conversationMap.get(userId);
        
        requestBody = addDataToRequestWithRole("user", requestBody, prompt);

        return geminiClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(GeminiResponse.class)
                .map(response -> {
                    if (response != null && !response.getCandidates().isEmpty()) {
                        return response.getCandidates().get(0).getContent().getParts().get(0).getText();
                    }
                    return "Error: No response from Gemini API";
                })
                .block();
    }

    public String initiateConversation(String userId){

        GeminiRequest requestBody = createSystemInstruction();

        // System.out.println(requestBody);

        String geminiTextResponse = geminiClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(GeminiResponse.class)
                .map(response -> {
                    if (response != null && !response.getCandidates().isEmpty()) {
                        return response.getCandidates().get(0).getContent().getParts().get(0).getText();
                    }
                    return "Error: No response from Gemini API";
                })
                .block();
        
        conversationMap.put(userId, addDataToRequestWithRole("user",requestBody, geminiTextResponse));
        return geminiTextResponse;
    }

    private GeminiRequest addDataToRequestWithRole(String role, GeminiRequest requestBody, String data) {
        List<Content> contents = requestBody.getContents();
        List<Content> newContents = new ArrayList<>();
        if (contents == null) {
            contents = new ArrayList<>();
        }
        newContents.addAll(contents);
        newContents.add(getContent(role, data));
        requestBody.setContents(newContents);
        System.out.println(requestBody);
        return requestBody;
    }

    private Content getContent(String role, String response) {
        return Content.builder()
        .role(role)
        .parts(List.of(Part.builder().text(response).build()))
        .build();
    }



    private GeminiRequest createSystemInstruction(){
        return GeminiRequest.builder()
            .systemInstruction(SystemInstruction.builder()
                            .parts
                                    (Part.builder().text(LORE).build()).
                            build())
            .contents(List.of(Content.builder()
            .role("user")
            .parts(List.of(Part.builder().text("Hello there!").build())).build()))
            .build();
    }

        
}
