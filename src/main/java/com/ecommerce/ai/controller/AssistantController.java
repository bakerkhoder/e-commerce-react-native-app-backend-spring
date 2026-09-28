package com.ecommerce.ai.controller;

import com.ecommerce.ai.service.ShoppingAssistantService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assistant")
public class AssistantController {

    private final ShoppingAssistantService assistantService;

    public AssistantController(ShoppingAssistantService assistantService) {
        this.assistantService = assistantService;
    }

    @GetMapping("/ask")
    public String ask(@RequestParam String question) {
        return assistantService.ask(question);
    }
}