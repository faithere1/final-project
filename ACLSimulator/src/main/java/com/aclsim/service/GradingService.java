package com.aclsim.service;

import com.aclsim.model.GradingResult;
import com.aclsim.model.LabScenario;
import com.aclsim.model.Packet;
import com.aclsim.model.Rule;
import java.util.List;

public class GradingService {
    private final SimulatorService simulator = new SimulatorService();

    /**
     * Grades the user's ACL ruleset against a specific lab scenario.
     */
    public GradingResult gradeLab(LabScenario scenario, List<Rule> userRules) {
        List<Packet> testPackets = scenario.getTestPackets();
        List<String> expectedActions = scenario.getExpectedActions();
        StringBuilder feedback = new StringBuilder();
        
        boolean allPassed = true;
        String hint = "";

        feedback.append("Grading Level: ").append(scenario.getTitle()).append("\n");
        feedback.append("Evaluating security policy...\n\n");

        for (int i = 0; i < testPackets.size(); i++) {
            Packet packet = testPackets.get(i);
            String expected = expectedActions.get(i);

            // Run the simulation against the student's current rules
            Rule match = simulator.simulate(packet, userRules);
            String actualAction = (match != null) ? match.getAction() : "DENY"; // Default behavior

            feedback.append(String.format("Test #%d: Packet from %s to %s (%s)%n", 
                (i + 1), packet.getSourceIp(), packet.getDestIp(), packet.getProtocol()));
            feedback.append("  - Expected: ").append(expected).append("\n");
            feedback.append("  - Your ACL: ").append(actualAction).append("\n");

            if (actualAction.equalsIgnoreCase(expected)) {
                feedback.append("  STATUS: [PASSED] ✅\n\n");
            } else {
                feedback.append("  STATUS: [FAILED] ❌\n\n");
                allPassed = false;
                
                // Formulate an intelligent hint based on what failed (3.4.1)
                if (expected.equals("DENY")) {
                    hint = "HINT: Malicious traffic is getting through. Ensure you have a DENY rule that matches the source/protocol of the attacker.";
                } else {
                    hint = "HINT: Legitimate traffic is being blocked. Check if your DENY rules are too broad (e.g. blocking a whole protocol or using 'ANY' incorrectly).";
                }
            }
        }

        if (allPassed) {
            feedback.append("CONGRATULATIONS! You have successfully secured the network and passed this level! 🎉");
            hint = "No hints needed—you're a hero!";
        } else {
            feedback.append("LAB FAILED. Review your rules, reorder them, and try again.");
        }

        return new GradingResult(allPassed, feedback.toString(), hint);
    }
}