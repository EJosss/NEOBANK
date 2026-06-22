package com.neobank.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtils {

    public static String encriptarSHA256(String passwordPlana) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // CRÍTICO: Usar StandardCharsets.UTF_8 garantiza que el hash sea idéntico en cualquier Sistema Operativo
            byte[] hashBytes = digest.digest(passwordPlana.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error crítico en seguridad: Algoritmo SHA-256 no disponible.", e);
        }
    }
}