package com.smart.serviceImpl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.smart.model.Student;
import com.smart.model.TimeTable;
import com.smart.service.StudentService;

@Service
public class StudentServiceImpl implements StudentService {

	@Autowired
	private DataSource dataSource;

	@Override
	public void saveStudent(Student student) {

		String userSql = "INSERT INTO users(username,password,role,is_active) VALUES(?,?,?,?)";
		String studentSql = """
				INSERT INTO student_details
				(fullname, student_id, email, year_of_study, phone, dob, department,
				gender, nationality, address, guardian_name, guardian_phone, user_id)
				VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)
				""";

		try (Connection con = dataSource.getConnection()) {
			con.setAutoCommit(false); 

			// 1. Insert user
			int userId = 0;
			try (PreparedStatement psUser = con.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS)) {
				psUser.setString(1, student.getUsername());
				psUser.setString(2, student.getPassword());
				psUser.setString(3, "STUDENT"); // CustomUserDetailService adds ROLE_ prefix
				psUser.setString(4, "Y");
				psUser.executeUpdate();

				try (ResultSet rs = psUser.getGeneratedKeys()) {
					if (rs.next()) {
						userId = rs.getInt(1);
					}
				}
			}

			// 2. Insert student details
			try (PreparedStatement psStudent = con.prepareStatement(studentSql)) {
				psStudent.setString(1, student.getFullname());
				psStudent.setString(2, student.getStudentid());
				psStudent.setString(3, student.getEmail());
				psStudent.setString(4, student.getYear());
				psStudent.setString(5, student.getMobile()); // Used mobile from form
				if (student.getDob() != null) {
					psStudent.setDate(6, java.sql.Date.valueOf(student.getDob()));
				} else {
					psStudent.setNull(6, java.sql.Types.DATE);
				}
				psStudent.setString(7, student.getDepartment()); // Used department from form
				psStudent.setString(8, student.getGender());
				psStudent.setString(9, student.getNationality());
				psStudent.setString(10, student.getAddress());
				psStudent.setString(11, student.getGuardianName());
				psStudent.setString(12, student.getGuardianPhone());
				psStudent.setInt(13, userId);
				psStudent.executeUpdate();
			}

			con.commit(); 

		} catch (Exception e) {
			// Rollback logic is handled by the try-with-resources if we add a rollback call here
			throw new RuntimeException("Failed to save student: " + e.getMessage(), e);
		}
	}

	@Override
	public List<TimeTable> getStudentTimetable(String deptCode) throws SQLException {

		List<TimeTable> timetableList = new ArrayList<>();

		String sql = "SELECT dm.dept_name, tm.day, sm.subject_name, tm.start_time, tm.end_time, "
				+ "tm.room, fd.full_name " + "FROM timetable tm "
				+ "LEFT JOIN department_master dm ON dm.dept_code = tm.dept_code "
				+ "LEFT JOIN subject_master sm ON sm.subject_code = tm.subject "
				+ "LEFT JOIN faculty_details fd ON fd.faculty_id = tm.faculty " + "WHERE tm.dept_code = ? OR dm.dept_name = ?";

		try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, deptCode);
			ps.setString(2, deptCode);

			try (ResultSet rs = ps.executeQuery()) {

				while (rs.next()) {

					TimeTable tt = new TimeTable();

					// 🔽 Basic timetable fields
					tt.setDay(rs.getString("day"));
					tt.setRoom(rs.getString("room"));
					tt.setStartTime(rs.getString("start_time"));
					tt.setEndTime(rs.getString("end_time"));

					// 🔽 Department
					tt.setDeptName(rs.getString("dept_name"));

					// 🔽 Subject
					tt.setSubjectName(rs.getString("subject_name"));

					// 🔽 Faculty (for students view)
					tt.setFacultyName(rs.getString("full_name"));

					timetableList.add(tt);
				}
			}
		}

		return timetableList;
	}

	@Override
	public int getTotalStudentCount() throws SQLException {
		String sql = "SELECT COUNT(*) FROM users WHERE role = 'ROLE_STUDENT'";
		try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
			if (rs.next()) {
				return rs.getInt(1);
			}
		}
		return 0;
	}

}
