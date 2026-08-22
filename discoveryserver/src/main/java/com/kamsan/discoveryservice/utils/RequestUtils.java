package com.kamsan.discoveryservice.utils;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.http.HttpStatus;

public class RequestUtils {
    public static String getMessage(HttpServletRequest request) {
        var status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (null != status) {
            int statusCode = Integer.parseInt(status.toString());
            if (statusCode == HttpStatus.SC_NOT_FOUND) {
                return String.format("%s - Not Found error", statusCode);
            }
            if (statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                return String.format("%s - Internal server error", statusCode);
            }

            if (statusCode == HttpStatus.SC_FORBIDDEN) {
                return String.format("%s - Forbidden error", statusCode);
            }
        }

        return "An error occurred";
    }
}
