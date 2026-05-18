package com.aclsim.service;

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
            // Skip the header row
            if (scanner.hasNextLine()) {
                scanner.nextLine();
            }

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.trim().isEmpty()) continue;

                String[] p = line.split(",");
                if (p.length >= 6) {
                    // Create rule (ID is 0 because DB will assign a new one)
                    Rule rule = new Rule(
                        0,
                        Integer.parseInt(p[0].trim()), // Priority
                        p[1].trim(),                   // Action
                        p[2].trim(),                   // Protocol
                        p[3].trim(),                   // Source IP
                        p[4].trim(),                   // Dest IP
                        p[5].trim()                    // Port
                    );
                    importedRules.add(rule);
                }
            }
        }
        return importedRules;
    }
}