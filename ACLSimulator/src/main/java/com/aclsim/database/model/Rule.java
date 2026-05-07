// Step 1.3: The `Rule.java` Model
// This is a standard Java class (POJO) representing a single ACL rule. It matches the columns we want in our database.


package com.aclsim.model;

public class Rule {
    private int id;
    private int priority;
    private String action;   // PERMIT or DENY
    private String protocol; // TCP, UDP, ICMP, ANY
    private String sourceIp;
    private String destIp;
    private int destPort;    // -1 for "any"

    // Constructor
    public Rule(int id, int priority, String action, String protocol, String sourceIp, String destIp, int destPort) {
        this.id = id;
        this.priority = priority;
        this.action = action;
        this.protocol = protocol;
        this.sourceIp = sourceIp;
        this.destIp = destIp;
        this.destPort = destPort;
    }

    // Getters and Setters
    public int getId() { return id; }
    public int getPriority() { return priority; }
    public String getAction() { return action; }
    public String getProtocol() { return protocol; }
    public String getSourceIp() { return sourceIp; }
    public String getDestIp() { return destIp; }
    public int getDestPort() { return destPort; }
}



