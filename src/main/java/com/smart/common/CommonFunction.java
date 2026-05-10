package com.smart.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommonFunction {

	@Autowired
	private DataSource dataSource;

	public int getStudentCount() {

		int count = 0;

		String sql = "SELECT COUNT(*) FROM student_details";

		try (Connection con = dataSource.getConnection();
				PreparedStatement ps = con.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {

			if (rs.next()) {
				count = rs.getInt(1);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return count;
	}

	public String generateStudentId() {

		String prefix = "ITER";

		int year = java.time.Year.now().getValue();

		int count = getStudentCount();

		int next = count + 1;

		String sequence = String.format("%03d", next);

		return prefix + year + sequence;
	}

	public String generateUsername() {

		String prefix = "ITER";

		int year = java.time.Year.now().getValue();

		int count = getStudentCount();

		int next = count + 1;

		String seq = String.format("%03d", next);

		return prefix + seq + year;
	}
	
	public int getFacultyCount() {

		int count = 0;

		String sql = "SELECT COUNT(*) FROM faculty_details";

		try (Connection con = dataSource.getConnection();
				PreparedStatement ps = con.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {

			if (rs.next()) {
				count = rs.getInt(1);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return count;
	}
	

	public String generateFacUsername() {

		String prefix = "SOA";

		int year = java.time.Year.now().getValue();

		int count = getFacultyCount();

		int next = count + 1;

		String seq = String.format("%03d", next);

		return prefix + seq + year;
	}

}
