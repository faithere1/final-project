package com.aclsim.service;

import com.aclsim.model.Packet;
import com.aclsim.model.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SimulatorServiceTest {

    private SimulatorService simulator;
    private List<Rule> rules;

    @BeforeEach
    public void setUp() {
        simulator = new SimulatorService();
        rules = new ArrayList<>();

        // Set up standard rules (Unsorted to test priority sorting)
        rules.add(new Rule(2, 20, "PERMIT", "TCP", "192.168.1.0/24", "ANY", "80"));
        rules.add(new Rule(1, 10, "DENY", "TCP", "192.168.1.50", "ANY", "80"));
        rules.add(new Rule(3, 30, "DENY", "ICMP", "ANY", "ANY", "8")); // Block Pings (2.1.5)
    }

    @Test
    public void testFirstMatchSequencing() {
        // Packet from 192.168.1.50 matching both Rule #10 (DENY) and Rule #20 (PERMIT)
        Packet packet = new Packet("192.168.1.50", "8.8.8.8", "TCP", 80, -1);
        
        Rule match = simulator.simulate(packet, rules);
        
        assertNotNull(match);
        assertEquals(10, match.getPriority()); // Rule #10 should win because of lower priority number
        assertEquals("DENY", match.getAction());
    }

    @Test
    public void testPermitFallThrough() {
        // Packet from 192.168.1.99 (fails Rule #10, matches Rule #20)
        Packet packet = new Packet("192.168.1.99", "8.8.8.8", "TCP", 80, -1);
        
        Rule match = simulator.simulate(packet, rules);
        
        assertNotNull(match);
        assertEquals(20, match.getPriority());
        assertEquals("PERMIT", match.getAction());
    }

    @Test
    public void testImplicitDeny() {
        // Packet that matches absolutely no rules in the list
        Packet packet = new Packet("172.16.0.1", "8.8.8.8", "TCP", 22, -1);
        
        Rule match = simulator.simulate(packet, rules);
        
        assertNull(match); // Returning null represents an Implicit Deny
    }

    @Test
    public void testIcmpTypeMatching() {
        // Packet with ICMP Type 8 (Should hit Rule #30 DENY)
        Packet pingPacket = new Packet("1.1.1.1", "2.2.2.2", "ICMP", -1, 8);
        Rule match1 = simulator.simulate(pingPacket, rules);
        assertNotNull(match1);
        assertEquals(30, match1.getPriority());

        // Packet with ICMP Type 3 (Unreachable - should ignore Rule #30 and hit Implicit Deny)
        Packet unreachablePacket = new Packet("1.1.1.1", "2.2.2.2", "ICMP", -1, 3);
        Rule match2 = simulator.simulate(unreachablePacket, rules);
        assertNull(match2); // Hits Implicit Deny
    }
}