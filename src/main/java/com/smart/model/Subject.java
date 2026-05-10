package com.smart.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Subject {

	private Integer subId;
	private String subjectName;
	private String subjectCode;
	private String deptCode;
	private String deptName;
}
