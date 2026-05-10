package com.smart.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.config.EmailService;
import com.smart.config.JwtService;
import com.smart.model.Department;
import com.smart.model.Student;
import com.smart.service.MasterService;
import com.smart.service.StudentService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class LoginController {

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private MasterService masterService;

	@Autowired
	private EmailService emailService;

	@Autowired
	private com.smart.config.CaptchaService captchaService;

	@Autowired
	private com.smart.service.AdminService adminService;

	private final StudentService studentService;

	@Autowired
	public LoginController(StudentService studentService) {
		this.studentService = studentService;
	}
	
	@Autowired
	private com.smart.common.CommonFunction commonFunction;

	@GetMapping("/index")
	public String home(Model model) {
		try {
			model.addAttribute("totalStudents", studentService.getTotalStudentCount());
		} catch (Exception e) {
			model.addAttribute("totalStudents", 0);
		}
		return "util/index";
	}

	@GetMapping("/login")
	public String login(@RequestParam(value = "error", required = false) String error, Model model, jakarta.servlet.http.HttpSession session) {
		if (error != null) {
			model.addAttribute("errorMessage", "Invalid Username or Password");
		}
		
		// Generate Captcha
		String captchaText = captchaService.generateCaptchaText();
		session.setAttribute("captcha", captchaText);
		
		return "util/login";
	}

	@GetMapping("/captcha")
	public void getCaptcha(jakarta.servlet.http.HttpSession session, jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
		String captchaText = (String) session.getAttribute("captcha");
		if (captchaText == null) {
			captchaText = captchaService.generateCaptchaText();
			session.setAttribute("captcha", captchaText);
		}
		java.awt.image.BufferedImage img = captchaService.generateCaptchaImage(captchaText);
		response.setContentType("image/png");
		javax.imageio.ImageIO.write(img, "png", response.getOutputStream());
	}

	@PostMapping("/loginreq")
	public String login(@RequestParam String username, 
						@RequestParam String password, 
						@RequestParam String captcha,
						jakarta.servlet.http.HttpSession session,
						jakarta.servlet.http.HttpServletResponse response,
						Model model) {
		
		// 1. Verify Captcha
		String sessionCaptcha = (String) session.getAttribute("captcha");
		if (sessionCaptcha == null || !sessionCaptcha.equalsIgnoreCase(captcha)) {
			return "redirect:/login?captchaError";
		}

		try {
			Authentication authentication = authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(username, password));

			if (authentication.isAuthenticated()) {
				// Generate JWT
				String token = jwtService.generateToken(username);
				Cookie cookie = new Cookie("jwt", token);
				cookie.setHttpOnly(true);
				cookie.setPath("/");
				cookie.setMaxAge(24 * 60 * 60);
				response.addCookie(cookie);

				String role = authentication.getAuthorities().stream().findFirst().get().getAuthority();

				if (role.equals("ROLE_ADMIN")) {
					return "redirect:/admin/admindashboard";
				} else if (role.equals("ROLE_FACULTY")) {
					return "redirect:/faculty/facultydashboard";
				} else if (role.equals("ROLE_STUDENT")) {
					return "redirect:/student/studentDashBoard";
				} else {
					return "redirect:/dashboard";
				}
			}
		} catch (Exception e) {
			System.out.println("Login Failed: " + e.getMessage());
		}
		
		return "redirect:/login?error";
	}

	@GetMapping("/logout")
	public String logout(HttpServletResponse response) {
		SecurityContextHolder.clearContext();
		Cookie cookie = new Cookie("jwt", null);
		cookie.setHttpOnly(true);
		cookie.setPath("/");
		cookie.setMaxAge(0);
		response.addCookie(cookie);
		return "redirect:/login?logout";
	}

	@GetMapping("/studentregister")
	public String studentRegisterPage(Model model) {
		List<Department> allDepartments = masterService.getAllDepartments();
		model.addAttribute("student", new Student());
		model.addAttribute("allDepartments", allDepartments);
		return "util/studentregister";
	}

	@GetMapping("/facultyregister")
	public String facultyRegisterPage(Model model) {
		List<Department> allDepartments = masterService.getAllDepartments();
		model.addAttribute("faculty", new com.smart.model.Faculty());
		model.addAttribute("allDepartments", allDepartments);
		return "util/facultyregister";
	}

	@PostMapping("/savefaculty")
	public String registerFaculty(@ModelAttribute com.smart.model.Faculty faculty) throws java.sql.SQLException {
		
		// Generate Username (similar logic as student if needed, or use email prefix)
		String username = faculty.getEmail().split("@")[0] + "_f";
		faculty.setUsername(username);
		
		// Default Password
		String defaultPassword = "Faculty1@";
		faculty.setPassword(passwordEncoder.encode(defaultPassword));
		
		// Set defaults for self-registration
		faculty.setDesignation("Assistant Professor"); // Default
		faculty.setJoiningDate(java.time.LocalDate.now());

		// Ensure role is FACULTY (CustomUserDetailService adds ROLE_ prefix)
		adminService.insertFaculty(faculty);

		// Send email
		emailService.sendStudentCredentials(faculty.getEmail(), faculty.getFullName(), username, defaultPassword);
		
		return "redirect:/login";
	}

	@PostMapping("/savestudent")
	public String registerStudent(@ModelAttribute Student student) {

		// Generate Student ID
		String studentId = commonFunction.generateStudentId();
		student.setStudentid(studentId);

		// Generate Username
		String username = commonFunction.generateUsername();
		student.setUsername(username);

		// Default Password
		String defaultPassword = "Welcome1@";

		// Encode password
		student.setPassword(passwordEncoder.encode(defaultPassword));

		// Save student
		studentService.saveStudent(student);

		// Send email with credentials
		emailService.sendStudentCredentials(student.getEmail(), student.getFullname(), username, defaultPassword);
		return "redirect:/login";
	}

}
