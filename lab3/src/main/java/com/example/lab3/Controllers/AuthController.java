package com.example.lab3.Controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.example.lab3.CasdoorProperties;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final CasdoorProperties casdoorProperties;
    private final String redirectUri = "https://localhost:8001/auth/callback";

    public AuthController(CasdoorProperties casdoorProperties) {
        this.casdoorProperties = casdoorProperties;
    }

    @GetMapping("/login")
    public void login(HttpServletResponse response) throws IOException {
        System.out.println("Login requested");

        String authorizeUrl = casdoorProperties.getConnectEndpoint() + casdoorProperties.getLoginEndpoint() +
                "?client_id=" + casdoorProperties.getConnectClientId() +
                "&response_type=code" +
                "&redirect_uri=" + redirectUri +
                "&scope=openid profile email";

        System.out.println("Authorize URL: " + authorizeUrl);

        response.sendRedirect(authorizeUrl);
    }

    @GetMapping("/callback")
    public void callback(@RequestParam String code, HttpServletResponse response) throws IOException {
        System.out.println("Got code: " + code);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("code", code);
        params.add("redirect_uri", redirectUri);
        params.add("client_id", casdoorProperties.getConnectClientId());
        params.add("client_secret", casdoorProperties.getConnectClientSecret());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        System.out.println("Request to get token: " + request);

        ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(
                casdoorProperties.getConnectEndpoint() + casdoorProperties.getTokenEndpoint(),
                request,
                Map.class);

        // Handle the token response
        if (tokenResponse.getStatusCode().is2xxSuccessful()) {

            String accessToken = (String) tokenResponse.getBody().get("access_token");

            if (accessToken != null) {
                Cookie accessTokenCookie = new Cookie("access_token", accessToken);
                accessTokenCookie.setPath("/");
                // Add that for securety, but now we cannot access the cookie in the browser
                accessTokenCookie.setHttpOnly(true);
                response.addCookie(accessTokenCookie);
            } else {
                System.err.println("Failed to get access token from response.");
            }
        } else {
            System.err.println("Failed to get token: " + tokenResponse.getStatusCode());
        }

        response.sendRedirect("/");
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getUserStatus(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Cookie[] cookies = request.getCookies();
        boolean loggedIn = false;

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("access_token".equals(cookie.getName())) {
                    loggedIn = true;
                    break;
                }
            }
        }

        response.put("loggedIn", loggedIn);
        return ResponseEntity.ok(response);
    }

}
