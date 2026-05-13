package com.aclsim.model;

public class Rule {
    private int id;
    private int priority;
    private String action;   // PERMIT or DENY
    private String protocol; // TCP, UDP, ICMP, ANY
    private String sourceIp;
    private String destIp;
    private String destPort; // CHANGED to String to support ranges like "80-443"

    // Constructor
    public Rule(int id, int priority, String action, String protocol, String sourceIp, String destIp, String destPort) {
        this.id = id;
        this.priority = priority;
        this.action = action;
        this.protocol = protocol;
        this.sourceIp = sourceIp;
        this.destIp = destIp;
        this.destPort = destPort;
    }

    // Getters
    public int getId() { return id; }
    public int getPriority() { return priority; }
    public String getAction() { return action; }
    public String getProtocol() { return protocol; }
    public String getSourceIp() { return sourceIp; }
    public String getDestIp() { return destIp; }
    public String getDestPort() { return destPort; }

    // Setters (Useful for the Edit feature in 2.1.3)
    public void setPriority(int priority) { this.priority = priority; }
    public void setAction(String action) { this.action = action; }
    public void setProtocol(String protocol) { this.protocol = protocol; }
    public void setSourceIp(String sourceIp) { this.sourceIp = sourceIp; }
    public void setDestIp(String destIp) { this.destIp = destIp; }
    public void setDestPort(String destPort) { this.destPort = destPort; }
}