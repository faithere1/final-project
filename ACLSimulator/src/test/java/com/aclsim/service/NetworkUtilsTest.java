package com.aclsim.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class NetworkUtilsTest {

    @Test
    public void testIsValidIP() {
        // Standard IPs
        assertTrue(NetworkUtils.isValidIP("192.168.1.1"));
        assertTrue(NetworkUtils.isValidIP("10.0.0.0"));
        assertTrue(NetworkUtils.isValidIP("ANY"));
        assertTrue(NetworkUtils.isValidIP("0.0.0.0"));

        // CIDR Blocks (2.1.1)
        assertTrue(NetworkUtils.isValidIP("192.168.1.0/24"));
        assertTrue(NetworkUtils.isValidIP("10.0.0.0/8"));

        // Wildcard Masks (2.1.4)
        assertTrue(NetworkUtils.isValidIP("192.168.1.0 0.0.0.255"));
        assertTrue(NetworkUtils.isValidIP("10.0.0.0 0.255.255.255"));

        // Invalid Formats
        assertFalse(NetworkUtils.isValidIP("abc.def.ghi.jkl"));
        assertFalse(NetworkUtils.isValidIP("192.168.1.300")); // Out of bounds octet
        assertFalse(NetworkUtils.isValidIP("192.168.1.0/35")); // Invalid CIDR prefix
    }

    @Test
    public void testIsInRange_CIDR() {
        // Test /24 matching (e.g., 192.168.1.0 to 192.168.1.255)
        String cidr = "192.168.1.0/24";
        assertTrue(NetworkUtils.isInRange(cidr, "192.168.1.50"));
        assertTrue(NetworkUtils.isInRange(cidr, "192.168.1.254"));
        assertFalse(NetworkUtils.isInRange(cidr, "192.168.2.1")); // Different subnet

        // Test /16 matching
        String cidrLarge = "172.16.0.0/16";
        assertTrue(NetworkUtils.isInRange(cidrLarge, "172.16.99.99"));
        assertFalse(NetworkUtils.isInRange(cidrLarge, "172.17.0.1"));
    }

    @Test
    public void testIsInRange_Wildcard() {
        // Test Class-A Wildcard (Only care about the first octet)
        String wildcard = "10.0.0.0 0.255.255.255";
        assertTrue(NetworkUtils.isInRange(wildcard, "10.55.123.4"));
        assertTrue(NetworkUtils.isInRange(wildcard, "10.255.255.254"));
        assertFalse(NetworkUtils.isInRange(wildcard, "11.0.0.1"));
    }

    @Test
    public void testMatchPort() {
        // Single Ports
        assertTrue(NetworkUtils.matchPort("80", 80));
        assertFalse(NetworkUtils.matchPort("80", 443));

        // Ranges (2.1.2)
        assertTrue(NetworkUtils.matchPort("80-443", 80));
        assertTrue(NetworkUtils.matchPort("80-443", 100));
        assertTrue(NetworkUtils.matchPort("80-443", 443));
        assertFalse(NetworkUtils.matchPort("80-443", 22));
        assertFalse(NetworkUtils.matchPort("80-443", 8080));

        // Wildcard Ports
        assertTrue(NetworkUtils.matchPort("ANY", 22));
        assertTrue(NetworkUtils.matchPort("-1", 443));
    }
}