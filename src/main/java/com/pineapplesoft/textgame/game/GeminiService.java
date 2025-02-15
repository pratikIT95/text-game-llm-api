package com.pineapplesoft.textgame.game;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pineapplesoft.textgame.game.api.Content;
import com.pineapplesoft.textgame.game.api.GeminiRequest;
import com.pineapplesoft.textgame.game.api.GeminiResponse;
import com.pineapplesoft.textgame.game.api.Part;
import com.pineapplesoft.textgame.game.api.StoryResponse;
import com.pineapplesoft.textgame.game.api.SystemInstruction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeminiService {
    private final WebClient geminiClient;
    private final String LORE = "You are a text adventure game and this is the lore - a murder mystery detective story in early 20th Century, Colonial India. You randomly assign a detective character to the player - Sherlock Holmes with Dr. John Watson, Byomkesh Bakshi with Ajit, Feluda with Topshe (who have accidentally time travelled - this scenario would involve some fish out of the water situations for Topshe), or Hercule Poirot. The assistants to the detectives help the detectives out, with questions and some queries. The story takes place in either of Chennai, Kolkata or Darjeeling, and should imbibe the atmosphere of those places in colonial times. You give the player three choices and you remember each choice. The story has three parts - a beginning, with detailed info about setting, character and the murder, a middle part involving investigation of at least 3 suspects and the ending which involves resolution. The choices will only affect the storytelling, but there is a chance that the user might be wrong with who they are suspecting, which is only revealed at the end.  You are also supposed to end the story within maximum of 20 prompts. The story should not end on a cliffhanger. Your responses should be in pure JSON format with the following fields - storyText, choices (list of 3), isEnding(whether the story has ended), without any markdown tags. If the story ends, then add a The End at in the storyText";

    private ConcurrentHashMap <String, GeminiRequest> conversationMap = new ConcurrentHashMap<>();
    private ObjectMapper objectMapper = new ObjectMapper();

    public StoryResponse getGeneratedText(String userId, String prompt){

        GeminiRequest requestBody = conversationMap.get(userId);
        
        requestBody = addDataToRequestWithRole("user", requestBody, prompt);

        String geminiResponse = geminiClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(GeminiResponse.class)
                .map(response -> {
                    if (response != null && !response.getCandidates().isEmpty()) {
                        String formattedResponse = response.getCandidates().get(0).getContent().getParts().get(0).getText();
                        formattedResponse = formattedResponse.replaceAll("```json|```", "");
                        return formattedResponse;
                    }
                    return "{ \"storyText\": \"error\", \"choices\": null, \"isEnding\": true }";
                })
                .block();
        return  getStoryResponse(geminiResponse);
    }

    private StoryResponse getStoryResponse(String geminiResponse) {
        StoryResponse storyResponse = StoryResponse.builder().build();
        try {
            storyResponse = objectMapper.readValue(geminiResponse, StoryResponse.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return storyResponse;
    }

    public StoryResponse initiateConversation(String userId){

        GeminiRequest requestBody = createSystemInstruction();

        String geminiTextResponse = geminiClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(GeminiResponse.class)
                .map(response -> {
                    if (response != null && !response.getCandidates().isEmpty()) {
                        String formattedResponse = response.getCandidates().get(0).getContent().getParts().get(0).getText();
                        formattedResponse = formattedResponse.replaceAll("```json|```", "");
                        return formattedResponse;
                    }
                    return "{ \"storyText\": \"error\", \"choices\": null, \"isEnding\": true }";
                })
                .block();

        
        
        conversationMap.put(userId, addDataToRequestWithRole("model",requestBody, geminiTextResponse));
        return getStoryResponse(geminiTextResponse);
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
