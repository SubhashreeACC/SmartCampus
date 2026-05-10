package com.smart.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smart.serviceImpl.AiService;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin
public class AiController {

	@Autowired
	private AiService aiService;

	@PostMapping("/notes")
	public String generateNotes(@RequestBody Map<String, String> request) {
		System.out.println("Helllo");
		String text = request.get("text");
		String prompt = "Create clean structured study notes from this text:\n\n" + text;
		return aiService.generateResponse(prompt);
	}

	@PostMapping("/doubt")
	public String solveDoubt(@RequestBody Map<String, String> request) {
		System.out.println("Helllo1");

		String question = request.get("question");

		String prompt = "Explain this clearly like a teacher:\n\n" + question;

		return aiService.generateResponse(prompt);
	}

	@PostMapping("/summary")
	public String summarize(@RequestBody Map<String, String> request) {
		System.out.println("Helllo2");

		String text = request.get("text");

		String prompt = "Summarize this in simple bullet points:\n\n" + text;

		return aiService.generateResponse(prompt);
	}

	@PostMapping("/mcq")
	public String generateMcq(@RequestBody Map<String, String> request) {
		System.out.println("Helllo3");

		String text = request.get("text");

		String prompt = "Create 10 multiple choice questions with 4 options and correct answer from:\n\n" + text;

		return aiService.generateResponse(prompt);
	}

	@PostMapping("/chat")
	public Map<String, String> chat(@RequestBody Map<String, String> request) {
		String message = request.get("message");		
		String response = aiService.askAI(message);
		System.out.println("response "+response);

		Map<String, String> result = new HashMap<>();
		result.put("response", response);

		return result;
	}

}
