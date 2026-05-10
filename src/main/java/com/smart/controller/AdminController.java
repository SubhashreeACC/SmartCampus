package com.smart.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.smart.common.CommonFunction;
import com.smart.config.EmailService;
import com.smart.model.Department;
import com.smart.model.Faculty;
import com.smart.model.Subject;
import com.smart.model.TimeTable;
import com.smart.service.AdminService;
import com.smart.service.MasterService;

@Controller
@RequestMapping("/admin")
public class AdminController {

	@Autowired
	private EmailService emailService;

	@Autowired
	private AdminService adminService;

	@Autowired
	private MasterService masterService;

	@Autowired
	private CommonFunction commonFunction;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@ModelAttribute
	public void commonUser(Model model, org.springframework.security.core.Authentication auth) {
		if (auth != null && auth.isAuthenticated()) {
			com.smart.config.CustomUserDetails user = (com.smart.config.CustomUserDetails) auth.getPrincipal();
			Map<String, String> currentUser = new HashMap<>();
			currentUser.put("name", user.getFullName() != null ? user.getFullName() : user.getUsername());
			currentUser.put("role", user.getRoleDisplay()); // Assume this exists or use auth.getAuthorities()
			// Only add studentId for student users
			if ("STUDENT".equalsIgnoreCase(user.getRole())) {
				currentUser.put("studentId", user.getUserRefId());
			}
			model.addAttribute("currentUser", currentUser);
		}
	}

	@GetMapping("/admindashboard")
	public String dashboard(Model model) throws SQLException {

		// Dynamic Stats
		Map<String, Object> stats = new HashMap<>();
		stats.put("studentCount", adminService.getStudentCount());
		stats.put("facultyCount", adminService.getFacultyCount());
		stats.put("deptCount", masterService.getDepartmentCount());
		stats.put("subjectCount", masterService.getSubjectCount());

		model.addAttribute("stats", stats);

		// Today Date
		String today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy"));
		model.addAttribute("todayDate", today);

		// Attendance Data (Still placeholder for now or could be dynamic if needed)
		List<Map<String, Object>> subjectAttendance = new ArrayList<>();
		subjectAttendance.add(createAttendance("Total Students", 100)); // Placeholder ratio
		model.addAttribute("subjectAttendance", subjectAttendance);

		// Leaderboard (Placeholder students for now)
		List<Map<String, Object>> leaderboard = new ArrayList<>();
		leaderboard.add(createStudent("Priya Sharma", "CSE", 9.6));
		leaderboard.add(createStudent("Rahul Mehta", "ECE", 9.4));
		leaderboard.add(createStudent("Anita Verma", "MECH", 9.2));
		model.addAttribute("leaderboard", leaderboard);

		// Weather
		Map<String, Object> weather = new HashMap<>();
		weather.put("temp", "31°");
		weather.put("condition", "Partly Cloudy");
		model.addAttribute("weather", weather);
		model.addAttribute("campusLocation", "Bhubaneswar");

		return "admin/admindashboard";
	}

	@GetMapping("/departmentlist")
	public String departmentPage(Model model) {
		List<Department> list = masterService.getAllDepartments();
		model.addAttribute("deptList", list);
		return "admin/departmentlist";
	}

	@PostMapping("/savedept")
	public String saveDepartment(@ModelAttribute Department dept) {
		masterService.saveDepartment(dept);
		return "redirect:/admin/departmentlist";
	}

	@GetMapping("/deletedept/{id}")
	public String deleteDepartment(@PathVariable int id) {
		masterService.deleteDepartment(id);
		return "redirect:/admin/departmentlist";
	}

	@PostMapping("/updatedept")
	public String updateDepartment(@ModelAttribute Department dept) {
		masterService.updateDepartment(dept);
		return "redirect:/admin/departmentlist";
	}

	@GetMapping("/subjectlist")
	public String subjectPage(Model model) {
		List<Subject> allSubjects = masterService.getAllSubjects();
		List<Department> allDepartments = masterService.getAllDepartments();
		model.addAttribute("allDepartments", allDepartments);
		model.addAttribute("allSubjects", allSubjects);
		return "admin/subjectlist";
	}

	@PostMapping("/savesubject")
	public String saveSubject(@ModelAttribute Subject subject) {
		masterService.saveSubject(subject);
		return "redirect:/admin/subjectlist";
	}

	@PostMapping("/updateSubject")
	public String updateSubject(@RequestParam Long subId, @RequestParam String subjectName) {
		masterService.updateSubject(subId, subjectName);
		return "redirect:/admin/subjectlist";

	}

	@GetMapping("/deleteSubject/{id}")
	public String deleteSubject(@PathVariable("id") Long id) {
		masterService.deleteSubject(id);
		return "redirect:/admin/subjectlist";
	}

	@GetMapping("/facultyList")
	public String facultyDetails(Model model) {
		List<Department> allDepartments = masterService.getAllDepartments();
		model.addAttribute("allDepartments", allDepartments);
		return "admin/facultyDetails";
	}

	@GetMapping("/registerFaculty")
	public String registerFaculty(@ModelAttribute Faculty faculty, Model model) {
		List<Department> allDepartments = masterService.getAllDepartments();
		model.addAttribute("allDepartments", allDepartments);
		model.addAttribute("faculty", new Faculty());
		return "admin/facultyDetails";
	}

	@InitBinder("faculty")
	public void initBinder(WebDataBinder binder) {
		binder.setDisallowedFields("profilePicture"); // Stop Spring from auto-binding MultipartFile → byte[]
	}

	@PostMapping("/saveFaculty")
	public String saveFaculty(@ModelAttribute("faculty") Faculty faculty,
			@RequestParam("profilePicture") MultipartFile profilePicture, BindingResult result)
			throws SQLException, IOException {

		// Handle file bytes manually — don't let Spring bind it
		if (!profilePicture.isEmpty()) {
			faculty.setProfilePicture(profilePicture.getBytes());
		}

		String defaultPassword = "Welcome1@";
		String userName = commonFunction.generateFacUsername();
		faculty.setUsername(userName);
		faculty.setPassword(passwordEncoder.encode(defaultPassword));

		adminService.insertFaculty(faculty);
		emailService.sendFacultyCredentials(faculty.getEmail(), faculty.getFullName(), userName, defaultPassword);

		return "redirect:/admin/facultylist";
	}

	@GetMapping("/facultylist")
	public String getAllFaculty(Model model) throws SQLException {
		List<Faculty> facultyList = adminService.getAllFaculty();
		model.addAttribute("facultyList", facultyList);
		return "admin/facultyList";
	}

	@PostMapping("/saveTimeTable")
	public String saveTimeTable(TimeTable time) {
		adminService.saveTime(time);
		return "redirect:/admin/timeTableList";
	}

	@GetMapping("/timeTableList")
	public String timeTable(Model model) throws SQLException {
		List<TimeTable> list = adminService.getAllTime();
		List<Department> allDepartments = masterService.getAllDepartments();
		List<Faculty> allFaculty = adminService.getAllFaculty();
		model.addAttribute("allFaculty", allFaculty);
		model.addAttribute("allDepartments", allDepartments);
		model.addAttribute("timeTableList", list);
		return "admin/timetabledetails";
	}

	@GetMapping("/deleteTime/{id}")
	public String deleteTime(@PathVariable("id") int id) {
		adminService.deleteTimeById(id);
		return "redirect:/admin/timeTableList";
	}

	@GetMapping("/getSubjectsByDept")
	@ResponseBody
	public List<Subject> getSubjectsByDept(@RequestParam String deptCode) {
		return masterService.getAllSubjectsByDeptCode(deptCode);
	}

	@GetMapping("/getFacultyByDept")
	@ResponseBody
	public List<Faculty> getFacultyByDept(@RequestParam String deptCode) throws SQLException {
		return adminService.getAllFacultyByDeptId(deptCode);
	}

	private Map<String, Object> createAttendance(String subject, int percentage) {
		Map<String, Object> data = new HashMap<>();
		data.put("subject", subject);
		data.put("percentage", percentage);
		return data;
	}

	private Map<String, Object> createStudent(String name, String dept, double gpa) {
		Map<String, Object> student = new HashMap<>();
		student.put("name", name);
		student.put("dept", dept);
		student.put("gpa", gpa);
		return student;
	}

}