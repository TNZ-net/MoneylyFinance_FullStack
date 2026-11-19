package com.example.moneyly_backend.ai;

import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class SpendingPredictionController {

    private final SpendingPredictionService service;
    public SpendingPredictionController(
            SpendingPredictionService service) {
        this.service = service;
    }
    @PostMapping("/predict-spending")
    public Map<String, Double> predict(
            @RequestBody Map<String, double[]> request) {

        double[] spending = request.get("monthlySpending");
        return Map.of(
                "predictedSpending",
                service.predict(spending)
        );
    }
}