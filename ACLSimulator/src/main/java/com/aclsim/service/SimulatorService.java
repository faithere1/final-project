package com.aclsim.service;

import com.aclsim.model.Packet;
import com.aclsim.model.Rule;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SimulatorService {

    public Rule simulate(Packet packet, List<Rule> rules) {
        // 1. Create a copy and sort by priority (Sequence Number)
        // Smaller numbers are processed first in networking!
        List<Rule> sortedRules = new ArrayList<>(rules);
        sortedRules.sort(Comparator.comparingInt(Rule::getPriority));

        // 2. Iterate through rules in order
        for (Rule rule : sortedRules) {
            if (isMatch(packet, rule)) {
                return rule; // FIRST MATCH FOUND
            }
        }

        // 3. No match found -> Implicit Deny (Standard Networking Behavior)
        return null;
    }

    private boolean isMatch(Packet packet, Rule rule) {
        boolean protocolMatch = NetworkUtils.matchValue(rule.getProtocol(), packet.getProtocol());
        boolean sourceMatch   = NetworkUtils.matchValue(rule.getSourceIp(), packet.getSourceIp());
        boolean destMatch     = NetworkUtils.matchValue(rule.getDestIp(), packet.getDestIp());
        boolean portMatch     = NetworkUtils.matchPort(rule.getDestPort(), packet.getDestPort());

        // All criteria must match (Logical AND)
        return protocolMatch && sourceMatch && destMatch && portMatch;
    }
}
