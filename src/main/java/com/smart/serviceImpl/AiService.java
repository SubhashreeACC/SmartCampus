package com.smart.serviceImpl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;

@Service
public class AiService {

	@Autowired
	private DataSource dataSource;

	private final String OLLAMA_URL = "http://localhost:11434/api/generate";
	private final String MODEL = "tinyllama"; // change if needed

	public String generateResponse(String prompt) {

		RestTemplate restTemplate = new RestTemplate();

		Map<String, Object> body = new HashMap<>();
		body.put("model", MODEL);
		body.put("prompt", prompt);
		body.put("stream", false);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

		ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
				OLLAMA_URL,
				HttpMethod.POST,
				entity,
				new ParameterizedTypeReference<Map<String, Object>>() {}
		);

		Map<String, Object> responseBody = response.getBody();
		return (responseBody != null && responseBody.get("response") != null) ? responseBody.get("response").toString() : "No response";
	}

	public String askAI(String question) {

		String q = question.toLowerCase().trim();

		if (q.equals("hi") || q.equals("hello") || q.equals("hey")) {
			return "Hello 👋 How can I help you about the campus?";
		}

		String context = getCampusData();

		String prompt = "You are a helpful campus assistant.\n"
				+ "Use only the campus data below to answer the question.\n"
				+ "If the answer is not present in the campus data, reply politely:\n"
				+ "\"Sorry, I couldn't find that information in the campus data.\"\n\n" + "Campus Data:\n" + context
				+ "\n\nQuestion: " + question + "\nAnswer:";

		return callTinyLlama(prompt);
	}

	private String getCampusData() {

		StringBuilder data = new StringBuilder();

		try (Connection conn = dataSource.getConnection()) {

			String sql = "SELECT student_name, student_id, department, semester, course, faculty_name, subject, classroom, building, library_books, hostel_name, hostel_room, bus_route, club, event_name, event_date, cafeteria_item, sports_team, lab_name, notice FROM campus_data LIMIT 10";

			PreparedStatement ps = conn.prepareStatement(sql);

			ResultSet rs = ps.executeQuery();

			while (rs.next()) {

				data.append("Student: ").append(rs.getString("student_name")).append(" (ID: ")
						.append(rs.getString("student_id")).append(") studies ").append(rs.getString("course"))
						.append(" in ").append(rs.getString("department")).append(", Semester ")
						.append(rs.getInt("semester")).append(". Faculty: ").append(rs.getString("faculty_name"))
						.append(" teaches ").append(rs.getString("subject")).append(". Classroom: ")
						.append(rs.getString("classroom")).append(", Building: ").append(rs.getString("building"))
						.append(". Event: ").append(rs.getString("event_name")).append(" on ")
						.append(rs.getString("event_date")).append(". Notice: ").append(rs.getString("notice"))
						.append(".\n\n");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return data.toString();
	}

	private String callTinyLlama(String question) {

		try {

			URL url = new URL("http://localhost:11434/api/chat");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setDoOutput(true);

			ObjectMapper mapper = new ObjectMapper();

			Map<String, Object> message = new HashMap<>();
			message.put("role", "user");
			message.put("content", question);

			Map<String, Object> body = new HashMap<>();
			body.put("model", "tinyllama");
			body.put("messages", List.of(message));
			body.put("stream", false);

			String json = mapper.writeValueAsString(body);

			OutputStream os = conn.getOutputStream();
			os.write(json.getBytes());
			os.flush();
			os.close();

			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

			StringBuilder response = new StringBuilder();
			String line;

			while ((line = br.readLine()) != null) {
				response.append(line);
			}

			br.close();

			// Parse JSON response
			Map<String, Object> result = mapper.readValue(response.toString(), new TypeReference<Map<String, Object>>() {});
			@SuppressWarnings("unchecked")
			Map<String, Object> msg = (Map<String, Object>) result.get("message");

			String content = msg.get("content").toString();

			// Clean unwanted text
			content = content.replace("The answer is:", "").replace("Answer:", "").trim();

			return content;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return "AI error";
	}

}
