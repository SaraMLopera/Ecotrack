package ecotrack.backend.controllers;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "http://localhost:5432")
public class TestController {
    
    @GetMapping("/")
    public String home() {
        return "Conexión exitosa entre React y Spring Boot ✅";
    }

}
