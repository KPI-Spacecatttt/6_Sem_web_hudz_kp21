package com.example.lab3;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "casdoor")
public class CasdoorProperties {

    private String connectEndpoint;
    private String connectClientId;
    private String connectClientSecret;
    private String loginEndpoint;
    private String tokenEndpoint;

    public String getConnectEndpoint() {
        return connectEndpoint;
    }

    public String getConnectClientId() {
        return connectClientId;
    }

    public String getConnectClientSecret() {
        return connectClientSecret;
    }

    public String getLoginEndpoint() {
        return loginEndpoint;
    }

    public String getTokenEndpoint() {
        return tokenEndpoint;
    }

    public void setConnectEndpoint(String connectEndpoint) {
        this.connectEndpoint = connectEndpoint;
    }

    public void setConnectClientId(String connectClientId) {
        this.connectClientId = connectClientId;
    }

    public void setConnectClientSecret(String connectClientSecret) {
        this.connectClientSecret = connectClientSecret;
    }

    public void setLoginEndpoint(String loginEndpoint) {
        this.loginEndpoint = loginEndpoint;
    }

    public void setTokenEndpoint(String tokenEndpoint) {
        this.tokenEndpoint = tokenEndpoint;
    }
}
