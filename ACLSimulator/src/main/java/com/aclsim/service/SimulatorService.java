package com.aclsim.service;

import com.aclsim.model.Packet;
import com.aclsim.model.Rule;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SimulatorService {

    /**
     * The core ACL logic: Find the FIRST rule that matches based on priority.
     */
    public Rule simulate(Packet packet, List<Rule> rules) {
        // 1. Create a copy and sort by priority (Sequence Number)
        // In networking, rules with lower sequence numbers are evaluated first.
        List<Rule> sortedRules = new ArrayList<>(rules);
        sortedRules.sort(Comparator.comparingInt(Rule::getPriority));

        // 2. Iterate through rules in order
        for (Rule rule : sortedRules) {
            if (isMatch(packet, rule)) {
                return rule; // FIRST MATCH FOUND (Algorithm stops here)
            }
        }

        // 3. No match found -> Implicit Deny
        return null;
    }

    /**
     * Checks if a packet meets all the criteria of a specific rule.
     */
    private boolean isMatch(Packet packet, Rule rule) {
        // Protocol and Port matching remain the same
        boolean protocolMatch = NetworkUtils.matchValue(rule.getProtocol(), packet.getProtocol());
        boolean portMatch     = NetworkUtils.matchPort(rule.getDestPort(), packet.getDestPort());
        
        // NEW FOR 2.1.1: Use bitwise range matching for IPs
        // This handles both exact matches (1.1.1.1) and subnets (192.168.1.0/24)
        boolean sourceMatch   = NetworkUtils.isInRange(rule.getSourceIp(), packet.getSourceIp());
        boolean destMatch     = NetworkUtils.isInRange(rule.getDestIp(), packet.getDestIp());

        // All criteria must match (Logical AND)
        return protocolMatch && sourceMatch && destMatch && portMatch;
    }
}