package com.example.lab3.Controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.lab3.CasdoorProperties;
import com.example.lab3.JwtDecoder;

import io.jsonwebtoken.Claims;

@RestController
@RequestMapping("/user")
public class UserController {

    private final JwtDecoder jwtDecoder;

    public UserController(CasdoorProperties casdoorProperties) {
        this.jwtDecoder = new JwtDecoder(casdoorProperties);

    }

    @GetMapping("/info")
    public ResponseEntity<?> getUserInfo(@CookieValue(value = "access_token", required = false) String accessToken) {
        if (accessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Access token not found in cookies");
        }
        try {

            // Check Signature and decode the JWT token
            System.out.println("Try decode Token");
            Claims claims = jwtDecoder.decodeToken(accessToken);

            String id = claims.getSubject();
            String name = claims.get("name", String.class);

            System.out.println("Id (sub): " + id);
            System.out.println("Name: " + name);

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", id);
            userInfo.put("username", name);

            return ResponseEntity.ok(userInfo);

        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Malformed token"+ e.getMessage());
        } catch (org.json.JSONException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token payload"+ e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token" + e.getMessage());
        }
    }
}
