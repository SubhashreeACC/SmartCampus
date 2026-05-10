package com.smart.serviceImpl;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.smart.model.Faculty;
import com.smart.model.TimeTable;
import com.smart.service.AdminService;

@Service
public class AdminServiceImpl implements AdminService {

	@Autowired
	private DataSource dataSource;

	@Override
	public void insertFaculty(Faculty faculty) throws SQLException {
		String userSql = "INSERT INTO users(username, password, role, is_active) VALUES(?, ?, ?, ?)";
		String facultySql = "INSERT INTO faculty_details "
				+ "(user_id, full_name, designation, email, phone, gender, joining_date, dept_code, address, profile_picture) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		try (Connection conn = dataSource.getConnection()) {
			conn.setAutoCommit(false);

			try (PreparedStatement psUser = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS);
					PreparedStatement psFaculty = conn.prepareStatement(facultySql)) {

				// --- Insert into users table ---
				psUser.setString(1, faculty.getUsername());
				psUser.setString(2, faculty.getPassword());
				psUser.setString(3, "FACULTY");
				psUser.setString(4, "Y");
				psUser.executeUpdate();

				// --- Get generated user ID ---
				int userId;
				try (ResultSet generatedKeys = psUser.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						userId = generatedKeys.getInt(1);
					} else {
						throw new SQLException("Failed to retrieve generated user ID.");
					}
				}

				// --- Insert into faculty_details with user_id ---
				psFaculty.setInt(1, userId);
				psFaculty.setString(2, faculty.getFullName());
				psFaculty.setString(3, faculty.getDesignation());
				psFaculty.setString(4, faculty.getEmail());
				psFaculty.setString(5, faculty.getPhone());
				psFaculty.setString(6, faculty.getGender());

				if (faculty.getJoiningDate() != null) {
					psFaculty.setDate(7, Date.valueOf(faculty.getJoiningDate()));
				} else {
					psFaculty.setNull(7, java.sql.Types.DATE);
				}

				psFaculty.setString(8, faculty.getDeptCode());
				psFaculty.setString(9, faculty.getAddress());
				psFaculty.setBytes(10, faculty.getProfilePicture());
				psFaculty.executeUpdate();

				conn.commit();

			} catch (SQLException e) {
				conn.rollback();
				throw e;
			} finally {
				conn.setAutoCommit(true);
			}
		}
	}

	@Override
	public List<Faculty> getAllFaculty() throws SQLException {
		List<Faculty> facultyList = new ArrayList<>();

		String sql = "SELECT faculty_id, full_name, designation, email, phone, gender, joining_date, dm.dept_name, address FROM faculty_details fd\r\n"
				+ "left outer join department_master dm on dm.dept_code= fd.dept_code ";

		try (Connection conn = dataSource.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {
				Faculty faculty = new Faculty();

				faculty.setId(rs.getLong("faculty_id"));
				faculty.setFullName(rs.getString("full_name"));
				faculty.setDesignation(rs.getString("designation"));
				faculty.setEmail(rs.getString("email"));
				faculty.setPhone(rs.getString("phone"));
				faculty.setGender(rs.getString("gender"));

				if (rs.getDate("joining_date") != null) {
					faculty.setJoiningDate(rs.getDate("joining_date").toLocalDate());
				}

				faculty.setDeptName(rs.getString("dept_name"));
				faculty.setAddress(rs.getString("address"));

				facultyList.add(faculty);
			}
		}

		return facultyList;
	}

	@Override
	public List<Faculty> getAllFacultyByDeptId(String deptCode) throws SQLException {

		List<Faculty> facultyList = new ArrayList<>();

		String sql = "SELECT faculty_id, full_name, designation, email, phone, gender, joining_date, dm.dept_name, address "
				+ "FROM faculty_details fd " + "LEFT OUTER JOIN department_master dm ON dm.dept_code = fd.dept_code "
				+ "WHERE fd.dept_code = ?";

		try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			// ✅ Set parameter
			ps.setString(1, deptCode);

			try (ResultSet rs = ps.executeQuery()) {

				while (rs.next()) {
					Faculty faculty = new Faculty();

					faculty.setId(rs.getLong("faculty_id"));
					faculty.setFullName(rs.getString("full_name"));
					faculty.setDesignation(rs.getString("designation"));
					faculty.setEmail(rs.getString("email"));
					faculty.setPhone(rs.getString("phone"));
					faculty.setGender(rs.getString("gender"));

					if (rs.getDate("joining_date") != null) {
						faculty.setJoiningDate(rs.getDate("joining_date").toLocalDate());
					}

					faculty.setDeptName(rs.getString("dept_name"));
					faculty.setAddress(rs.getString("address"));

					facultyList.add(faculty);
				}
			}
		}

		return facultyList;
	}

	@Override
	public void saveTime(TimeTable tt) {

		String sql = "INSERT INTO timetable (day, subject, faculty, start_time, end_time, room, dept_code ) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?)";

		try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, tt.getDay());
			ps.setString(2, tt.getSubject());
			ps.setString(3, tt.getFaculty());
			ps.setString(4, tt.getStartTime());
			ps.setString(5, tt.getEndTime());
			ps.setString(6, tt.getRoom());
			ps.setString(7, tt.getDepartment());

			ps.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<TimeTable> getAllTime() {

		List<TimeTable> list = new ArrayList<>();

		String sql = "SELECT * FROM timetable " + "Left join subject_master sm on timetable.subject=sm.subject_code "
				+ "left outer join faculty_details fd on timetable.faculty=fd.faculty_id "
				+ "left join department_master dm on sm.dept_code=dm.dept_code ORDER BY day, start_time ";

		try (Connection conn = dataSource.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {

				TimeTable tt = new TimeTable();

				tt.setId(rs.getLong("id"));
				tt.setDay(rs.getString("day"));
				tt.setDepartment(rs.getString("dept_name"));
				tt.setSubject(rs.getString("subject_name"));
				tt.setFaculty(rs.getString("full_name"));
				tt.setStartTime(rs.getString("start_time"));
				tt.setEndTime(rs.getString("end_time"));
				tt.setRoom(rs.getString("room"));

				list.add(tt);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return list;
	}

	@Override
	public void deleteTimeById(int id) {

		String sql = "DELETE FROM timetable WHERE id = ?";

		try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, id);
			ps.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public int getFacultyCount() throws SQLException {
		String sql = "SELECT COUNT(*) FROM faculty_details";
		try (Connection conn = dataSource.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			if (rs.next()) {
				return rs.getInt(1);
			}
		}
		return 0;
	}

	@Override
	public int getStudentCount() throws SQLException {
		String sql = "SELECT COUNT(*) FROM student_details";
		try (Connection conn = dataSource.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			if (rs.next()) {
				return rs.getInt(1);
			}
		}
		return 0;
	}
}
