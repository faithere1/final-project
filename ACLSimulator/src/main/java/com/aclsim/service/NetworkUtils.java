package com.aclsim.service;

public class NetworkUtils {

    // Regex allows optional CIDR notation (e.g., 192.168.1.0 or 192.168.1.0/24)
    private static final String IP_PATTERN = 
        "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(/([0-9]|[12][0-9]|3[0-2]))?$";

    /**
     * Validates if the input is a proper IPv4 address or CIDR block.
     */
    public static boolean isValidIP(String ip) {
        if (ip == null || ip.equalsIgnoreCase("ANY") || ip.equals("0.0.0.0")) {
            return true;
        }
        return ip.matches(IP_PATTERN);
    }

    /**
     * Core Logic for 2.1.1: Checks if an actual IP falls within a CIDR range.
     */
    public static boolean isInRange(String cidrPattern, String actualIp) {
        if (cidrPattern == null || cidrPattern.equalsIgnoreCase("ANY") || cidrPattern.equals("0.0.0.0")) {
            return true;
        }

        if (!cidrPattern.contains("/")) {
            return cidrPattern.equals(actualIp);
        }

        try {
            String[] parts = cidrPattern.split("/");
            String networkAddress = parts[0];
            int prefixLength = Integer.parseInt(parts[1]);

            int mask = (prefixLength == 0) ? 0 : 0xffffffff << (32 - prefixLength);

            int networkInt = ipToInt(networkAddress);
            int actualInt = ipToInt(actualIp);

            return (actualInt & mask) == (networkInt & mask);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Converts a dot-decimal IP string to a 32-bit signed integer.
     */
    private static int ipToInt(String ipAddress) {
        String[] octets = ipAddress.split("\\.");
        int result = 0;
        for (int i = 0; i < 4; i++) {
            result |= (Integer.parseInt(octets[i]) << (24 - (8 * i)));
        }
        return result;
    }

    /**
     * Checks if a specific value matches a rule pattern (for Protocol matching).
     */
    public static boolean matchValue(String pattern, String actualValue) {
        if (pattern == null || pattern.equalsIgnoreCase("ANY") || pattern.equals("0.0.0.0")) {
            return true;
        }
        return pattern.equalsIgnoreCase(actualValue);
    }

    /**
     * Updated for 2.1.2: Checks if a packet's port matches a single port or a range.
     * Supports: "80", "80-443", "ANY", or "-1"
     */
    public static boolean matchPort(String rulePort, int packetPort) {
        // 1. Handle Wildcards
        if (rulePort == null || rulePort.equalsIgnoreCase("ANY") || rulePort.equals("-1")) {
            return true;
        }

        try {
            // 2. Handle Port Ranges (e.g., "80-443")
            if (rulePort.contains("-")) {
                String[] parts = rulePort.split("-");
                int minPort = Integer.parseInt(parts[0].trim());
                int maxPort = Integer.parseInt(parts[1].trim());
                
                return packetPort >= minPort && packetPort <= maxPort;
            } 
            
            // 3. Handle Single Port (e.g., "80")
            int singlePort = Integer.parseInt(rulePort.trim());
            return singlePort == packetPort;

        } catch (NumberFormatException e) {
            System.err.println("Invalid port format in rule: " + rulePort);
            return false;
        }
    }
}
