package com.aclsim.service;

import com.aclsim.model.LabScenario;
import com.aclsim.model.Packet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LabDatabase {
    private static final List<LabScenario> scenarios = new ArrayList<>();

    static {
        // --- LEVEL 1: GROUND ZERO (Standard Host Block) ---
        scenarios.add(new LabScenario(
            1,
            "Level 1: Ground Zero",
            "ALERT: An attacker at IP '1.1.1.1' is brute-forcing our database.\n\n" +
            "MISSION: Create an ACL rule to block (DENY) all traffic originating from '1.1.1.1'.\n" +
            "Ensure traffic from safe workers (like '192.168.1.100') is permitted!",
            Arrays.asList(
                new Packet("1.1.1.1", "10.0.0.5", "TCP", 80, -1),      // Attacker (DENY)
                new Packet("192.168.1.100", "10.0.0.5", "TCP", 80, -1)  // Worker (PERMIT)
            ),
            Arrays.asList("DENY", "PERMIT")
        ));

        // --- LEVEL 2: STEALTH MODE (Protocol Aware ICMP Block) ---
        scenarios.add(new LabScenario(
            2,
            "Level 2: Stealth Mode",
            "ALERT: Reconnaissance scanners are pinging our subnet to discover active hosts.\n\n" +
            "MISSION: Create a rule to block (DENY) ICMP Ping requests (ICMP Type 8) from reaching your network.\n" +
            "However, ensure that normal web traffic (TCP Port 80) is permitted from ANY source IP.",
            Arrays.asList(
                new Packet("172.16.5.5", "192.168.1.10", "ICMP", -1, 8), // Ping Scan (DENY)
                new Packet("172.16.5.5", "192.168.1.10", "TCP", 80, -1)   // Web Traffic (PERMIT)
            ),
            Arrays.asList("DENY", "PERMIT")
        ));

        // --- LEVEL 3: QUARANTINE (CIDR Subnet Block) ---
        scenarios.add(new LabScenario(
            3,
            "Level 3: Quarantine",
            "ALERT: An infection has broken out in the guest department subnet '192.168.50.0/24'.\n\n" +
            "MISSION: Prevent (DENY) any outbound traffic coming from the '192.168.50.0/24' subnet from reaching your server '10.0.0.5'.\n" +
            "Ensure corporate subnet workers at '192.168.10.50' can still reach the server.",
            Arrays.asList(
                new Packet("192.168.50.100", "10.0.0.5", "TCP", 443, -1), // Guest (DENY)
                new Packet("192.168.10.50", "10.0.0.5", "TCP", 443, -1)   // Corporate (PERMIT)
            ),
            Arrays.asList("DENY", "PERMIT")
        ));

        // --- LEVEL 4: THE VIP SERVER (Specific Port Matching) ---
        scenarios.add(new LabScenario(
            4,
            "Level 4: The VIP Server",
            "ALERT: Unregulated access to our primary MySQL Database Server is a massive security risk.\n\n" +
            "MISSION: Allow ONLY the Network Administrator's specific machine at '10.0.0.50' to access the database server '10.0.0.5' on TCP Port 3306.\n" +
            "Wipe database access (Port 3306) for ALL other devices.",
            Arrays.asList(
                new Packet("10.0.0.50", "10.0.0.5", "TCP", 3306, -1), // Admin DB Access (PERMIT)
                new Packet("10.0.0.99", "10.0.0.5", "TCP", 3306, -1), // Normal User DB Access (DENY)
                new Packet("10.0.0.99", "10.0.0.5", "TCP", 80, -1)    // Normal User Web Access (PERMIT)
            ),
            Arrays.asList("PERMIT", "DENY", "PERMIT")
        ));

        // --- LEVEL 5: SECURE PORT WEB FARM (Port Ranges - 2.1.2) ---
        scenarios.add(new LabScenario(
            5,
            "Level 5: Web Shield",
            "ALERT: Port scanners are trying to find open backdoors on our public web servers.\n\n" +
            "MISSION: Our public server at '10.10.10.10' should accept ONLY secure web traffic (TCP Ports 80 to 443).\n" +
            "Block all other ports (like SSH port 22 or database port 3306) from accessing the server.",
            Arrays.asList(
                new Packet("1.1.1.1", "10.10.10.10", "TCP", 80, -1),   // HTTP Web (PERMIT)
                new Packet("1.1.1.1", "10.10.10.10", "TCP", 443, -1),  // HTTPS Web (PERMIT)
                new Packet("1.1.1.1", "10.10.10.10", "TCP", 22, -1)    // SSH Attack (DENY)
            ),
            Arrays.asList("PERMIT", "PERMIT", "DENY")
        ));

        // --- LEVEL 6: ISP LOCKDOWN (Cisco-Style Wildcard Masks - 2.1.4) ---
        scenarios.add(new LabScenario(
            6,
            "Level 6: Rogue ISP Block",
            "ALERT: A suspicious internet service provider range '203.0.113.x' is launching a coordinated DDoS attack on our website.\n\n" +
            "MISSION: Create a rule blocking all traffic originating from the entire Class C network range '203.0.113.0'.\n" +
            "You MUST use Cisco wildcard notation: '203.0.113.0 0.0.0.255'.",
            Arrays.asList(
                new Packet("203.0.113.55", "10.0.0.5", "TCP", 80, -1),  // Attacker 1 (DENY)
                new Packet("203.0.113.250", "10.0.0.5", "TCP", 80, -1), // Attacker 2 (DENY)
                new Packet("198.51.100.5", "10.0.0.5", "TCP", 80, -1)   // Normal visitor (PERMIT)
            ),
            Arrays.asList("DENY", "DENY", "PERMIT")
        ));

        // --- LEVEL 7: DMZ ISOLATION (Multi-Protocol Security) ---
        scenarios.add(new LabScenario(
            7,
            "Level 7: DMZ Isolation",
            "ALERT: An attacker has compromised a device outside the firewall and is attempting UDP flood attacks against our DMZ.\n\n" +
            "MISSION: Allow incoming TCP traffic to reach our public DMZ Web Server at '192.168.100.10'.\n" +
            "Strictly BLOCK any incoming UDP or ICMP traffic from reaching the DMZ server.",
            Arrays.asList(
                new Packet("2.2.2.2", "192.168.100.10", "TCP", 443, -1), // Safe Web request (PERMIT)
                new Packet("2.2.2.2", "192.168.100.10", "UDP", 53, -1),  // UDP Flood Attack (DENY)
                new Packet("2.2.2.2", "192.168.100.10", "ICMP", -1, 8)   // Ping probe (DENY)
            ),
            Arrays.asList("PERMIT", "DENY", "DENY")
        ));

        // --- LEVEL 8: ULTIMATE HARDENING (Strict First-Match Ordering - 2.4.1) ---
        scenarios.add(new LabScenario(
            8,
            "Level 8: The Gatekeeper",
            "ALERT: Rogue administrators are trying to gain unauthorized remote control of our backend servers.\n\n" +
            "MISSION: Ensure that only the Master Administrator machine at '192.168.20.50' is permitted to establish SSH (TCP Port 22) connections to the management core '10.254.0.0/16'.\n" +
            "Wipe SSH (Port 22) access for all other hosts in the subnet.\n\n" +
            "HINT: Order of rules (First-Match) is critical! The specific permit must stand above the broader deny.",
            Arrays.asList(
                new Packet("192.168.20.50", "10.254.55.5", "TCP", 22, -1), // Master Admin SSH (PERMIT)
                new Packet("192.168.20.99", "10.254.55.5", "TCP", 22, -1), // Unauthorized SSH (DENY)
                new Packet("192.168.20.99", "10.254.55.5", "TCP", 80, -1)  // Standard Web traffic (PERMIT)
            ),
            Arrays.asList("PERMIT", "DENY", "PERMIT")
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