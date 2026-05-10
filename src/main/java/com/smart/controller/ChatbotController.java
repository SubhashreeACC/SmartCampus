package com.smart.controller;

import java.sql.SQLException;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.smart.service.ChatbotService;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    @Autowired
    private ChatbotService chatbotService;

    @PostMapping("/ask")
    public Map<String, String> ask(@RequestBody Map<String, String> payload) {
        String question = payload.get("question");
        try {
            String answer = chatbotService.getAnswer(question);
            return Map.of("answer", answer);
        } catch (SQLException e) {
            return Map.of("answer", "System error: Unable to access knowledge base.");
        }
    }
}
