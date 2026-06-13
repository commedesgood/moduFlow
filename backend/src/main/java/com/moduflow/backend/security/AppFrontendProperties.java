package com.moduflow.backend.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.frontend")
public class AppFrontendProperties {
    private String baseUrl = "http://localhost:5173";
    private String oauthSuccessPath = "/oauth/callback";
    private String oauthFailurePath = "/oauth/callback";

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getOauthSuccessPath() {
        return oauthSuccessPath;
    }

    public void setOauthSuccessPath(String oauthSuccessPath) {
        this.oauthSuccessPath = oauthSuccessPath;
    }

    public String getOauthFailurePath() {
        return oauthFailurePath;
    }

    public void setOauthFailurePath(String oauthFailurePath) {
        this.oauthFailurePath = oauthFailurePath;
    }
}
