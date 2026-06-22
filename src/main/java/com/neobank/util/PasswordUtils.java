package com.neobank.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {

    // Genera el hash con un "Salt" dinámico (Log rounds = 12, estándar bancario seguro)
    public static String encriptarBCrypt(String passwordPlana) {
        return BCrypt.hashpw(passwordPlana, BCrypt.gensalt(12));
    }

    // Verifica si la contraseña escrita en el Login coincide con el hash encriptado de la base de datos
    public static boolean verificarPassword(String passwordPlana, String hashGuardado) {
        try {
            return BCrypt.checkpw(passwordPlana, hashGuardado);
        } catch (IllegalArgumentException e) {
            return false; // Retorna falso si el hash guardado es antiguo o tiene mal formato
        }
    }
}