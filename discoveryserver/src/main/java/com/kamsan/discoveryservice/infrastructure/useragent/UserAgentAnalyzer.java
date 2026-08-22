package com.kamsan.discoveryservice.infrastructure.useragent;

public class UserAgentAnalyzer {
    private static nl.basjes.parse.useragent.UserAgentAnalyzer INSTANCE;

    /**
     * SINGLETON instance
     *
     * @return
     */
    public static nl.basjes.parse.useragent.UserAgentAnalyzer getInstance() {
        if (INSTANCE == null) {
            INSTANCE = nl.basjes.parse.useragent.UserAgentAnalyzer
                    .newBuilder()
                    .hideMatcherLoadStats()
                    .withCache(10000)
                    .build();
        }
        return INSTANCE;
    }
}
