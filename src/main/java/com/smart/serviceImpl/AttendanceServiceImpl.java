package com.smart.serviceImpl;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.smart.model.AttendanceRequest;
import com.smart.service.AttendanceService;

@Service
public class AttendanceServiceImpl implements AttendanceService {

	@Autowired
	private DataSource dataSource;

	@Override
	public Map<String, Object> startSession(Map<String, Object> body) throws Exception {

		String classCode = (String) body.get("classCode");
		String className = (String) body.get("className");
		int validityMin = Integer.parseInt(body.get("validity").toString());
		String baseUrl = body.containsKey("baseUrl") ? (String) body.get("baseUrl") : "http://localhost:8080";

		String sessionId = "SC" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

		// Build QR URL using dynamic server base URL so students can scan from mobile
		String qrData = baseUrl + "/attendance/mark?sessionId=" + sessionId;

		String sql = "INSERT INTO attendance_session "
				+ "(session_id, class_code, class_name, date, start_time, validity_minutes, active) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?)";

		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setString(1, sessionId);
			ps.setString(2, classCode);
			ps.setString(3, className);
			ps.setDate(4, Date.valueOf(LocalDate.now()));
			ps.setTime(5, Time.valueOf(LocalTime.now()));
			ps.setInt(6, validityMin);
			ps.setBoolean(7, true);

			ps.executeUpdate();
		}

		return Map.of("sessionId", sessionId, "qrData", qrData, "message", "Session started");
	}

	@Override
	public Map<String, Object> endSession(String sessionId) throws Exception {

		String sql = "UPDATE attendance_session SET active = 0, end_time = ? WHERE session_id = ?";

		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setTime(1, Time.valueOf(LocalTime.now()));
			ps.setString(2, sessionId);

			int updated = ps.executeUpdate();
			if (updated == 0)
				throw new RuntimeException("Session not found");
		}

		return Map.of("message", "Session ended");
	}

	@Override
	public Map<String, Object> markAttendance(AttendanceRequest req) throws Exception {

		try (Connection con = dataSource.getConnection()) {

			// ✅ 1. Check student exists
			String studentSql = "SELECT fullname FROM student_details WHERE student_id = ?";
			String studentName = null;

			try (PreparedStatement ps = con.prepareStatement(studentSql)) {
				ps.setString(1, req.getRegNo());
				ResultSet rs = ps.executeQuery();

				if (rs.next()) {
					studentName = rs.getString("fullname");
				} else {
					return Map.of("success", false, "message", "Invalid Student ID");
				}
			}

			// ✅ 2. Check duplicate attendance (same session)
			String checkSql = "SELECT COUNT(*) FROM attendance WHERE reg_no=? AND session_id=?";
			try (PreparedStatement ps = con.prepareStatement(checkSql)) {
				ps.setString(1, req.getRegNo());
				ps.setString(2, req.getSessionId());

				ResultSet rs = ps.executeQuery();
				if (rs.next() && rs.getInt(1) > 0) {
					return Map.of("success", false, "message", "Already marked for this session");
				}
			}

			// ✅ 3. Insert attendance
			String insertSql = "INSERT INTO attendance "
					+ "(reg_no, name, date, time, latitude, longitude, method, session_id) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

			try (PreparedStatement ps = con.prepareStatement(insertSql)) {

				ps.setString(1, req.getRegNo());
				ps.setString(2, studentName); // ✅ auto name

				ps.setDate(3, Date.valueOf(LocalDate.now()));

				// ✅ FIX: Proper time format (HH:mm:ss)
				String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
				ps.setString(4, time);

				ps.setObject(5, req.getLatitude());
				ps.setObject(6, req.getLongitude());
				ps.setString(7, req.getMethod());
				ps.setString(8, req.getSessionId());

				ps.executeUpdate();
			}

			return Map.of("success", true, "message", "Attendance marked", "name", studentName);
		}
	}

	// ✅ GET STUDENT DETAILS (AUTO FILL)
	@Override
	public Map<String, Object> getStudentById(String studentId) throws Exception {

		try (Connection con = dataSource.getConnection()) {

			String sql = "SELECT fullname FROM student_details WHERE student_id=?";

			try (PreparedStatement ps = con.prepareStatement(sql)) {
				ps.setString(1, studentId);
				ResultSet rs = ps.executeQuery();

				if (rs.next()) {
					return Map.of("success", true, "name", rs.getString("fullname"));
				} else {
					return Map.of("success", false, "message", "Invalid Student ID");
				}
			}
		}
	}

	@Override
	public List<Map<String, Object>> getBySession(String sessionId) throws Exception {

		List<Map<String, Object>> list = new ArrayList<>();
		String sql = "SELECT * FROM attendance WHERE session_id=?";

		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setString(1, sessionId);

			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				Map<String, Object> row = new HashMap<>();
				row.put("regNo", rs.getString("reg_no"));
				row.put("name", rs.getString("name"));
				row.put("date", rs.getDate("date"));
				row.put("time", rs.getString("time"));
				row.put("latitude", rs.getDouble("latitude"));
				row.put("longitude", rs.getDouble("longitude"));
				row.put("method", rs.getString("method"));

				list.add(row);
			}
		}

		return list;
	}

	@Override
	public List<Map<String, Object>> getAll() throws Exception {

		List<Map<String, Object>> list = new ArrayList<>();
		String sql = "SELECT a.*, s.class_name, s.class_code FROM attendance a LEFT JOIN attendance_session s ON a.session_id = s.session_id ORDER BY a.date DESC, a.time DESC";

		try (Connection con = dataSource.getConnection();
				PreparedStatement ps = con.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {
				Map<String, Object> row = new HashMap<>();
				row.put("regNo", rs.getString("reg_no"));
				row.put("name", rs.getString("name"));
				row.put("sessionId", rs.getString("session_id"));
				row.put("className", rs.getString("class_name"));
				row.put("classCode", rs.getString("class_code"));
				row.put("date", rs.getDate("date"));
				row.put("time", rs.getString("time"));
				row.put("method", rs.getString("method"));
				row.put("latitude", rs.getDouble("latitude"));
				row.put("longitude", rs.getDouble("longitude"));
				list.add(row);
			}
		}

		return list;
	}

	@Override
	public List<Map<String, Object>> getByStudent(String regNo) throws Exception {
		List<Map<String, Object>> list = new ArrayList<>();
		String sql = "SELECT a.*, s.class_name, s.class_code FROM attendance a LEFT JOIN attendance_session s ON a.session_id = s.session_id WHERE a.reg_no = ? ORDER BY a.date DESC, a.time DESC";

		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setString(1, regNo);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Map<String, Object> row = new HashMap<>();
					row.put("regNo", rs.getString("reg_no"));
					row.put("name", rs.getString("name"));
					row.put("sessionId", rs.getString("session_id"));
					row.put("className", rs.getString("class_name"));
					row.put("classCode", rs.getString("class_code"));
					row.put("date", rs.getDate("date"));
					row.put("time", rs.getString("time"));
					row.put("method", rs.getString("method"));
					row.put("latitude", rs.getDouble("latitude"));
					row.put("longitude", rs.getDouble("longitude"));
					list.add(row);
				}
			}
		}

		return list;
	}

}
