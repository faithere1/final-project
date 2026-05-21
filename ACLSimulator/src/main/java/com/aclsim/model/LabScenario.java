package com.aclsim.model;

import java.util.List;

public class LabScenario {
    private int levelId;
    private String title;
    private String description;
    private List<Packet> testPackets;
    private List<String> expectedActions; // "PERMIT" or "DENY"

    public LabScenario(int levelId, String title, String description, List<Packet> testPackets, List<String> expectedActions) {
        this.levelId = levelId;
        this.title = title;
        this.description = description;
        this.testPackets = testPackets;
        this.expectedActions = expectedActions;
    }

    public int getLevelId() { return levelId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public List<Packet> getTestPackets() { return testPackets; }
    public List<String> getExpectedActions() { return expectedActions; }
}
