package org.example;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @GetMapping("/get/{name}")
    public String get(@PathVariable String name) {
        System.out.println("Got request for name" + name);
        return String.format("Hello, %s", name);
    }
}