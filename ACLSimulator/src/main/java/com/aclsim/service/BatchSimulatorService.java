package com.aclsim.service;

import com.aclsim.model.Packet;
import com.aclsim.model.Rule;
import java.util.ArrayList;
import java.util.List;

public class BatchSimulatorService {

    public String runBatch(List<Packet> packets, List<Rule> rules, SimulatorService engine) {
        int permitCount = 0;
        int denyCount = 0;
        StringBuilder report = new StringBuilder();

        report.append("=== BATCH SIMULATION REPORT ===\n");
        report.append(String.format("%-15s | %-15s | %-10s | %-20s\n", "Source IP", "Dest IP", "Port", "Result"));
        report.append("-".repeat(70)).append("\n");

        for (Packet p : packets) {
            Rule match = engine.simulate(p, rules);
            String result = (match != null) ? match.getAction() : "DENY (Implicit)";
            
            if (result.contains("PERMIT")) permitCount++;
            else denyCount++;

            report.append(String.format("%-15s | %-15s | %-10s | %-20s\n", 
                p.getSourceIp(), p.getDestIp(), p.getDestPort(), result));
        }

        report.append("-".repeat(70)).append("\n");
        report.append("TOTAL PACKETS: ").append(packets.size()).append("\n");
        report.append("PERMITTED:     ").append(permitCount).append("\n");
        report.append("DENIED:        ").append(denyCount).append("\n");

        return report.toString();
    }
}