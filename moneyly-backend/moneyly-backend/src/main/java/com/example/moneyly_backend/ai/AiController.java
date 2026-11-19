package com.example.moneyly_backend.ai;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/ai")
public class AiController
{
    private final ExpenseCategorizationService categorizationService;
    public AiController(
            ExpenseCategorizationService categorizationService
    )
    {
        this.categorizationService = categorizationService;
    }
    public record PredictionRequest(String description) {}
    public record PredictionResponse(String category) {}
    @PostMapping("/predict-category")
    public PredictionResponse predictCategory(
            @RequestBody PredictionRequest request
    )
    {
        if (request == null || request.description() == null
                || request.description().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Description must not be empty"
            );
        }
        return new PredictionResponse(
                categorizationService.predictCategory(
                        request.description()
                )
        );
    }
}