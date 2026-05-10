package com.smart.service;

import java.sql.SQLException;
import java.util.List;

import com.smart.model.Faculty;
import com.smart.model.TimeTable;

public interface AdminService {

	public void insertFaculty(Faculty faculty) throws SQLException;

	public List<Faculty> getAllFaculty() throws SQLException;

	public void saveTime(TimeTable tt);

	public List<TimeTable> getAllTime();

	public void deleteTimeById(int id);

	public List<Faculty> getAllFacultyByDeptId(String deptCode) throws SQLException;

	public int getFacultyCount() throws SQLException;

	public int getStudentCount() throws SQLException;
}
