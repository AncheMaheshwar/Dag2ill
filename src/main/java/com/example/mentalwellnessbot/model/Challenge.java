package com.example.mentalwellnessbot.model;

public enum Challenge {
    OVERTHINKING,
    ANXIETY,
    LOW_SELF_ESTEEM,
    DEPRESSION,
    SOCIAL_COMPARISON,
    LONELINESS,
    BURNOUT,
    STRESS;

    public String pretty() {
        return switch (this) {
            case OVERTHINKING -> "Overthinking";
            case ANXIETY -> "Anxiety";
            case LOW_SELF_ESTEEM -> "Low Self-Esteem / Negative Thinking";
            case DEPRESSION -> "Depression";
            case SOCIAL_COMPARISON -> "Social Comparison";
            case LONELINESS -> "Loneliness";
            case BURNOUT -> "Burnout";
            case STRESS -> "Stress";
        };
    }
}
