package com.smart.controller;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

@Controller
@RequestMapping("/faculty")
public class FacultyController {

	@Autowired
	private FacultyService facultyService;

	@Autowired
	private AttendanceService attendanceService;

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

	@GetMapping("/facultydashboard")
	public String dashboard(Model model, @AuthenticationPrincipal CustomUserDetails user) throws SQLException {

		String facultyId = user.getUserRefId();

		// Stats — wrapped in map to match template ${stats.xxx} expressions
		Map<String, Object> stats = new HashMap<>();
		stats.put("classesToday", facultyService.getClassesTodayCount(user.getUserRefId()));
		stats.put("totalStudents", facultyService.getStudentCountByDept(user.getDepartment()));
		stats.put("subjectsTaught", facultyService.getSubjectCountByFaculty(user.getUserRefId()));
		stats.put("attendanceRate", "84%");
		model.addAttribute("stats", stats);

		// Analytics
		Map<String, Object> analytics = new HashMap<>();
		analytics.put("avgAttendance", "84%");
		analytics.put("activeBatches", 3);
		analytics.put("pendingGrades", 12);
		model.addAttribute("analytics", analytics);

		// Today Date
		String today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy"));
		model.addAttribute("todayDate", today);

		// Today's Classes
		List<TimeTable> todayClasses = facultyService.getFacultyTimetable(facultyId);
		String dayOfWeek = LocalDate.now().getDayOfWeek().name();
		List<TimeTable> filteredClasses = todayClasses.stream()
				.filter(c -> c.getDay().equalsIgnoreCase(dayOfWeek))
				.toList();
		
		model.addAttribute("todayClasses", filteredClasses);

		// Weather
		Map<String, Object> weather = new HashMap<>();
		weather.put("temp", "31°");
		weather.put("condition", "Partly Cloudy");
		model.addAttribute("weather", weather);
		model.addAttribute("campusLocation", "Bhubaneswar");

		return "faculty/facultydashboard";
	}

	@GetMapping("/attendance")
	public String attendancePage(Model model, @AuthenticationPrincipal CustomUserDetails user) throws SQLException {

		List<TimeTable> myClasses = facultyService.getFacultyTimetable(user.getUserRefId());
		model.addAttribute("myClasses", myClasses);

		return "faculty/attendance";
	}

	@GetMapping("/myclasses")
	public String myclass(Model model, @AuthenticationPrincipal CustomUserDetails user) throws SQLException {

		String facultyId = user.getUserRefId();
		List<TimeTable> list = facultyService.getFacultyTimetable(facultyId);
		model.addAttribute("timetableList", list);

		return "faculty/myclasses";
	}

	@GetMapping("/attendance-records")
	public String attendanceRecords(Model model, @AuthenticationPrincipal CustomUserDetails user) throws Exception {
		model.addAttribute("pageTitle", "Attendance Intelligence");

		List<Map<String, Object>> records = attendanceService.getAll();
		model.addAttribute("records", records);

		return "faculty/attendance_records";
	}

	@Autowired
	private com.smart.service.MasterService masterService;

	@GetMapping("/grades")
	public String uploadGradesPage(Model model, @AuthenticationPrincipal CustomUserDetails user) throws SQLException {
		model.addAttribute("pageTitle", "Grade Management");

		model.addAttribute("allDepartments", masterService.getAllDepartments());
		model.addAttribute("allSubjects", masterService.getAllSubjects());

		return "faculty/upload_grades";
	}

	@GetMapping("/get-students-by-dept")
	@org.springframework.web.bind.annotation.ResponseBody
	public List<com.smart.model.Student> getStudentsByDept(
			@org.springframework.web.bind.annotation.RequestParam String deptCode,
			@org.springframework.web.bind.annotation.RequestParam String subjectCode,
			@org.springframework.web.bind.annotation.RequestParam int semester,
			@org.springframework.web.bind.annotation.RequestParam String academicYear) throws SQLException {
		return facultyService.getUngradedStudents(deptCode, subjectCode, semester, academicYear);
	}

	@org.springframework.web.bind.annotation.PostMapping("/save-grade")
	@org.springframework.web.bind.annotation.ResponseBody
	public Map<String, Object> saveGrade(@org.springframework.web.bind.annotation.RequestBody com.smart.model.Grade grade) throws SQLException {
		facultyService.saveGrade(grade);
		return Map.of("success", true, "message", "Grade saved successfully");
	}

}
