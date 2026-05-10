package com.smart.serviceImpl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.smart.model.TimeTable;
import com.smart.service.FacultyService;

@Service
public class FacultyServiceImpl implements FacultyService {

	@Autowired
	private DataSource dataSource;

	@Override
	public List<TimeTable> getFacultyTimetable(String facultyId) throws SQLException {

		List<TimeTable> timetableList = new ArrayList<>();

		String sql = "SELECT " + "t.id, t.day, t.subject, t.faculty, t.start_time, t.end_time, t.room, t.dept_code, "
				+ "s.sub_id, s.subject_name, s.subject_code, " + "d.dept_id, d.dept_name, d.dept_code "
				+ "FROM smartcampus.timetable t "
				+ "LEFT JOIN smartcampus.subject_master s ON t.subject = s.subject_code "
				+ "LEFT JOIN smartcampus.department_master d ON t.dept_code = d.dept_code " + "WHERE t.faculty = ?";

		try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, facultyId);

			try (ResultSet rs = ps.executeQuery()) {

				while (rs.next()) {

					TimeTable tt = new TimeTable();

					// 🔽 Timetable fields
					tt.setId(rs.getLong("id"));
					tt.setDay(rs.getString("day"));
					tt.setSubject(rs.getString("subject"));
					tt.setFaculty(rs.getString("faculty"));
					tt.setRoom(rs.getString("room"));

					// 🔽 Time as String (since your model uses String)
					tt.setStartTime(rs.getString("start_time"));
					tt.setEndTime(rs.getString("end_time"));

					// 🔽 Department (from timetable)
					tt.setDepartment(rs.getString("dept_code"));

					// 🔽 Subject details
					tt.setSubjectId(rs.getLong("sub_id"));
					tt.setSubjectName(rs.getString("subject_name"));
					tt.setSubjectCode(rs.getString("subject_code"));

					// 🔽 Department details
					tt.setDeptId(rs.getLong("dept_id"));
					tt.setDeptName(rs.getString("dept_name"));
					tt.setDeptCode(rs.getString("dept_code"));

					// 🔽 Faculty ID (same as input)
					tt.setFacultyId(rs.getString("faculty"));

					timetableList.add(tt);
				}
			}
		}

		return timetableList;
	}

	@Override
	public int getClassesTodayCount(String facultyId) throws SQLException {
		String day = LocalDate.now().getDayOfWeek().name();
		String sql = "SELECT COUNT(*) FROM timetable WHERE faculty = ? AND UPPER(day) = UPPER(?)";
		try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, facultyId);
			ps.setString(2, day);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1);
				}
			}
		}
		return 0;
	}

	@Override
	public int getStudentCountByDept(String deptCode) throws SQLException {
		String sql = "SELECT COUNT(*) FROM student_details WHERE department = ?";
		try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, deptCode);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1);
				}
			}
		}
		return 0;
	}

	@Override
	public int getSubjectCountByFaculty(String facultyId) throws SQLException {
		String sql = "SELECT COUNT(DISTINCT subject) FROM timetable WHERE faculty = ?";
		try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, facultyId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1);
				}
			}
		}
		return 0;
	}

	@Override
	public List<com.smart.model.Student> getUngradedStudents(String deptCode, String subjectCode, int semester,
			String academicYear) throws SQLException {
		List<com.smart.model.Student> students = new ArrayList<>();
		String sql = "SELECT student_id, fullname, email, department FROM student_details " +
				"WHERE department = ? AND student_id NOT IN (" +
				"  SELECT student_id FROM student_grades " +
				"  WHERE subject_code = ? AND semester = ? AND academic_year = ?" +
				")";
		try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, deptCode);
			ps.setString(2, subjectCode);
			ps.setInt(3, semester);
			ps.setString(4, academicYear);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					com.smart.model.Student s = new com.smart.model.Student();
					s.setStudentid(rs.getString("student_id"));
					s.setFullname(rs.getString("fullname"));
					s.setEmail(rs.getString("email"));
					s.setDeptCode(rs.getString("department"));
					students.add(s);
				}
			}
		}
		return students;
	}

	@Override
	public void saveGrade(com.smart.model.Grade grade) throws SQLException {
		// Ensure table exists first
		String createTableSql = "CREATE TABLE IF NOT EXISTS student_grades (" +
				"id INT AUTO_INCREMENT PRIMARY KEY, " +
				"student_id VARCHAR(50), " +
				"subject_code VARCHAR(50), " +
				"grade VARCHAR(10), " +
				"semester INT, " +
				"academic_year VARCHAR(20), " +
				"UNIQUE KEY unique_grade (student_id, subject_code)" +
				")";

		String sql = "INSERT INTO student_grades (student_id, subject_code, grade, semester, academic_year) " +
				"VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE grade = ?";

		try (Connection conn = dataSource.getConnection()) {
			// 1. Create table
			try (PreparedStatement cps = conn.prepareStatement(createTableSql)) {
				cps.executeUpdate();
			}

			// 2. Insert/Update grade
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.setString(1, grade.getStudentId());
				ps.setString(2, grade.getSubjectCode());
				ps.setString(3, grade.getGrade());
				ps.setInt(4, grade.getSemester());
				ps.setString(5, grade.getAcademicYear());
				ps.setString(6, grade.getGrade());
				ps.executeUpdate();
			}
		}
	}

	@Override
	public List<Map<String, Object>> getGradesByStudent(String studentId) throws SQLException {
		List<Map<String, Object>> grades = new ArrayList<>();
		String sql = "SELECT g.*, s.subject_name FROM student_grades g " +
				"LEFT JOIN subject_master s ON g.subject_code = s.subject_code " +
				"WHERE g.student_id = ? ORDER BY g.semester DESC";
		try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, studentId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Map<String, Object> row = new HashMap<>();
					row.put("subjectCode", rs.getString("subject_code"));
					row.put("subjectName", rs.getString("subject_name"));
					row.put("grade", rs.getString("grade"));
					row.put("semester", rs.getInt("semester"));
					row.put("academicYear", rs.getString("academic_year"));
					grades.add(row);
				}
			}
		}
		return grades;
	}

}