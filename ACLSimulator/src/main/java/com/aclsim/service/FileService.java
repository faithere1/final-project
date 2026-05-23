package com.aclsim.service;

import com.aclsim.model.Packet;
import com.aclsim.model.Rule;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.xml.transform.Source;

public class FileService {

    /**
     * 2.3.2: Exports rules to a CSV file.
     */
    public void exportToCSV(List<Rule> rules, File file) throws Exception {
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println("Priority,Action,Protocol,Source_IP,Dest_IP,Port");
            for (Rule rule : rules) {
                writer.printf("%d,%s,%s,%s,%s,%s%n",
                    rule.getPriority(),
                    rule.getAction(),
                    rule.getProtocol(),
                    rule.getSourceIp(),
                    rule.getDestIp(),
                    rule.getDestPort()
                );
            }
        }
    }

    /**
     * 2.3.2: Reads a CSV file and converts lines into Rule objects.
     */
    public List<Rule> importFromCSV(File file) throws Exception {
        List<Rule> importedRules = new ArrayList<>();
        
        try (Scanner scanner = new Scanner(file)) {
            if (scanner.hasNextLine()) {
                scanner.nextLine(); // Skip header
            }

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.trim().isEmpty()) continue;

                String[] p = line.split(",");
                if (p.length >= 6) {
                    Rule rule = new Rule(
                        0,
                        Integer.parseInt(p[0].trim()),
                        p[1].trim(),
                        p[2].trim(),
                        p[3].trim(),
                        p[4].trim(),
                        p[5].trim()
                    );
                    importedRules.add(rule);
                }
            }
        }
        return importedRules;
    }

    /**
     * NEW FOR 2.3.4: Reads a file of test packets for batch simulation.
     * Expected format: srcIp, destIp, protocol, portOrType
     * Example: 1.1.1.1, 8.8.8.8, TCP, 80
     */
    public List<Packet> importPackets(File file) throws Exception {
        List<Packet> packets = new ArrayList<>();
        
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.trim().isEmpty() || line.startsWith("#")) continue;

                String[] p = line.split(",");
                if (p.length >= 4) {
                    String src = p[0].trim();
                    String dest = p[1].trim();
                    String proto = p[2].trim();
                    int portVal = Integer.parseInt(p[3].trim());

                    // Logic from 2.1.5: Treat portVal as ICMP Type if protocol is ICMP
                    int destPort = proto.equalsIgnoreCase("ICMP") ? -1 : portVal;
                    int icmpType = proto.equalsIgnoreCase("ICMP") ? portVal : -1;

                    packets.add(new Packet(src, dest, proto, destPort, icmpType));
                }
            }
        }
        return packets;
    }

    /**
     * 3.4.4-unplanned: Generates a complete, beautiful academic progress audit report.
     */
    public void generateProgressReport(File file, List<Rule> currentRules, String formattedLogs,
        List<com.aclsim.model.LabScenario> scenarios, com.aclsim.database.ProgressDAO progressDAO) throws Exception {
            try(PrintWriter writer = new PrintWriter(file)){
                writer.println("=================================================================================");
                writer.println("                     ACL SIMULATOR - ACADEMIC AUDIT REPORT                       ");
                writer.println("=================================================================================");
                writer.println("Generated on: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                writer.println("=================================================================================");
                writer.println();

                //1.Progress Overview
                writer.println("---1.ACADEMY PROGRESS SUMMARY---");
                int totalCompleted= progressDAO.getCompletedCount();
                int totalLabs = scenarios.size();
                double completionRate = ((double) totalCompleted / totalLabs) * 100.0;
                writer.printf("Total Labs Completed: %d of %d (%.1f%%)%n%n", totalCompleted, totalLabs, completionRate);

                writer.printf("%-5s | %-25s | %-12s | &-8s&n", "ID", "Scenario Name", "Status", "Attempts");
                writer.println("-".repeat(59));
                for (com.aclsim.model.LabScenario s : scenarios){
                    boolean isDone = progressDAO.isLabCompleted(s.getLevelId());
                    int attempts = progressDAO.getAttempts(s.getLevelId());
                    writer.printf("%-5d | %-25s | %-12s | %-8d%n", s.getLevelId(), s.getTitle(), isDone ? "COMPLETED ✅" : "INCOMPLETE ❌", attempts);
                }
                writer.println();

                //2.Current Rules Configuration
                writer.println("---2.CURRENT RUNNING SECURITY POLICY (ACL)---");
                if (currentRules.isEmpty()){
                    writer.println("[No active rules currently configured on the router.]");
                } else {
                    writer.printf("%-8s | %-8s | %-8s | %-20s | %-20s | %-10s%n", "Priority", "Action", "Protocol", "Source IP", "Dest IP", "Port/Type");
                    writer.println("-".repeat(84));
                    for (Rule r : currentRules){
                    writer.printf("%-8d | %-8s | %-8s | %-20s | %-20s | %-10s%n", r.getPriority(), r.getAction(), r.getProtocol(), r.getSourceIp(), r.getDestIp(), r.getDestPort()
                        );
                    }
                }
                writer.println();

                //3.History Logs
                writer.println("---3.LIVE SIMULATION HISTORY LOGS---");
                writer.println(formattedLogs);
                writer.println();

                writer.println("=================================================================================");
                writer.println("                          END OF ACADEMIC AUDIT REPORT                           ");
                writer.println("=================================================================================");
        }
    }
}