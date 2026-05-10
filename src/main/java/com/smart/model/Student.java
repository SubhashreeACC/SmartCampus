package com.smart.model;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Student {

	private String fullname;
	private String username;
	private String studentid;
	private String email;
	private String year;
	private String phone;
	private String mobile; // Added for registration form
	private LocalDate dob;
	private String deptCode;
	private String deptName;
	private String department; // Added for registration form
	private String gender;
	private String nationality;
	private String address;
	private String guardianName;
	private String guardianPhone;
	private String password;

}
