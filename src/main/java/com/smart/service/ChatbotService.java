package com.smart.service;

import java.sql.SQLException;
import java.util.List;
import com.smart.model.ChatbotKnowledge;

public interface ChatbotService {
    void saveKnowledge(ChatbotKnowledge knowledge) throws SQLException;
    String getAnswer(String question) throws SQLException;
    List<ChatbotKnowledge> getAllKnowledge() throws SQLException;
}
