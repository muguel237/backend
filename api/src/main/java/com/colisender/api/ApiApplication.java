package com.colisender.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean; // Ajoutez cet import
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // Ajoutez cet import
import org.springframework.security.crypto.password.PasswordEncoder; // Ajoutez cet import

@SpringBootApplication
public class ApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }
// System.setProperty("java.security.egd", "file:/dev/./urandom");
    // Ajoutez cette méthode ici, à l'intérieur de la classe ApiApplication
    // @Bean
    // public PasswordEncoder passwordEncoder() {
    //     return new BCryptPasswordEncoder();
    // }
}