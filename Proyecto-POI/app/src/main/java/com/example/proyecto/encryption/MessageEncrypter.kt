package com.example.proyecto.encryption

import android.os.Build
import androidx.annotation.RequiresApi
import java.nio.charset.Charset
import java.security.MessageDigest
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class MessageEncrypter {

    private lateinit var password : String;

    constructor(password : String) {
        this.password = password;
    }

    private fun generateKey() : SecretKeySpec{
        var key : ByteArray = this.password.toByteArray(Charset.forName("UTF-8"));
        val md = MessageDigest.getInstance("SHA-256");
        key = md.digest(key);
        val keyspec = SecretKeySpec(key, "AES");
        return keyspec;
    }

    fun decrypt(message: String): String {
        val key = generateKey();
        val cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        val decodedData = Base64.getDecoder().decode(message);
        val decryptedData = cipher.doFinal(decodedData);
        return String(decryptedData);
    }

    fun encrypt(message : ByteArray): String {
        val key = generateKey();
        val cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        val encryptedData = cipher.doFinal(message);
        val encryptedMessage = Base64.getEncoder().encodeToString(encryptedData);
        return encryptedMessage;
    }
}