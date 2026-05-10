package com.smart.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.smart.model.AttendanceRequest;
import com.smart.service.AttendanceService;

@Controller
@RequestMapping("/attendance")
public class AttendanceController {

	@Autowired
	private AttendanceService attendanceService;

	@PostMapping("/session/start")
	@ResponseBody
	public Map<String, Object> startSession(@RequestBody Map<String, Object> body,
			jakarta.servlet.http.HttpServletRequest request) throws Exception {
		// Inject the server's real base URL so the QR code points to the correct address
		String scheme = request.getScheme();
		String host = request.getServerName();
		int port = request.getServerPort();
		String baseUrl = scheme + "://" + host + (port != 80 && port != 443 ? ":" + port : "");
		body.put("baseUrl", baseUrl);
		return attendanceService.startSession(body);
	}

	@PostMapping("/session/end/{sessionId}")
	@ResponseBody
	public Map<String, Object> endSession(@PathVariable String sessionId) throws Exception {
		return attendanceService.endSession(sessionId);
	}

	 @PostMapping("/mark")
	    public ResponseEntity<Map<String, Object>> markAttendance(@RequestBody AttendanceRequest req) {
	        try {
	            Map<String, Object> response = attendanceService.markAttendance(req);
	            return ResponseEntity.ok(response);
	        } catch (Exception e) {
	            return ResponseEntity.badRequest().body(
	                    Map.of("success", false, "message", e.getMessage())
	            );
	        }
	    }

	    // ✅ Get student details (auto-fill name)
	    @GetMapping("/student/{studentId}")
	    public ResponseEntity<Map<String, Object>> getStudent(@PathVariable String studentId) {
	        try {
	            Map<String, Object> student = attendanceService.getStudentById(studentId);
	            return ResponseEntity.ok(student);
	        } catch (Exception e) {
	            return ResponseEntity.badRequest().body(
	                    Map.of("success", false, "message", e.getMessage())
	            );
	        }
	    }

	@GetMapping("/session/{sessionId}")
	@ResponseBody
	public List<Map<String, Object>> getBySession(@PathVariable String sessionId) throws Exception {
		return attendanceService.getBySession(sessionId);
	}

	@GetMapping("/all")
	@ResponseBody
	public List<Map<String, Object>> getAll() throws Exception {
		return attendanceService.getAll();
	}

	@GetMapping("/mark")
	public String openMarkPage(@RequestParam("sessionId") String sessionId, Model model) {
		model.addAttribute("sessionId", sessionId);
		return "faculty/mark";
	}

}
