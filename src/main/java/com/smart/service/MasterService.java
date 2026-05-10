package com.smart.service;

import java.util.List;

import com.smart.model.Department;
import com.smart.model.Subject;

public interface MasterService {

	public List<Department> getAllDepartments();

	public void saveDepartment(Department dept);

	void updateDepartment(Department dept);

	void deleteDepartment(int id);
	
	public List<Subject> getAllSubjects();
	
	public List<Subject> getAllSubjectsByDeptCode(String deptcode);
	
	public void saveSubject(Subject subject);
	
	public void updateSubject(Long subjectId, String newSubjectName);
	
	public void deleteSubject(Long subjectId);
	
	public int getDepartmentCount();
	
	public int getSubjectCount();

}
