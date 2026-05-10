package com.smart.serviceImpl;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.smart.model.ChatbotKnowledge;
import com.smart.service.ChatbotService;

@Service
public class ChatbotServiceImpl implements ChatbotService {

    @Autowired
    private DataSource dataSource;

    private void ensureTableExists() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS chatbot_knowledge (" +
                     "id INT AUTO_INCREMENT PRIMARY KEY, " +
                     "question TEXT, " +
                     "answer TEXT, " +
                     "category VARCHAR(100))";
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            
            // Seed initial data if empty
            String checkSql = "SELECT COUNT(*) FROM chatbot_knowledge";
            try (ResultSet rs = stmt.executeQuery(checkSql)) {
                if (rs.next() && rs.getInt(1) == 0) {
                    seedData(conn);
                }
            }
        }
    }

    private void seedData(Connection conn) throws SQLException {
        String sql = "INSERT INTO chatbot_knowledge (question, answer, category) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String[][] data = {
                {"admission process", "Admission is based on entrance merit. Visit the portal to apply.", "Admin"},
                {"hostel fees", "Hostel fees are $500 per semester including mess.", "Facilities"},
                {"exam schedule", "Exams start from Dec 15th. Check the dashboard for details.", "Academic"},
                {"contact info", "Contact us at support@smartcampus.edu", "Support"}
            };
            for (String[] row : data) {
                ps.setString(1, row[0]);
                ps.setString(2, row[1]);
                ps.setString(3, row[2]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    @Override
    public void saveKnowledge(ChatbotKnowledge knowledge) throws SQLException {
        ensureTableExists();
        String sql = "INSERT INTO chatbot_knowledge (question, answer, category) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, knowledge.getQuestion());
            ps.setString(2, knowledge.getAnswer());
            ps.setString(3, knowledge.getCategory());
            ps.executeUpdate();
        }
    }

    @Override
    public String getAnswer(String userQuestion) throws SQLException {
        ensureTableExists();
        
        // 1. Fetch all knowledge (small set) to allow AI to perform intelligent matching
        StringBuilder context = new StringBuilder();
        String sql = "SELECT question, answer FROM chatbot_knowledge";
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                context.append("Information on ").append(rs.getString("question")).append(": ").append(rs.getString("answer")).append("\n");
            }
        }
        
        // 2. Pass context to AI for fuzzy retrieval and natural conversation
        return callLocalAIStrict(userQuestion, context.toString());
    }

    private String callLocalAIStrict(String question, String context) {
        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            
            String systemPrompt = "You are a STRICT SmartCampus Assistant. \\n" +
                                  "RULE 1: You ONLY answer questions about this college based on the context below.\\n" +
                                  "RULE 2: If the user asks about ANYTHING ELSE (math, general knowledge, global news, other topics), you MUST respond with EXACTLY this phrase: 'I am designed to answer only college-related questions. Please ask about admissions, fees, or campus info.'\\n" +
                                  "RULE 3: Do not use your own knowledge. Only use the context provided.\\n\\n" +
                                  "Context:\\n" + context.replace("\"", "\\\"").replace("\n", "\\n");
            
            String fullPrompt = systemPrompt + "\\n\\nUser: " + question + "\\nAssistant:";
            
            String json = "{\"model\": \"tinyllama\", \"prompt\": \"" + fullPrompt + "\", \"stream\": false}";
            
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("http://localhost:11434/api/generate"))
                    .header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(json))
                    .build();

            java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                String body = response.body();
                int start = body.indexOf("\"response\":\"") + 12;
                int end = body.indexOf("\"", start);
                if (start > 11 && end > start) {
                    return body.substring(start, end).replace("\\n", "\n").replace("\\\"", "\"");
                }
            }
        } catch (Exception e) {
            System.err.println("AI Connection Error: " + e.getMessage());
        }
        return "I'm sorry, I only have information on Admissions, Fees, and Exams based on our official records.";
    }

    @Override
    public List<ChatbotKnowledge> getAllKnowledge() throws SQLException {
        ensureTableExists();
        List<ChatbotKnowledge> list = new ArrayList<>();
        String sql = "SELECT * FROM chatbot_knowledge";
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ChatbotKnowledge k = new ChatbotKnowledge();
                k.setId(rs.getLong("id"));
                k.setQuestion(rs.getString("question"));
                k.setAnswer(rs.getString("answer"));
                k.setCategory(rs.getString("category"));
                list.add(k);
            }
        }
        return list;
    }
}
