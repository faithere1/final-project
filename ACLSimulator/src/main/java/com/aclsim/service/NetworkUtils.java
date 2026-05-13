package com.aclsim.service;

public class NetworkUtils {

    /**
     * Advanced Regex for 2.1.4: 
     * Supports Standard IP (1.1.1.1), CIDR (1.1.1.0/24), 
     * and Wildcard Masks (1.1.1.0 0.0.0.255).
     */
    private static final String IP_PATTERN = 
        "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)" +
        "(/([0-9]|[12][0-9]|3[0-2])|(\\s((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)))?$";

    /**
     * Checks if the input string is a valid IP, CIDR, or Wildcard Mask.
     */
    public static boolean isValidIP(String ip) {
        if (ip == null || ip.equalsIgnoreCase("ANY") || ip.equals("0.0.0.0")) {
            return true;
        }
        return ip.matches(IP_PATTERN);
    }

    /**
     * Logic for 2.1.1 and 2.1.4: 
     * Matches an IP against exact strings, CIDR blocks, or Wildcard Masks.
     */
    public static boolean isInRange(String pattern, String actualIp) {
        if (pattern == null || pattern.equalsIgnoreCase("ANY") || pattern.equals("0.0.0.0")) {
            return true;
        }

        try {
            int actualInt = ipToInt(actualIp);

            // --- Case A: Wildcard Mask (2.1.4) ---
            // Example: "192.168.1.0 0.0.0.255"
            if (pattern.contains(" ")) {
                String[] parts = pattern.split("\\s+");
                int networkInt = ipToInt(parts[0]);
                int wildcardInt = ipToInt(parts[1]);

                // Invert wildcard (0.0.0.255 becomes 255.255.255.0) to create a match mask
                int matchMask = ~wildcardInt;
                return (actualInt & matchMask) == (networkInt & matchMask);
            }

            // --- Case B: CIDR Notation (2.1.1) ---
            // Example: "192.168.1.0/24"
            if (pattern.contains("/")) {
                String[] parts = pattern.split("/");
                int networkInt = ipToInt(parts[0]);
                int prefixLength = Integer.parseInt(parts[1]);

                // Calculate mask (e.g., /24 = 255.255.255.0)
                int mask = (prefixLength == 0) ? 0 : 0xffffffff << (32 - prefixLength);
                return (actualInt & mask) == (networkInt & mask);
            }

            // --- Case C: Exact Match ---
            return pattern.equals(actualIp);

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Converts an IP string to a 32-bit Integer for bitwise math.
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
     * Matches protocol names (Case-Insensitive).
     */
    public static boolean matchValue(String pattern, String actualValue) {
        if (pattern == null || pattern.equalsIgnoreCase("ANY") || pattern.equals("0.0.0.0")) {
            return true;
        }
        return pattern.equalsIgnoreCase(actualValue);
    }

    /**
     * Matches single ports or ranges (2.1.2).
     */
    public static boolean matchPort(String rulePort, int packetPort) {
        if (rulePort == null || rulePort.equalsIgnoreCase("ANY") || rulePort.equals("-1")) {
            return true;
        }
        try {
            if (rulePort.contains("-")) {
                String[] parts = rulePort.split("-");
                int minPort = Integer.parseInt(parts[0].trim());
                int maxPort = Integer.parseInt(parts[1].trim());
                return packetPort >= minPort && packetPort <= maxPort;
            } 
            return Integer.parseInt(rulePort.trim()) == packetPort;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}