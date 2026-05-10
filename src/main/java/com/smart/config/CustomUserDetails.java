package com.smart.config;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class CustomUserDetails implements UserDetails {

	private String username;
	private String password;
	private boolean enabled;
	private Collection<? extends GrantedAuthority> authorities;

	// 🔽 NEW FIELDS
	private String userRefId; // faculty_id OR student_id
	private String fullName;
	private String department;
	private String role;

	// 🔹 Constructor for ADMIN (no extra data)
	public CustomUserDetails(String username, String password, boolean enabled,
			Collection<? extends GrantedAuthority> authorities) {
		this.username = username;
		this.password = password;
		this.enabled = enabled;
		this.authorities = authorities;
	}

	// 🔹 Constructor for STUDENT / FACULTY
	public CustomUserDetails(String username, String password, boolean enabled,
			Collection<? extends GrantedAuthority> authorities, String userRefId, String fullName, String department,
			String role) {

		this.username = username;
		this.password = password;
		this.enabled = enabled;
		this.authorities = authorities;
		this.userRefId = userRefId;
		this.fullName = fullName;
		this.department = department;
		this.role = role;
	}

	// 🔽 Getters for new fields
	public String getUserRefId() {
		return userRefId;
	}

	public String getFullName() {
		return fullName;
	}

	public String getDepartment() {
		return department;
	}

	public String getRole() {
		return role;
	}

	public String getRoleDisplay() {
		if (role == null) return "User";
		return role.substring(0, 1).toUpperCase() + role.substring(1).toLowerCase();
	}

	// 🔽 Existing methods
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}
}