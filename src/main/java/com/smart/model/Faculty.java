package com.smart.model;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Faculty {

	private Long id;
	private String fullName;
	private String email;
	private String phone;
	private String gender; // Male, Female
	private Long departmentId; // Maps to selected department
	private String designation;
	private LocalDate joiningDate;
	private String address;
	private String role;
	private byte[] profilePicture;
	private String deptCode;
	private String username;
	private String password;
	private String deptName;

}
