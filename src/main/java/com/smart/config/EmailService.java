package com.smart.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

	@Autowired
	private JavaMailSender mailSender;

	public void sendStudentCredentials(String toEmail, String studentName, String username, String password) {

		try {

			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

			helper.setTo(toEmail);
			helper.setSubject("SmartCampus AI | Your Student Portal Login Credentials");

			helper.setText(buildStudentEmailHtml(studentName, username, password), true);

			mailSender.send(message);

			System.out.println("Student credential email sent to: " + toEmail);

		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	private String buildStudentEmailHtml(String name, String username, String password) {

		return """
				    <div style="font-family: 'Segoe UI', Arial, sans-serif; max-width:600px; margin:auto; border-radius:10px; overflow:hidden; border:1px solid #e0e0e0">

				        <!-- HEADER -->
				        <div style="background: linear-gradient(135deg,#6a11cb,#2575fc); padding:30px; text-align:center;">
				            <h1 style="color:white; margin:0;">SmartCampus AI</h1>
				            <p style="color:rgba(255,255,255,0.9); margin-top:5px;">
				                Student Portal Access
				            </p>
				        </div>

				        <!-- BODY -->
				        <div style="padding:30px; background:#ffffff;">

				            <p>Hello <b>%s</b>,</p>

				            <p>
				                Welcome to <b>SmartCampus AI</b>! 🎓
				                Your student account has been successfully created.
				            </p>

				            <p>Please use the credentials below to login to the portal:</p>

				            <!-- LOGIN BOX -->
				            <div style="margin:25px 0; padding:25px; background:#f5f7ff; border-radius:8px; border:1px dashed #2575fc; text-align:center;">

				                <p style="margin:5px 0; font-size:14px; color:#555;">Username</p>
				                <h2 style="margin:5px 0; color:#2575fc;">%s</h2>

				                <p style="margin:15px 0 5px; font-size:14px; color:#555;">Password</p>
				                <h2 style="margin:5px 0; color:#6a11cb;">%s</h2>

				            </div>

				            <p style="color:#e74c3c;">
				                ⚠ For security reasons, please change your password after your first login.
				            </p>

				            <div style="text-align:center; margin:30px 0;">
				                <a href="http://localhost:8080/login"
				                   style="background:#2575fc; color:white; padding:12px 25px;
				                          text-decoration:none; border-radius:6px; font-weight:bold;">
				                   Login to SmartCampus
				                </a>
				            </div>

				            <p>
				                If you did not create this account, please contact the campus administrator.
				            </p>

				            <p>Best Regards,<br><b>SmartCampus AI Team</b></p>

				        </div>

				        <!-- FOOTER -->
				        <div style="background:#f8f9fa; padding:15px; text-align:center; font-size:12px; color:#777;">
				            © 2026 SmartCampus AI · All Rights Reserved
				        </div>

				    </div>
				"""
				.formatted(name, username, password);
	}

	public void sendFacultyCredentials(String toEmail, String facultyName, String username, String password) {

		try {

			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

			helper.setTo(toEmail);
			helper.setSubject("SmartCampus AI | Your Faculty Portal Login Credentials");

			helper.setText(buildFacultyEmailHtml(facultyName, username, password), true);

			mailSender.send(message);

			System.out.println("Faculty credential email sent to: " + toEmail);

		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	private String buildFacultyEmailHtml(String name, String username, String password) {

		return """
				<div style="font-family: 'Segoe UI', Arial, sans-serif; max-width:600px; margin:auto; border-radius:10px; overflow:hidden; border:1px solid #e0e0e0">

				    <!-- HEADER -->
				    <div style="background: linear-gradient(135deg,#6a11cb,#2575fc); padding:30px; text-align:center;">
				        <h1 style="color:white; margin:0;">SmartCampus AI</h1>
				        <p style="color:rgba(255,255,255,0.9); margin-top:5px;">
				            Faculty Portal Access
				        </p>
				    </div>

				    <!-- BODY -->
				    <div style="padding:30px; background:#ffffff;">

				        <p>Hello <b>%s</b>,</p>

				        <p>
				            Welcome to <b>SmartCampus AI</b>! 🎓
				            Your faculty account has been successfully created.
				        </p>

				        <p>Please use the credentials below to login to the portal:</p>

				        <!-- LOGIN BOX -->
				        <div style="margin:25px 0; padding:25px; background:#f5f7ff; border-radius:8px; border:1px dashed #2575fc; text-align:center;">

				            <p style="margin:5px 0; font-size:14px; color:#555;">Username</p>
				            <h2 style="margin:5px 0; color:#2575fc;">%s</h2>

				            <p style="margin:15px 0 5px; font-size:14px; color:#555;">Password</p>
				            <h2 style="margin:5px 0; color:#6a11cb;">%s</h2>

				        </div>

				        <p style="color:#e74c3c;">
				            ⚠ For security reasons, please change your password after your first login.
				        </p>

				        <div style="text-align:center; margin:30px 0;">
				            <a href="http://localhost:8080/faculty/login"
				               style="background:#2575fc; color:white; padding:12px 25px;
				                      text-decoration:none; border-radius:6px; font-weight:bold;">
				               Login to Faculty Portal
				            </a>
				        </div>

				        <p>
				            If you did not create this account, please contact the campus administrator.
				        </p>

				        <p>Best Regards,<br><b>SmartCampus AI Team</b></p>

				    </div>

				    <!-- FOOTER -->
				    <div style="background:#f8f9fa; padding:15px; text-align:center; font-size:12px; color:#777;">
				        © 2026 SmartCampus AI · All Rights Reserved
				    </div>

				</div>
				"""
				.formatted(name, username, password);
	}
}