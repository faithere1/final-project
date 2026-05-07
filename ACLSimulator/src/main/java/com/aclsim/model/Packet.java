package com.aclsim.model;

public class Packet {
    private String sourceIp;
    private String destIp;
    private String protocol;
    private int destPort;

    public Packet(String sourceIp, String destIp, String protocol, int destPort) {
        this.sourceIp = sourceIp;
        this.destIp = destIp;
        this.protocol = protocol;
        this.destPort = destPort;
    }

    public String getSourceIp() { return sourceIp; }
    public String getDestIp() { return destIp; }
    public String getProtocol() { return protocol; }
    public int getDestPort() { return destPort; }
}