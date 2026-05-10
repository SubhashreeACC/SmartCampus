package com.smart.controller;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.smart.config.CustomUserDetails;
import com.smart.model.TimeTable;
import com.smart.service.AttendanceService;
import com.smart.service.FacultyService;
import com.smart.service.StudentService;

@Controller
@RequestMapping("/student")
public class StudentController {

	@Autowired
	private StudentService studentService;

	@Autowired
	private AttendanceService attendanceService;

	@Autowired
	private FacultyService facultyService;

	@ModelAttribute
	public void commonUser(Model model, @AuthenticationPrincipal CustomUserDetails user) {
		if (user != null) {
			Map<String, String> currentUser = new HashMap<>();
			currentUser.put("name", user.getFullName());
			currentUser.put("role", user.getRoleDisplay() + " · " + user.getDepartment());
			// Only add studentId for student users
			if ("STUDENT".equalsIgnoreCase(user.getRole())) {
				currentUser.put("studentId", user.getUserRefId());
			}
			model.addAttribute("currentUser", currentUser);
		}
	}

	@GetMapping("/studentDashBoard")
	public String dashboard(Model model, @AuthenticationPrincipal CustomUserDetails user) {

		// Mock Stats
		Map<String, Object> stats = new HashMap<>();
		stats.put("attendanceRate", "92%");
		stats.put("classesToday", 4);
		stats.put("assignmentsPending", 3);
		stats.put("upcomingEvents", 2);
		model.addAttribute("stats", stats);

		// Mock Activities
		List<Map<String, String>> activities = List.of(
			Map.of("type", "Assignment", "desc", "Data Structures Quiz submitted", "time", "2h ago"),
			Map.of("type", "Attendance", "desc", "Marked present in Java Lab", "time", "5h ago"),
			Map.of("type", "System", "desc", "New announcement: Annual Fest dates", "time", "Yesterday")
		);
		model.addAttribute("activities", activities);

		// Mock Deadlines
		List<Map<String, String>> deadlines = List.of(
			Map.of("title", "Database Project", "date", "Oct 15", "color", "#ff4d4d"),
			Map.of("title", "Algorithm Analysis", "date", "Oct 18", "color", "#c8f135")
		);
		model.addAttribute("deadlines", deadlines);

		return "student/studentdashboard";
	}

	@GetMapping("/mytimetable")
	public String myTimetable(Model model, @AuthenticationPrincipal CustomUserDetails user) throws SQLException {
		String deptCode = user.getDepartment();
		List<TimeTable> list = studentService.getStudentTimetable(deptCode);

		model.addAttribute("timetableList", list);

		return "student/mytimetable";
	}

	@GetMapping("/attendancerecord")
	public String attendanceRecord(Model model, @AuthenticationPrincipal CustomUserDetails user) throws Exception {
		model.addAttribute("pageTitle", "My Attendance");

		// getUserRefId() now returns the student registration number (e.g. 21CS045)
		// which matches reg_no stored in the attendance table
		List<Map<String, Object>> myRecords = attendanceService.getByStudent(user.getUserRefId());
				
		model.addAttribute("records", myRecords);

		return "student/attendance_records";
	}

	@GetMapping("/grades")
	public String myGrades(Model model, @AuthenticationPrincipal CustomUserDetails user) throws Exception {
		model.addAttribute("pageTitle", "Academic Performance");

		// Fetch grades for this specific student using the service
		List<Map<String, Object>> myResults = facultyService.getGradesByStudent(user.getUserRefId());
		model.addAttribute("results", myResults);

		return "student/my_results";
	}

	@Autowired
	private javax.sql.DataSource dataSource;

	@GetMapping("/addTestClasses")
	@org.springframework.web.bind.annotation.ResponseBody
	public String addTestClasses(@AuthenticationPrincipal CustomUserDetails user) {
		String dept = user.getDepartment();
		String sql = "INSERT INTO timetable (day, subject, faculty, start_time, end_time, room, dept_code) VALUES (?, ?, ?, ?, ?, ?, ?)";
		try (java.sql.Connection conn = dataSource.getConnection();
			 java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
			
			// Try to insert a few classes for whatever department the student belongs to
			ps.setString(1, "Monday");
			ps.setString(2, "SUB01");
			ps.setString(3, "FAC01");
			ps.setString(4, "09:00 AM");
			ps.setString(5, "10:30 AM");
			ps.setString(6, "Room 101");
			ps.setString(7, dept);
			ps.addBatch();

			ps.setString(1, "Tuesday");
			ps.setString(2, "SUB02");
			ps.setString(3, "FAC02");
			ps.setString(4, "11:00 AM");
			ps.setString(5, "12:30 PM");
			ps.setString(6, "Lab 2");
			ps.setString(7, dept);
			ps.addBatch();

			ps.setString(1, "Wednesday");
			ps.setString(2, "SUB03");
			ps.setString(3, "FAC03");
			ps.setString(4, "02:00 PM");
			ps.setString(5, "03:30 PM");
			ps.setString(6, "Room 304");
			ps.setString(7, dept);
			ps.addBatch();

			ps.executeBatch();
			return "Successfully added test classes for department: " + dept;
		} catch (Exception e) {
			return "Error: " + e.getMessage();
		}
	}

}
