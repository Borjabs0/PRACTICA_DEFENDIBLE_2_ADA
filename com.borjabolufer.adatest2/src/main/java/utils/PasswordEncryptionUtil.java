package utils;

import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PasswordEncryptionUtil {
    // Logger para registrar eventos y errores
    private static final Logger logger = LoggerFactory.getLogger(PasswordEncryptionUtil.class);
    
    // Clave de encriptación (NOTA: En producción, usar un sistema seguro de gestión de claves)
    private static final String ENCRYPTION_KEY = "MySuperSecretKey";
    
    // Algoritmo de encriptación
    private static final String ALGORITHM = "AES";

    /**
     * Descifra la contraseña proporcionada.
     * 
     * @param password Contraseña a descifrar
     * @return Contraseña descifrada o contraseña original si el descifrado falla
     */
    public String decryptPassword(String password) {
        // Si la contraseña no está codificada en Base64, devolver la contraseña original
        if (!isBase64(password)) {
            return password;
        }

        try {
            // Crear especificación de clave secreta
            SecretKeySpec secretKey = new SecretKeySpec(
                ENCRYPTION_KEY.getBytes(StandardCharsets.UTF_8), 
                ALGORITHM
            );

            // Inicializar cifrado para descifrado
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            // Decodificar y descifrar
            byte[] decodedBytes = Base64.getDecoder().decode(password);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);

            // Convertir bytes descifrados a cadena
            return new String(decryptedBytes, StandardCharsets.UTF_8);

        } catch (GeneralSecurityException e) {
            // Registrar error para depuración
            logger.error("Falló el descifrado de la contraseña", e);
            
            // Devolver contraseña original en caso de cualquier error de descifrado
            return password;
        }
    }

    /**
     * Verifica si la cadena dada es una cadena codificada en Base64 válida.
     * 
     * @param str Cadena a verificar
     * @return true si la cadena está codificada en Base64, false en caso contrario
     */
    private boolean isBase64(String str) {
        // Manejar casos de cadena nula o vacía
        if (str == null || str.isEmpty()) {
            return false;
        }
        
        try {
            Base64.getDecoder().decode(str);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Encripta la contraseña proporcionada.
     * 
     * @param password Contraseña a encriptar
     * @return Contraseña encriptada o contraseña original si la encriptación falla
     */
    public String encryptPassword(String password) {
        try {
            // Crear especificación de clave secreta
            SecretKeySpec secretKey = new SecretKeySpec(
                ENCRYPTION_KEY.getBytes(StandardCharsets.UTF_8), 
                ALGORITHM
            );

            // Inicializar cifrado para encriptación
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            // Encriptar y codificar en Base64
            byte[] encryptedBytes = cipher.doFinal(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);

        } catch (GeneralSecurityException e) {
            // Registrar error para depuración
            logger.error("Falló la encriptación de la contraseña", e);
            return password;
        }
    }
}