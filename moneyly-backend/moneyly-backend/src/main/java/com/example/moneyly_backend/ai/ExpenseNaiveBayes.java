package com.example.moneyly_backend.ai;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class ExpenseNaiveBayes {
    private final Map<String, Integer> categoryCounts = new HashMap<>();
    private final Map<String, Map<String, Integer>> wordCounts = new HashMap<>();
    private final Map<String, Integer> totalWordsByCategory = new HashMap<>();
    private final Map<String, Integer> vocabulary = new HashMap<>();
    public void train(List<ExpenseTrainingData.Example> examples) {
        for (var example : examples) {
            String category = example.category();
            categoryCounts.merge(category, 1, Integer::sum);

            wordCounts.putIfAbsent(category, new HashMap<>());
            totalWordsByCategory.putIfAbsent(category, 0);

            for (String word : tokenize(example.description())) {
                wordCounts.get(category).merge(word, 1, Integer::sum);
                totalWordsByCategory.merge(category, 1, Integer::sum);
                vocabulary.put(word, 1);
            }
        }
    }

    public String predict(String description)
    {
        String bestCategory = "Other";
        double bestScore = Double.NEGATIVE_INFINITY;
        int totalExamples = categoryCounts.values()
                .stream().mapToInt(Integer::intValue).sum();
        int vocabularySize = Math.max(vocabulary.size(), 1);

        for (String category : categoryCounts.keySet()) {
            double score = Math.log(
                    (double) categoryCounts.get(category) / totalExamples
            );
            for (String word : tokenize(description)) {
                int count = wordCounts.get(category)
                        .getOrDefault(word, 0);
                int totalWords = totalWordsByCategory.get(category);

                double probability = (count + 1.0)
                        / (totalWords + vocabularySize);

                score += Math.log(probability);
            }

            if (score > bestScore) {
                bestScore = score;
                bestCategory = category;
            }
        }

        return bestCategory;
    }

    private List<String> tokenize(String text)
    {
        return List.of(text.toLowerCase()
                .split("\\s+"));
    }
}