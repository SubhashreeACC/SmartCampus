package com.smart.service;

import java.util.List;
import java.util.Map;

import com.smart.model.AttendanceRequest;

public interface AttendanceService {

	public Map<String, Object> startSession(Map<String, Object> body) throws Exception;

	public Map<String, Object> endSession(String sessionId) throws Exception;

	public Map<String, Object> markAttendance(AttendanceRequest req) throws Exception;

	public List<Map<String, Object>> getBySession(String sessionId) throws Exception;

	public List<Map<String, Object>> getAll() throws Exception;
	
	Map<String, Object> getStudentById(String studentId) throws Exception;

	public List<Map<String, Object>> getByStudent(String regNo) throws Exception;

}
