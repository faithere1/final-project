package com.aclsim.service;

import com.aclsim.model.LabScenario;
import com.aclsim.model.Packet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LabDatabase {
    private static final List<LabScenario> scenarios = new ArrayList<>();

    static {
        // LEVEL 1: GROUND ZERO (Standard Host Block)
        scenarios.add(new LabScenario(
            1,
            "Level 1: Ground Zero",
            "ALERT: An attacker at IP '1.1.1.1' is brute-forcing our database.\n\n" +
            "MISSION: Create an ACL rule to block (DENY) all traffic originating from '1.1.1.1'.\n" +
            "Make sure traffic from safe users (like '192.168.1.100') is permitted!",
            Arrays.asList(
                new Packet("1.1.1.1", "10.0.0.5", "TCP", 80, -1),      // Malicious Packet
                new Packet("192.168.1.100", "10.0.0.5", "TCP", 80, -1)  // Safe Packet
            ),
            Arrays.asList("DENY", "PERMIT")
        ));

        // LEVEL 2: STEALTH MODE (Protocol Aware ICMP Block)
        scenarios.add(new LabScenario(
            2,
            "Level 2: Stealth Mode",
            "ALERT: Network scanners are pinging our subnet to discover active hosts.\n\n" +
            "MISSION: Create rules to block (DENY) ICMP Ping requests (ICMP Type 8) from reaching your network.\n" +
            "However, you must ensure that normal web traffic (TCP Port 80) is permitted from ANY source IP.",
            Arrays.asList(
                new Packet("172.16.5.5", "192.168.1.10", "ICMP", -1, 8), // Ping Scan Packet
                new Packet("172.16.5.5", "192.168.1.10", "TCP", 80, -1)   // Normal Web Packet
            ),
            Arrays.asList("DENY", "PERMIT")
        ));

        // LEVEL 3: QUARANTINE (CIDR Subnet Block)
        scenarios.add(new LabScenario(
            3,
            "Level 3: Quarantine",
            "ALERT: An infection has broken out in the guest department subnet '192.168.50.0/24'.\n\n" +
            "MISSION: Prevent (DENY) any outbound traffic coming from the '192.168.50.0/24' subnet from reaching your server '10.0.0.5'.\n" +
            "Ensure that corporate subnet workers at '192.168.10.50' can still reach the server.",
            Arrays.asList(
                new Packet("192.168.50.100", "10.0.0.5", "TCP", 443, -1), // Guest quarantine packet
                new Packet("192.168.10.50", "10.0.0.5", "TCP", 443, -1)   // Corp employee packet
            ),
            Arrays.asList("DENY", "PERMIT")
        ));
    }

    public static List<LabScenario> getAllScenarios() {
        return scenarios;
    }

    public static LabScenario getScenarioById(int id) {
        return scenarios.stream()
                .filter(s -> s.getLevelId() == id)
                .findFirst()
                .orElse(null);
    }
}