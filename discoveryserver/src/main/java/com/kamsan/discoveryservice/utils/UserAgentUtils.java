package com.kamsan.discoveryservice.utils;

import com.kamsan.discoveryservice.infrastructure.useragent.UserAgentAnalyzer;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import nl.basjes.parse.useragent.UserAgent;

@Slf4j
public class UserAgentUtils {
    private static final String USER_AGENT_HEADER = "user-agent";
    private static final String X_FORWARDED_FOR_HEADER = "X-FORWARDED-FOR";

    private UserAgentUtils() {
    }

    public static String getIpAddress(HttpServletRequest request) {
        String ipAddress = "Unknown IP";
        if (request != null) {
            ipAddress = request.getHeader(X_FORWARDED_FOR_HEADER);
            if (ipAddress == null || ipAddress.isBlank()) {
                ipAddress = request.getRemoteAddr();
            }
        }
        return ipAddress;
    }

    public static String getClient(HttpServletRequest request) {
        var userAgentAnalyzer = UserAgentAnalyzer.getInstance();
        var agent = userAgentAnalyzer.parse(request.getHeader(USER_AGENT_HEADER));
        return agent.getValue(UserAgent.AGENT_NAME);
    }

    public static String getDevice(HttpServletRequest request) {
        var userAgentAnalyzer = UserAgentAnalyzer.getInstance();
        var agent = userAgentAnalyzer.parse(request.getHeader(USER_AGENT_HEADER));
        log.info("Agent data : {}", agent);
        return agent.getValue(UserAgent.DEVICE_NAME);
    }
}
