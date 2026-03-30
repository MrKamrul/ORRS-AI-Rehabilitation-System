package com.orrs.utils;

import com.orrs.model.GaitAssessment.GaitRiskLevel;

public class ScoreCalculator {

    public static GaitRiskLevel calculateGaitRisk(double tugTimeSeconds) {
        if (tugTimeSeconds < 10) return GaitRiskLevel.LOW;
        else if (tugTimeSeconds <= 20) return GaitRiskLevel.MODERATE;
        else return GaitRiskLevel.HIGH;
    }

    public static int calculateDexterityScore(int completionTime, int errorCount) {
        int score = 100 - completionTime - (errorCount * 5);
        return Math.max(score, 0); // Ensure score not negative
    }
}
