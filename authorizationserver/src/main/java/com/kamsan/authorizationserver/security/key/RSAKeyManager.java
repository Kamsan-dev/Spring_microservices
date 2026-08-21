package com.kamsan.authorizationserver.security.key;

import com.kamsan.authorizationserver.sharedkernel.exception.ApiException;
import com.nimbusds.jose.jwk.RSAKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.UUID;

@Slf4j
@Component
public class RSAKeyManager {
    private static final String RSA = "RSA";
    @Value("${spring.profiles.active}")
    private String activeProfile;
    @Value("${keys.private}")
    private String privateKeyFileName;
    @Value("${keys.public}")
    private String publicKeyFileName;

    public RSAKey getRSAKeyPair() {
        return generateRSAKeyPair(privateKeyFileName, publicKeyFileName);
    }

    private RSAKey generateRSAKeyPair(String privateKeyFileName, String publicKeyFileName) {
        var keysDirectory = Paths.get("src", "main", "resources", "keys");
        verifyKeysDirectory(keysDirectory);
        // case : Dev environement && les fichiers clés public & privé existent déjà.
        if (Files.exists(keysDirectory.resolve(publicKeyFileName)) && Files.exists(keysDirectory.resolve(privateKeyFileName))) {
            log.info("RSA keys already exist. Loading keys from file paths: {}, {}", publicKeyFileName, privateKeyFileName);
            try {
                var privateKeyFile = keysDirectory.resolve(privateKeyFileName).toFile();
                var publicKeyFile = keysDirectory.resolve(publicKeyFileName).toFile();
                // Factory capable de manipuler des clés RSA.
                var keyFactory = KeyFactory.getInstance(RSA);

                byte[] publicKeyBytes = Files.readAllBytes(publicKeyFile.toPath());
                X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
                RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);

                byte[] privateKeyBytes = Files.readAllBytes(privateKeyFile.toPath());
                PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
                RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(privateKeySpec);
                var keyId = UUID.randomUUID().toString();
                log.info("Key id : {}", keyId);
                return new RSAKey.Builder(publicKey).privateKey(privateKey).keyID(keyId).build();
            } catch (Exception ex) {
                log.error(ex.getMessage());
                throw new ApiException(ex.getMessage());
            }
        }
        // case : Production environement
        else {
            if (activeProfile.equalsIgnoreCase("prod")) {
                throw new ApiException("Public and private keys don't exist in prod environment");
            }
        }

        log.info("Generating new public and private keys : {}, {}", publicKeyFileName, privateKeyFileName);
        try {
            var keyPairGenerator = KeyPairGenerator.getInstance(RSA);
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

            // Ecriture de la pair publique et privé dans leur fichier respectif.
            try {
                try (var fos = new FileOutputStream(keysDirectory.resolve(publicKeyFileName).toFile())) {
                    X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKey.getEncoded());
                    fos.write(publicKeySpec.getEncoded());
                }
                try (var fos = new FileOutputStream(keysDirectory.resolve(privateKeyFileName).toFile())) {
                    PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKey.getEncoded());
                    fos.write(privateKeySpec.getEncoded());
                }
            } catch (Exception ex) {
                log.error(ex.getMessage());
                throw new ApiException(ex.getMessage());
            }

            return new RSAKey.Builder(publicKey).privateKey(privateKey).keyID(UUID.randomUUID().toString()).build();
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new ApiException(ex.getMessage());
        }
    }

    private void verifyKeysDirectory(Path keysDirectory) {
        if (!Files.exists(keysDirectory)) {
            try {
                Files.createDirectories(keysDirectory);
            } catch (Exception ex) {
                log.error(ex.getMessage());
                throw new ApiException(ex.getMessage());
            }
            log.info("Created keys directory : {}", keysDirectory);
        }
    }
}
