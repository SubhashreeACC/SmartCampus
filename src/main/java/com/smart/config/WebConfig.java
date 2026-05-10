package com.smart.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
//	@Value("${product.images.path}")
//	private String imagePath;

//	@Override
//	public void addResourceHandlers(ResourceHandlerRegistry registry) {
//		// Map URL /images/** to local folder
//		registry.addResourceHandler("/image/**").addResourceLocations("file:///" + imagePath);
//	}

}
