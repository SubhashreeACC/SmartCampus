package com.smart.serviceImpl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.smart.model.Department;
import com.smart.model.Subject;
import com.smart.service.MasterService;

@Service
public class MasterServiceImpl implements MasterService {

	@Autowired
	private DataSource dataSource;

	@Override
	public void saveDepartment(Department dept) {

		String getLastCode = "SELECT dept_code FROM department_master ORDER BY dept_id DESC LIMIT 1";
		String insertSql = "INSERT INTO department_master(dept_name, dept_code) VALUES (?, ?)";

		try (Connection con = dataSource.getConnection()) {

			String nextCode = "001";

			// Get last code
			try (PreparedStatement ps = con.prepareStatement(getLastCode); ResultSet rs = ps.executeQuery()) {

				if (rs.next()) {

					String lastCode = rs.getString("dept_code"); // ex: 001
					int num = Integer.parseInt(lastCode);
					num++;

					nextCode = String.format("%03d", num); // convert to 002,003
				}
			}

			// Insert department
			try (PreparedStatement ps = con.prepareStatement(insertSql)) {

				ps.setString(1, dept.getDeptName());
				ps.setString(2, nextCode);

				ps.executeUpdate();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<Department> getAllDepartments() {

		List<Department> list = new ArrayList<>();

		String sql = "SELECT * FROM department_master";

		try (Connection con = dataSource.getConnection();
				PreparedStatement ps = con.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {

				Department d = new Department();

				d.setDeptId(rs.getInt("dept_id"));
				d.setDeptName(rs.getString("dept_name"));
				d.setDeptCode(rs.getString("dept_code"));

				list.add(d);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return list;
	}

	@Override
	public void updateDepartment(Department dept) {

		String sql = "UPDATE department_master SET dept_name=? WHERE dept_id=?";

		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setString(1, dept.getDeptName());
			ps.setInt(2, dept.getDeptId());

			ps.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void deleteDepartment(int id) {

		String sql = "DELETE FROM department_master WHERE dept_id=?";

		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setInt(1, id);
			ps.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<Subject> getAllSubjects() {

		List<Subject> list = new ArrayList<>();

		String sql = "SELECT sm.sub_id, sm.subject_name, sm.subject_code, sm.dept_code, dm.dept_name "
				+ "FROM subject_master sm " + "LEFT JOIN department_master dm ON sm.dept_code = dm.dept_code";

		try (Connection con = dataSource.getConnection();
				PreparedStatement ps = con.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {

				Subject sub = new Subject();
				sub.setSubId(rs.getInt("sub_id"));
				sub.setSubjectName(rs.getString("subject_name"));
				sub.setSubjectCode(rs.getString("subject_code"));
				sub.setDeptCode(rs.getString("dept_code"));
				sub.setDeptName(rs.getString("dept_name"));
				list.add(sub);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return list;
	}

	@Override
	public List<Subject> getAllSubjectsByDeptCode(String deptcode) {

		List<Subject> list = new ArrayList<>();

		String sql = "SELECT sm.sub_id, sm.subject_name, sm.subject_code, sm.dept_code, dm.dept_name "
				+ "FROM subject_master sm " + "LEFT JOIN department_master dm ON sm.dept_code = dm.dept_code "
				+ "WHERE sm.dept_code = ?";

		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

			// ✅ Set parameter dynamically
			ps.setString(1, deptcode);

			try (ResultSet rs = ps.executeQuery()) {

				while (rs.next()) {

					Subject sub = new Subject();
					sub.setSubId(rs.getInt("sub_id"));
					sub.setSubjectName(rs.getString("subject_name"));
					sub.setSubjectCode(rs.getString("subject_code"));
					sub.setDeptCode(rs.getString("dept_code"));
					sub.setDeptName(rs.getString("dept_name"));

					list.add(sub);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return list;
	}

	@Override
	public void saveSubject(Subject subject) {

		String getLastCode = "SELECT subject_code FROM subject_master ORDER BY sub_id DESC LIMIT 1";
		String insertSql = "INSERT INTO subject_master(subject_name, subject_code,dept_code) VALUES (?, ?, ?)";

		try (Connection con = dataSource.getConnection()) {

			String nextCode = "001";

			// Get last code
			try (PreparedStatement ps = con.prepareStatement(getLastCode); ResultSet rs = ps.executeQuery()) {

				if (rs.next()) {

					String lastCode = rs.getString("subject_code"); // ex: 001
					int num = Integer.parseInt(lastCode);
					num++;

					nextCode = String.format("%03d", num); // convert to 002,003
				}
			}

			// Insert department
			try (PreparedStatement ps = con.prepareStatement(insertSql)) {

				ps.setString(1, subject.getSubjectName());
				ps.setString(2, nextCode);
				ps.setString(3, subject.getDeptCode());
				ps.executeUpdate();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void updateSubject(Long subjectId, String newSubjectName) {
		String updateSql = "UPDATE subject_master SET subject_name = ? WHERE sub_id = ?";

		try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(updateSql)) {

			ps.setString(1, newSubjectName); // Set new subject name
			ps.setLong(2, subjectId); // Set the ID of the subject to update

			ps.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();

		}
	}

	@Override
	public void deleteSubject(Long subjectId) {
		String deleteSql = "DELETE FROM subject_master WHERE sub_id = ?";

		try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(deleteSql)) {

			ps.setLong(1, subjectId);
			ps.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public int getDepartmentCount() {
		String sql = "SELECT COUNT(*) FROM department_master";
		try (Connection con = dataSource.getConnection();
				PreparedStatement ps = con.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public int getSubjectCount() {
		String sql = "SELECT COUNT(*) FROM subject_master";
		try (Connection con = dataSource.getConnection();
				PreparedStatement ps = con.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

}
