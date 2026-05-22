package com.aclsim.service;

import com.aclsim.model.GradingResult;
import com.aclsim.model.LabScenario;
import com.aclsim.model.Packet;
import com.aclsim.model.Rule;
import java.util.List;

public class GradingService {
    private final SimulatorService simulator = new SimulatorService();

    /**
     * Grades the user's ACL ruleset against a specific lab scenario
     * with intelligent, contextual hints.
     */
    public GradingResult gradeLab(LabScenario scenario, List<Rule> userRules) {
        List<Packet> testPackets = scenario.getTestPackets();
        List<String> expectedActions = scenario.getExpectedActions();
        StringBuilder feedback = new StringBuilder();
        
        boolean allPassed = true;
        String hint = "";

        feedback.append("=== ACADEMY GRADER REPORT ===\n");
        feedback.append("Scenario: ").append(scenario.getTitle()).append("\n");
        feedback.append("=".repeat(50)).append("\n\n");

        for (int i = 0; i < testPackets.size(); i++) {
            Packet packet = testPackets.get(i);
            String expected = expectedActions.get(i);

            // Run the simulation against the student's current rules
            Rule match = simulator.simulate(packet, userRules);
            String actualAction = (match != null) ? match.getAction() : "DENY"; // Default behavior is Implicit Deny

            feedback.append(String.format("Test #%d: [%s] Packet from %s to %s on Port %s%n", 
                (i + 1), packet.getProtocol(), packet.getSourceIp(), packet.getDestIp(), 
                packet.getProtocol().equalsIgnoreCase("ICMP") ? packet.getIcmpType() : packet.getDestPort()));
            feedback.append("  Expected: ").append(expected).append("\n");
            feedback.append("  Your ACL: ").append(actualAction).append("\n");

            if (actualAction.equalsIgnoreCase(expected)) {
                feedback.append("  STATUS:  [PASSED] ✅\n\n");
            } else {
                feedback.append("  STATUS:  [FAILED] ❌\n\n");
                allPassed = false;
                
                // 3.4.1: Generate a highly specific hint for the failed test case
                if (hint.isEmpty()) {
                    hint = generateIntelligentHint(scenario.getLevelId(), packet, expected);
                }
            }
        }

        if (allPassed) {
            feedback.append("==================================================\n");
            feedback.append("RESULT: SUCCESS! Network secured. Level unlocked! 🎉\n");
            feedback.append("==================================================\n");
            hint = "No hints needed—perfect execution!";
        } else {
            feedback.append("==================================================\n");
            feedback.append("RESULT: FAILED. Please review your ruleset.\n");
            feedback.append("==================================================\n");
        }

        return new GradingResult(allPassed, feedback.toString(), hint);
    }

    /**
     * 3.4.1 Logic: Generates a custom hint depending on which level 
     * and which test packet failed.
     */
    private String generateIntelligentHint(int levelId, Packet failedPacket, String expected) {
        StringBuilder sb = new StringBuilder("💡 MENTOR HINT:\n");

        switch (levelId) {
            case 1: // Ground Zero
                if (expected.equals("DENY")) {
                    sb.append("The attacker at '1.1.1.1' successfully penetrated the network.\n")
                      .append("Add a rule: Action='DENY', Source='1.1.1.1'.");
                } else {
                    sb.append("Legitimate users are being blocked.\n")
                      .append("Make sure you didn't block 'ANY' source IP instead of just '1.1.1.1'.");
                }
                break;

            case 2: // Stealth Mode
                if (failedPacket.getProtocol().equalsIgnoreCase("ICMP")) {
                    sb.append("Ping request got through.\n")
                      .append("Create a rule: Protocol='ICMP', Port/Type='8', Action='DENY'.");
                } else {
                    sb.append("Web traffic is failing. Ensure you permit TCP on Port 80 from ANY source.");
                }
                break;

            case 3: // Quarantine
                if (failedPacket.getSourceIp().startsWith("192.168.50")) {
                    sb.append("A device from the infected guest subnet accessed the server.\n")
                      .append("Use CIDR notation '192.168.50.0/24' in your Source IP field with Action='DENY'.");
                } else {
                    sb.append("Corporate network users are blocked. Ensure they bypass the quarantine.");
                }
                break;

            case 4: // VIP Server
                if (failedPacket.getSourceIp().equals("10.0.0.50")) {
                    sb.append("The Administrator is unable to access the database.\n")
                      .append("Ensure your permit rule for '10.0.0.50' is HIGHER (lower priority number) than your general deny rule!");
                } else {
                    sb.append("Unapproved users can still access database port 3306.\n")
                      .append("Add a rule blocking all port 3306 traffic, placed below the admin's permit rule.");
                }
                break;

            case 5: // Web Farm
                if (failedPacket.getDestPort() == 22) {
                    sb.append("SSH traffic (Port 22) reached the web farm.\n")
                      .append("Try denying port range '1024-65535' or explicitly block ports outside the 80-443 range.");
                } else {
                    sb.append("Web traffic (Port 80/443) is being blocked. Ensure your permit range is inclusive.");
                }
                break;

            case 6: // Rogue ISP
                sb.append("The attacker range was not correctly neutralized.\n")
                  .append("Double check your wildcard mask spacing. It must be written exactly as: '203.0.113.0 0.0.0.255'.");
                break;

            case 7: // DMZ Isolation
                sb.append("UDP or ICMP packets reached the DMZ Web Server.\n")
                  .append("Add rules explicitly denying UDP and ICMP to destination '192.168.100.10'.");
                break;

            case 8: // The Gatekeeper
                sb.append("The sequential flow of your rules is incorrect.\n")
                  .append("First-Match rule ordering is active. Drag your specific 'PERMIT' rule for the Admin host to the very top, above the broad 'DENY' subnet rule.");
                break;

            default:
                sb.append("Analyze your rules. Check for subnet overlaps or incorrect port assignments.");
                break;
        }

        return sb.toString();
    }
}