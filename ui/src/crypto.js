/**
 * Decrypts data using AES-GCM with PBKDF2 derived key.
 * Matches Java's AesEncryptionUtil implementation.
 */
export async function decrypt(encryptedData, password) {
    try {
        const parts = encryptedData.split('|');
        if (parts.length !== 4 || parts[0] !== 'v1') {
            throw new Error('Invalid encrypted format');
        }

        const salt = Uint8Array.from(atob(parts[1]), c => c.charCodeAt(0));
        const iv = Uint8Array.from(atob(parts[2]), c => c.charCodeAt(0));
        const ciphertext = Uint8Array.from(atob(parts[3]), c => c.charCodeAt(0));

        // 1. Import Password
        const passwordKey = await window.crypto.subtle.importKey(
            "raw",
            new TextEncoder().encode(password),
            "PBKDF2",
            false,
            ["deriveKey"]
        );

        // 2. Derive Key
        const key = await window.crypto.subtle.deriveKey(
            {
                name: "PBKDF2",
                salt: salt,
                iterations: 65536,
                hash: "SHA-256"
            },
            passwordKey,
            { name: "AES-GCM", length: 256 },
            false,
            ["decrypt"]
        );

        // 3. Decrypt
        const decryptedBuffer = await window.crypto.subtle.decrypt(
            {
                name: "AES-GCM",
                iv: iv
            },
            key,
            ciphertext
        );

        const decryptedText = new TextDecoder().decode(decryptedBuffer);
        return JSON.parse(decryptedText);

    } catch (error) {
        console.error("Decryption error:", error);
        throw new Error('Incorrect password or data corruption');
    }
}
