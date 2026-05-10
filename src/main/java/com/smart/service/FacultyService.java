package com.smart.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.smart.model.TimeTable;

public interface FacultyService {

	public List<TimeTable> getFacultyTimetable(String facultyId) throws SQLException;

	public int getClassesTodayCount(String facultyId) throws SQLException;

	public int getStudentCountByDept(String deptCode) throws SQLException;

	public int getSubjectCountByFaculty(String facultyId) throws SQLException;

	public List<com.smart.model.Student> getUngradedStudents(String deptCode, String subjectCode, int semester, String academicYear) throws SQLException;

	public void saveGrade(com.smart.model.Grade grade) throws SQLException;

	public List<Map<String, Object>> getGradesByStudent(String studentId) throws SQLException;

}
