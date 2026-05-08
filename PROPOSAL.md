## **Project Proposal: Network Access Control List (ACL) Simulator**

**1. Project Overview**
The objective of this project is to develop a Java-based desktop application that simulates the behavior of Network Access Control Lists (ACLs) typically found in routers and firewalls. The application will allow network administrators or students to define a set of security rules and test "mock" network packets against those rules to see if they are permitted or denied, providing a safe sandbox environment to validate configurations before deploying them to live production hardware.

**2. Problem Statement**
Configuring network security is high-stakes; a single incorrect ACL rule can shut down critical business services or leave a network vulnerable. There is a need for a lightweight, visual tool that allows users to build, order, and test these rulesets offline, using a "First-Match" logic engine consistent with industry standards (like Cisco or Juniper).

**3. Technical Stack**
*   **Language:** Java 17+
*   **GUI Framework:** JavaFX (for a modern, responsive user interface)
*   **Database:** SQLite (for local, file-based persistence)
*   **Data Access:** JDBC (Java Database Connectivity)
*   **Build Tool:** Maven

**4. Key Features**
*   **Rule Management (CRUD):** Users can create, read, update, and delete ACL rules. Each rule includes Priority, Action (Permit/Deny), Protocol (TCP, UDP, ICMP), Source/Destination IP (supporting CIDR notation), and Destination Port.
*   **Simulation Engine:** A core logic module that takes a "Test Packet" input and iterates through the defined ACL ruleset to find the first matching criteria.
*   **Persistence:** All rulesets and simulation logs will be stored in a local SQLite database, allowing users to save and reload their configurations across sessions.
*   **Visual Feedback:** A JavaFX-based dashboard that highlights exactly which rule was triggered during a simulation, aiding in debugging complex rulesets.

**5. Object-Oriented Design**
The project will utilize several design patterns and OOP principles:
*   **Model-View-Controller (MVC):** To separate the UI logic from the simulation engine and database layers.
*   **Encapsulation:** Using robust Data Models for `Rule` and `Packet` objects.
*   **Service Layer:** A dedicated `SimulatorService` to handle the algorithmic matching logic, keeping the code modular and testable.

**6. Implementation Roadmap**
*   **Phase 1:** Database schema design and JDBC connectivity setup.
*   **Phase 2:** Development of the ACL logic engine (IP/Subnet matching).
*   **Phase 3:** JavaFX GUI implementation (Dashboard and Rule Editor).
*   **Phase 4:** Integration of the GUI with the Database and Logic engine.
*   **Phase 5:** Final testing, validation, and packaging. Completed.