package com.smart.config;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Random;

import org.springframework.stereotype.Service;

@Service
public class CaptchaService {

    public String generateCaptchaText() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        while (sb.length() < 6) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public BufferedImage generateCaptchaImage(String text) {
        int width = 160;
        int height = 50;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // Background
        g.setColor(new Color(20, 25, 30)); // Match dark theme
        g.fillRect(0, 0, width, height);

        // Noise
        Random random = new Random();
        g.setColor(new Color(200, 241, 53, 50)); // Lime noise
        for (int i = 0; i < 50; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            g.drawOval(x, y, 2, 2);
        }

        // Text
        g.setFont(new Font("SansSerif", Font.BOLD, 32));
        g.setColor(new Color(200, 241, 53)); // High-contrast Lime text
        g.drawString(text, 20, 35);

        g.dispose();
        return image;
    }
}
