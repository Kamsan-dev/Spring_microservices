package com.kamsan.userservice.utils;

import com.kamsan.userservice.sharedkernel.exception.ApiException;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;

import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.kamsan.userservice.constants.Constants.kamsanIO;
import static dev.samstevens.totp.util.Utils.getDataUriForImage;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;

public class UserUtils {

    private UserUtils() {
    }

    public static Supplier<UUID> randomUUID = UUID::randomUUID;
    public static Supplier<String> memberId = () -> randomNumeric(4) + "-" + randomNumeric(2) + "-" + randomNumeric(4);
    public static Supplier<String> qrCodeSecret = () -> new DefaultSecretGenerator().generate();
    public static Function<String, QrData> qrDataFunction = codeSecret -> new QrData.Builder()
            .issuer(kamsanIO)
            .label(kamsanIO)
            .algorithm(HashingAlgorithm.SHA1)
            .secret(codeSecret)
            .digits(6)
            .period(30)
            .build();

    public static Function<String, String> qrCodeImageUri = qrCodeSecret -> {
        try {
            var data = qrDataFunction.apply(qrCodeSecret);
            var generator = new ZxingPngQrGenerator();
            byte[] imageData = generator.generate(data);
            return getDataUriForImage(imageData, generator.getImageMimeType());
        } catch (QrGenerationException exception) {
            throw new ApiException(exception.getMessage());
        }
    };
}
