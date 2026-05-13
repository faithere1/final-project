package com.aclsim.service;

import com.aclsim.model.Packet;
import com.aclsim.model.Rule;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SimulatorService {

    /**
     * Finds the first rule that matches based on priority.
     */
    public Rule simulate(Packet packet, List<Rule> rules) {
        List<Rule> sortedRules = new ArrayList<>(rules);
        sortedRules.sort(Comparator.comparingInt(Rule::getPriority));

        for (Rule rule : sortedRules) {
            if (isMatch(packet, rule)) {
                return rule; 
            }
        }
        return null;
    }

    /**
     * Updated for 2.1.5: 
     * If the protocol is ICMP, the "Port" field in the rule is interpreted 
     * as the ICMP Type (e.g., Type 8 for Echo Request).
     */
    private boolean isMatch(Packet packet, Rule rule) {
        // 1. IP and Protocol matching (supports CIDR and Wildcards from 2.1.1 & 2.1.4)
        boolean protocolMatch = NetworkUtils.matchValue(rule.getProtocol(), packet.getProtocol());
        boolean sourceMatch   = NetworkUtils.isInRange(rule.getSourceIp(), packet.getSourceIp());
        boolean destMatch     = NetworkUtils.isInRange(rule.getDestIp(), packet.getDestIp());

        // 2. Specialized Logic for 2.1.5 (ICMP Type vs TCP/UDP Port)
        boolean portOrTypeMatch;
        
        if (rule.getProtocol().equalsIgnoreCase("ICMP")) {
            // Check packet's ICMP Type against the rule's port field
            portOrTypeMatch = NetworkUtils.matchPort(rule.getDestPort(), packet.getIcmpType());
        } else {
            // Check packet's Dest Port against the rule's port field (supports ranges from 2.1.2)
            portOrTypeMatch = NetworkUtils.matchPort(rule.getDestPort(), packet.getDestPort());
        }

        // All criteria must match (Logical AND)
        return protocolMatch && sourceMatch && destMatch && portOrTypeMatch;
    }
}