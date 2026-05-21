package com.aclsim.model;

public class GradingResult {
    private boolean passed;
    private String feedbackDetails;
    private String hint;

    public GradingResult(boolean passed, String feedbackDetails, String hint) {
        this.passed = passed;
        this.feedbackDetails = feedbackDetails;
        this.hint = hint;
    }

    public boolean isPassed() { return passed; }
    public String getFeedbackDetails() { return feedbackDetails; }
    public String getHint() { return hint; }
}