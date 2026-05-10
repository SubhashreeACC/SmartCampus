package com.smart.config;

public class SecurityConstants {

	// URL Patterns
	public static final String[] PUBLIC_URLS = { "/", "/index", "/login", "/loginreq", "/studentregister/**", "/savestudent/**",
			"/facultyregister/**", "/savefaculty/**", "/captcha",
			"/questions", "/answer", "/shopform", "/saveshop", "/forgotpassword", "/verifyotp", "/addotp",
			"/resetpassword", "/updatepassword", "/delregister", "/api/ai/chat", "/attendance/mark/**",
			"/attendance/student/**", "/attendance/mark", "/api/chatbot/**" };
	public static final String ADMIN_URL = "/admin/**";
	public static final String[] FACULTY_URL = { "/faculty/**", "/attendance/**" };
	public static final String STUDENT_URL = "/student/**";
	public static final String[] STATIC_RESOURCES = { "/css/**", "/js/**", "/images/**", "/webjars/**" };
}
