package com.colisender.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class VerificationService {

    public String extraireNom(MultipartFile file) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        String base64 = Base64.getEncoder().encodeToString(file.getBytes());
        String body = "base64Image=data:image/png;base64," + URLEncoder.encode(base64, StandardCharsets.UTF_8) 
                    + "&apikey=K82161569488957&language=eng";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.ocr.space/parse/image"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
System.out.println("DEBUG API REPONSE : " + response.body());
        // Extraction précise du texte via Jackson
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.body());
        
        JsonNode results = root.path("ParsedResults");
        if (results.isArray() && results.size() > 0) {
            return results.get(0).path("ParsedText").asText();
        }
        return "";
    }
}