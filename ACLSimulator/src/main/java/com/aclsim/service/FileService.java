package com.aclsim.service;

import com.aclsim.model.Packet;
import com.aclsim.model.Rule;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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
}