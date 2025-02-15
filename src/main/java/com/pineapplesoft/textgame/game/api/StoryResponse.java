package com.pineapplesoft.textgame.game.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StoryResponse {
    private String storyText;
    @JsonProperty("isEnding")
    private boolean isEnding;
    private List<String> choices;
}
