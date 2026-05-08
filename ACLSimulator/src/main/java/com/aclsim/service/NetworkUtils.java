package com.aclsim.service;

public class NetworkUtils {

    private static final String IP_PATTERN =  "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

    public static boolean isValidIP(String ip){
        if (ip == null || ip.equalsIgnoreCase("ANY") || ip.equals("0.0.0.0"))
    return true;
        return ip.matches(IP_PATTERN);

    }
    /**
     * Checks if a specific value matches a rule pattern.
     * Supports "ANY" as a wildcard.
     */
    public static boolean matchValue(String pattern, String actualValue) {
        if (pattern == null || pattern.equalsIgnoreCase("ANY") || pattern.equals("0.0.0.0")) {
            return true;
        }
        return pattern.equals(actualValue);
    }

    /**
     * Checks if a port matches. -1 is considered "ANY".
     */
    public static boolean matchPort(int rulePort, int packetPort) {
        if (rulePort == -1) return true;
        return rulePort == packetPort;
    }
}