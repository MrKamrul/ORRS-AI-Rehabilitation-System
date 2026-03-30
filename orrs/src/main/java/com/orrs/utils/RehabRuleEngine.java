package com.orrs.utils;

import com.orrs.model.RehabPlan;

public class RehabRuleEngine {

    public static RehabPlan generatePlan(int cipnLabel) {
        RehabPlan plan = new RehabPlan();

        if (cipnLabel == 1) {
            plan.setRehabLevel(RehabPlan.RehabLevel.BASIC);
            plan.setExercisePrescription(
                    "Balance exercises, grip strengthening, sensory re-education"
            );
            plan.setSafetyNotes("Avoid uneven surfaces. Monitor pain and numbness.");
        } else {
            plan.setRehabLevel(RehabPlan.RehabLevel.ADVANCED);
            plan.setExercisePrescription("Strength training and coordination drills");
            plan.setSafetyNotes("Standard precautions apply.");
        }
        return plan;
    }
}
