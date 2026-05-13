package com.aclsim.model;

public class Packet {
    private String sourceIp;
    private String destIp;
    private String protocol;
    private int destPort;
    private int icmpType; // NEW: 0-255 for ICMP, -1 for others.

    public Packet(String sourceIp, String destIp, String protocol, int destPort, int icmpType) {
        this.sourceIp = sourceIp;
        this.destIp = destIp;
        this.protocol = protocol;
        this.destPort = destPort;
        this.icmpType = icmpType;
    }

    public String getSourceIp() { return sourceIp; }
    public String getDestIp() { return destIp; }
    public String getProtocol() { return protocol; }
    public int getDestPort() { return destPort; }
    public int getIcmpType() { return icmpType; }
}