package com.moduflow.backend.service;

public final class ExerciseIdNormalizer {

    private ExerciseIdNormalizer() {
    }

    public static String normalize(String exerciseId) {
        if (exerciseId == null) {
            return null;
        }

        String trimmed = exerciseId.trim();
        if (trimmed.isBlank()) {
            return null;
        }

        return trimmed.replace("-", "");
    }
}
