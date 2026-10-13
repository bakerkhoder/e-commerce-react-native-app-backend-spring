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
        // The assistant calls a model, so bound the input length
        if (question.length() > 200) {
            question = question.substring(0, 200);
        }
        return assistantService.ask(question);
    }
}