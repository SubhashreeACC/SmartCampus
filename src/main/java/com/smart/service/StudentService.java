package com.smart.service;

import java.sql.SQLException;
import java.util.List;

import com.smart.model.Student;
import com.smart.model.TimeTable;

public interface StudentService {

	public void saveStudent(Student student);

	public List<TimeTable> getStudentTimetable(String deptCode) throws SQLException;
	
	public int getTotalStudentCount() throws SQLException;

}
