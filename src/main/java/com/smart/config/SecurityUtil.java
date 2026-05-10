package com.smart.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

	public static CustomUserDetails getCurrentUser() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails)) {
			return null;
		}

		return (CustomUserDetails) auth.getPrincipal();
	}

	public static String getCurrentUserId() {
		CustomUserDetails user = getCurrentUser();
		return (user != null) ? user.getUserRefId() : null;
	}
}
