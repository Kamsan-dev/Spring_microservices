package com.kamsan.userservice.utils;

import java.util.UUID;
import java.util.function.Supplier;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;

public class UserUtils {

    public static Supplier<UUID> randomUUID = UUID::randomUUID;
    public static Supplier<String> memberId = () -> randomNumeric(4) + "-" + randomNumeric(2) + "-" + randomNumeric(4);
}
