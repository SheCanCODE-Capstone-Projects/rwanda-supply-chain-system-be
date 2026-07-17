<<<<<<< HEAD
# React + Vite

This template provides a minimal setup to get React working in Vite with HMR and some ESLint rules.

Currently, two official plugins are available:

- [@vitejs/plugin-react](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react) uses [Oxc](https://oxc.rs)
- [@vitejs/plugin-react-swc](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react-swc) uses [SWC](https://swc.rs/)

## React Compiler

The React Compiler is not enabled on this template because of its impact on dev & build performances. To add it, see [this documentation](https://react.dev/learn/react-compiler/installation).

## Expanding the ESLint configuration

If you are developing a production application, we recommend using TypeScript with type-aware lint rules enabled. Check out the [TS template](https://github.com/vitejs/vite/tree/main/packages/create-vite/template-react-ts) for information on how to integrate TypeScript and [`typescript-eslint`](https://typescript-eslint.io) in your project.
=======
# Rwanda Supply Chain Network (RSCN) — Backend Core

The **Rwanda Supply Chain Network (RSCN)** is a cloud-based, multi-tenant B2B infrastructure platform designed to unify Rwanda's fragmented supply chain ecosystem. Rather than replacing existing internal ERPs or point-of-sale software, this platform operates as a shared national digital fabric. It synchronizes data and optimizes physical goods movement across producers, manufacturers, logistics providers, warehouses, retailers, financial institutions, and regulatory bodies.

##  Core Tech Stack (Target Architecture)

* **Language/Runtime:** JAVA + SpringBoot
* **Database (Primary):** PostgreSQL
* **Geospatial Extensions:** PostGIS
* **Caching & Message Broker:** Websocket (For real-time WebSocket session tracking, background job queuing, and geospatial caching)
* **Authentication:** OAuth2 / JWT with fine-grained Role-Based Access Control (RBAC)

##  System Modules & Deep-Dive Logic

### 1. Identity & Organization Management 
* **Multi-Tenancy:** Schema-level or logical isolation separating corporate business profiles. Supports multi-branch physical mapping.
* **KYC & Verification State Machine:** Manages company verification states (`PENDING`, `VERIFIED`, `REJECTED`, `SUSPENDED`) against official tax documents (TIN) or cooperative records.
* **Granular RBAC:** Distinct security contexts for `PRODUCER`, `MANUFACTURER`, `TRANSPORTER`, `WAREHOUSE_MAN`, `RETAILER`, `BANK_ANALYST`, and `GOVERNMENT_AUDITOR`.

### 2. Product & Inventory Track-and-Trace 
* **SKU & Batch Architecture:** Maps inventory by distinct production batches, handling expiry windows and quality certificates.
* **Valuation Engine:** Computes real-time asset ledger valuations using FIFO (First-In, First-Out) and Weighted Average Cost methodologies.
* **Alert Triggers:** Low-stock alerts and expiration warnings calculated via asynchronous cron workers.

### 3. Smart B2B Marketplace & RFQ Matcher 
* **RFQ Matrix:** Handles Request for Quotations, structured multi-party digital bidding, and automated contract generation.
* **Proximity Matching Algorithm:** Restricts supply/demand search bounds using geographical coordinates to match buyers with local producers, cutting pre-transport friction.

### 4. Geospatial Warehouse Management (WMS) 
* **Capacity Matrix:** Breaks down storage environments dynamically by total capacity volume, zones, and climate requirements (e.g., ambient, cold storage).
* **Reservation Engine:** Atomic database transactions to handle block space scheduling, mitigating race conditions when multiple businesses book overlapping space.

### 5. Transportation Management & Deadhead Mitigation 
* **Route Optimization & Empty Return (Deadhead) Mitigation:** Analyzes trip histories via PostGIS queries to identify empty inbound trucks returning from regional runs (e.g., Rubavu back to Kigali) and matches them automatically with pending local shipping requests.
* **Live Telemetry:** Ingests live driver GPS streams using lightweight WebSockets or HTTP batch polling, mapping arrival time estimations.
* **Cryptographic Proof of Delivery (PoD):** Digitizes delivery receipts with structural metadata tracking containing electronic signatures, timestamps, and cargo photo URLs.

### 6. Order Lifecycle State Machine 
Enforces strict deterministic mutations across the entire transaction chain:

$$\text{Quotation} \longrightarrow \text{Order} \longrightarrow \text{Invoice} \longrightarrow \text{Payment Locked} \longrightarrow \text{In Transit} \longrightarrow \text{Delivered} \longrightarrow \text{Disbursed}$$

* **Alternative Credit Scoring Ingestion:** Logs anonymous, behavioral supply chain telemetry data points (e.g., shipment reliability, ordering cadence, payment timelines) creating high-fidelity risk metrics accessible to partner banks to help securely underwrite small business loans.

##  Getting Started & Setup

### Prerequisites
* Docker & Docker Compose
* Git

### Local Environment Setup

1. Clone the repository 
2. Initialize Environment Variables:
3. run the docker container
4. start the application
>>>>>>> 29e91169fd6bd2a79048cd4abce53848867aa4f3
