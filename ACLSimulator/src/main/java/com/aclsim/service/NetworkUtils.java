package com.aclsim.service;

public class NetworkUtils {

    // Updated Regex to allow optional CIDR notation (e.g., 192.168.1.0 or 192.168.1.0/24)
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
     * Example: isInRange("192.168.1.0/24", "192.168.1.50") -> true
     */
    public static boolean isInRange(String cidrPattern, String actualIp) {
        // 1. Handle wildcards
        if (cidrPattern == null || cidrPattern.equalsIgnoreCase("ANY") || cidrPattern.equals("0.0.0.0")) {
            return true;
        }

        // 2. If no CIDR slash is present, perform exact string match
        if (!cidrPattern.contains("/")) {
            return cidrPattern.equals(actualIp);
        }

        try {
            // 3. Split the CIDR pattern (e.g., "192.168.1.0/24")
            String[] parts = cidrPattern.split("/");
            String networkAddress = parts[0];
            int prefixLength = Integer.parseInt(parts[1]);

            // 4. Calculate the subnet mask using bitwise shifting
            // A /24 mask is 24 ones followed by 8 zeros
            int mask = (prefixLength == 0) ? 0 : 0xffffffff << (32 - prefixLength);

            // 5. Convert both IPs to 32-bit integers
            int networkInt = ipToInt(networkAddress);
            int actualInt = ipToInt(actualIp);

            // 6. Check if the network parts match
            return (actualInt & mask) == (networkInt & mask);
        } catch (Exception e) {
            System.err.println("Error parsing CIDR: " + cidrPattern);
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
            // Shift each octet to its correct position (24, 16, 8, 0 bits)
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
     * Checks if a port matches. -1 is considered "ANY".
     */
    public static boolean matchPort(int rulePort, int packetPort) {
        if (rulePort == -1) return true;
        return rulePort == packetPort;
    }
}