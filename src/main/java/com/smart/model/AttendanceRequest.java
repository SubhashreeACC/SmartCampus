package com.smart.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceRequest {

	private String regNo;
	private String name;
	private Double latitude;
	private Double longitude;
	private String method;
	private String sessionId;

}
