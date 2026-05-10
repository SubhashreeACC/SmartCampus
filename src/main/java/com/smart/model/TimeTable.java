package com.smart.model;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimeTable {

	private Long id;
	private String day;
	private String subject;
	private String faculty;
	private String startTime;
	private String endTime;
	private String room;
	private String department;

	private String facultyId;

	private Long subjectId;
	private String subjectName;
	private String subjectCode;

	private Long deptId;
	private String deptName;
	private String deptCode;
	private String facultyName;

}
