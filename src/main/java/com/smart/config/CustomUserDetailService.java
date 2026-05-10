package com.smart.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class CustomUserDetailService implements UserDetailsService {

	@Autowired
	private DataSource dataSource;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		String sql = "SELECT id,username, password, role, is_active FROM users WHERE username=?";

		try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, username);

			try (ResultSet rs = ps.executeQuery()) {

				if (!rs.next()) {
					throw new UsernameNotFoundException("User not found");
				}

				int id = rs.getInt("id");
				String dbUsername = rs.getString("username");
				String dbPassword = rs.getString("password");
				String role = rs.getString("role");
				boolean active = "Y".equalsIgnoreCase(rs.getString("is_active"));

				// ✅ Robust role handling: prevent ROLE_ROLE_ prefixing
				String finalRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;
				List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(finalRole));
				
				System.out.println(role);

				// 🔽 FACULTY
				if ("FACULTY".equalsIgnoreCase(role)) {

					String facultySql = "SELECT faculty_id, full_name, dept_code FROM faculty_details WHERE user_id = ?";

					try (PreparedStatement fps = conn.prepareStatement(facultySql)) {
						fps.setInt(1, id);

						try (ResultSet frs = fps.executeQuery()) {
							if (frs.next()) {

								String facultyId = frs.getString("faculty_id");
								String fullName = frs.getString("full_name");
								String dept = frs.getString("dept_code");

								System.out.println("LOGIN FACULTY ID: " + facultyId); // ✅ DEBUG

								return new CustomUserDetails(dbUsername, dbPassword, active, authorities, facultyId,
										fullName, dept, "FACULTY");
							}
						}
					}
				}

				// 🔽 STUDENT
				else if ("STUDENT".equalsIgnoreCase(role)) {

					String studentSql = "SELECT id, student_id, fullname, department FROM student_details WHERE user_id = ?";

					try (PreparedStatement sps = conn.prepareStatement(studentSql)) {
						sps.setInt(1, id);

						try (ResultSet srs = sps.executeQuery()) {
							if (srs.next()) {

								// Use student_id (reg no like 21CS045) so it matches reg_no in attendance table
								String studentRegNo = srs.getString("student_id");
								String fullName = srs.getString("fullname");
								String dept = srs.getString("department");

								return new CustomUserDetails(dbUsername, dbPassword, active, authorities, studentRegNo,
										fullName, dept, "STUDENT");
							}
						}
					}
				}

				// 🔽 ADMIN (no extra data)
				return new CustomUserDetails(dbUsername, dbPassword, active, authorities);

			}

		} catch (SQLException e) {
			throw new UsernameNotFoundException("Database error", e);
		}
	}

}
