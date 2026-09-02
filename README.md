# 🌸 RevWin — Autonomous Finance & Revenue Recovery Agent
### *AI-Driven Autonomous Win-Back Intelligence & Razorpay Smart Recovery Engine for Modern E-Commerce*

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Razorpay](https://img.shields.io/badge/Razorpay-Payment%20Gateway%20%26%20Links-0C2340?style=for-the-badge&logo=razorpay&logoColor=528FF0)](https://razorpay.com/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-38B2AC?style=for-the-badge&logo=tailwind-css&logoColor=white)](https://tailwindcss.com/)
[![Security](https://img.shields.io/badge/Security-OAuth2%20%2B%20HMAC--SHA256-blue?style=for-the-badge&logo=springsecurity&logoColor=white)](https://spring.io/projects/spring-security)
[![License](https://img.shields.io/badge/License-MIT-green.svg?style=for-the-badge)](LICENSE)

---

## 📌 Executive Overview

In modern e-commerce, **over 70% of potential sales are lost** due to unexpected payment failures, UPI PIN timeouts, bank gateway downtime, and shopping cart abandonments. Traditional merchant systems treat payment failure as a dead end, resulting in customer drop-off and lost marketing spend.

**RevWin** is an enterprise-grade, autonomous revenue recovery agent built directly on top of the **Razorpay Payment Ecosystem**. When a transaction fails or a checkout is interrupted, RevWin instantly intercepts the event, performs **real-time granular root-cause diagnosis**, provisions a dynamic **1-Click Razorpay Smart Recovery Route**, and executes an **anti-fatigue automated win-back cadence** with tailored VIP incentives.

> 🏆 **Built for the Razorpay Hackathon**: Transforming drop-offs into realized bank revenue autonomously with 0% customer spam.

---

## 📸 Visual Showcase & Platform Tour

### 1. Finance Agent Executive Control Center
*Real-time executive oversight tracking Gross Revenue at Risk, Won-Back Settled Revenue, Realized Value Ratio, and Autonomous Recovery Efficiency.*

![Finance Agent Control Center](./artifacts/Screenshot%202026-09-02%20220428.png)

---

### 2. Root Cause Diagnostic Breakdown & Cadence Stage Funnel
*Automated AI classification categorizing failures (Bank Gateway Outages, UPI Declines, Insufficient Balance, Cart Drops) and tracking stage-by-stage recovery conversion.*

![Diagnostic Breakdown and Win-Back Funnel](./artifacts/Screenshot%202026-09-02%20220443.png)

---

### 3. Live Interventions & Risk Pipeline
*Granular tracking of active interventions, cart value prioritization, diagnostic causes, and autonomous recovery actions in flight.*

![Interventions and Risk Pipeline](./artifacts/Screenshot%202026-09-02%20220448.png)

---

### 4. Immutable Audit Ledger & Event Execution Timeline
*Cryptographically transparent, real-time audit ledger logging event ingestion, root-cause diagnosis, stopping rules, and dispatch statuses.*

![Audit Ledger Timeline](./artifacts/Screenshot%202026-09-02%20220455.png)

---

### 5. Razorpay Standard Checkout & Hackathon Evaluator Simulation Lab
*Customer checkout interface paired with an on-screen Simulation Lab allowing hackathon judges to trigger realistic banking timeouts, UPI declines, and balance errors.*

![Razorpay Standard Checkout & Simulation Lab](./artifacts/Screenshot%202026-09-02%20220651.png)

---

### 6. 1-Click Smart Recovery Fallback Landing Page
*Customer-facing resilient checkout link (/checkout/recovery/{orderId}) with reserved countdown timer, root-cause guidance, and single-click retry via Razorpay.*

![1-Click Smart Recovery Landing Page](./artifacts/Screenshot%202026-09-02%20220711.png)

---

### 7. In-App Notification & Cart Abandonment Win-Back Dropdown
*Real-time notification bell alerting users to reserved carts and exclusive delivery perks before they leave the platform.*

![In-App Notification Dropdown](./artifacts/Screenshot%202026-09-02%20220615.png)

---

### 8. Merchant Administration Dashboard
*Unified merchant panel with direct access to catalog management, order processing, and the autonomous Finance Agent control center.*

![Merchant Admin Dashboard](./artifacts/Screenshot%202026-09-02%20220529.png)

---

## 📊 Data Flow Diagrams (DFD)

### 🔹 DFD Level 0 — System Context Diagram
The Level 0 Context Diagram depicts the high-level boundary of the **RevWin Autonomous Revenue Recovery Engine**, showing interactions between the Customer, Razorpay Payment Infrastructure, Merchant Administrator, and External Notification Channels.

`mermaid
graph TD
    classDef external fill:#1e293b,stroke:#3b82f6,stroke-width:2px,color:#fff;
    classDef system fill:#0f172a,stroke:#10b981,stroke-width:3px,color:#fff;

    Customer((👤 Customer)):::external
    Razorpay((💳 Razorpay Gateway & Webhooks)):::external
    Admin((🧑‍💼 Merchant Admin)):::external
    CommChannels((📱 WhatsApp / SMS / In-App Channels)):::external

    RevWinSystem[[⚙️ RevWin Autonomous Finance Recovery System]]:::system

    Customer -->|1. Places Order & Initiates Checkout| RevWinSystem
    RevWinSystem -->|2. Creates Razorpay Order / Payment Link| Razorpay
    Razorpay -->|3. Payment Failure / Timeout Webhook Event| RevWinSystem
    Razorpay -->|4. Payment Authorized / Settlement Event| RevWinSystem
    
    RevWinSystem -->|5. Real-Time Root Cause Diagnosis & Recovery Cadence| RevWinSystem
    RevWinSystem -->|6. Dispatches 1-Click Fallback Link & VIP Incentives| CommChannels
    CommChannels -->|7. Delivers Win-Back Notification| Customer
    Customer -->|8. Resumes Checkout via 1-Click Fallback Link| Razorpay
    
    RevWinSystem -->|9. Real-Time Analytics, Audit Ledger, & Simulation Controls| Admin
`

---

### 🔹 DFD Level 1 — Process Decomposition Diagram
The Level 1 Diagram breaks down the internal processes of RevWin: Event Ingestion, Diagnostic Engine, Razorpay Dynamic Fallback Link Creation, Autonomous Cadence Execution, and Immutable Audit Logging.

`mermaid
flowchart TD
    classDef proc fill:#1e1e38,stroke:#6366f1,stroke-width:2px,color:#fff;
    classDef store fill:#111827,stroke:#eab308,stroke-width:2px,color:#fff;
    classDef ext fill:#0f172a,stroke:#38bdf8,stroke-width:2px,color:#fff;

    RzpWebhook[(⚡ Razorpay Webhook Event: payment.failed / dismissed)]:::ext
    
    subgraph RevWin Internal Processing
        P1[1.0 Ingest Event & Verify Signature]:::proc
        P2[2.0 Granular Root Cause Diagnoser]:::proc
        P3[3.0 Razorpay Smart Fallback Route Generator]:::proc
        P4[4.0 Autonomous Cadence Scheduler & Anti-Fatigue Guardrails]:::proc
        P5[5.0 VIP Incentive & Promo Engine]:::proc
        P6[6.0 Immutable Audit Ledger Recorder]:::proc
        P7[7.0 Settlement & Stopping Rule Evaluator]:::proc
    end

    D1[(🗄️ PaymentRiskRecord Store)]:::store
    D2[(🗄️ AuditLog Store)]:::store
    D3[(🗄️ Orders & Promo Store)]:::store

    RzpSuccess[(✅ Razorpay Event: payment.authorized / payment_link.paid)]:::ext
    Outbox[(📲 Multi-Channel Dispatcher: SMS / WhatsApp / In-App Bell)]:::ext

    RzpWebhook --> P1
    P1 --> P6
    P1 --> P2
    P2 -->|Diagnosed Cause & Route Recommendation| P3
    P3 -->|Save Risk Record & Fallback URL| D1
    P3 --> P4
    
    P4 -->|Evaluate Time Interval & Stage Progression| P5
    P5 -->|Attach 10% VIP Promo if >= ₹10,000| D3
    P4 -->|Check Stopping Rules: Already Paid or Cross-Order Purchase?| P7
    P4 -->|Dispatch Notification| Outbox
    P4 -->|Log Step| P6
    P6 --> D2

    RzpSuccess --> P7
    P7 -->|Mark RECOVERED & Terminate Scheduled Cadence| D1
    P7 -->|Update Order to PAID| D3
    P7 -->|Log Revenue Won Back| P6
`

---

### 🔹 DFD Level 2 — Autonomous Win-Back Cadence & Stopping Rule Logic
The Level 2 Diagram illustrates the exact time-decay state machine, stopping rules, and VIP incentive branching implemented inside RevWinAgentService.java.

`mermaid
stateDiagram-v2
    [*] --> PaymentFailureEvent: Webhook Ingested

    state PaymentFailureEvent {
        [*] --> IngestPayload
        IngestPayload --> ClassifyRootCause: Inspect failure_code & failure_reason
        ClassifyRootCause --> GenerateSmartLink: Razorpay Smart Recovery Link (https://rzp.io/i/...)
    }

    PaymentFailureEvent --> Stage0_InstantLink: Stage 0 Dispatched
    
    state Stage0_InstantLink {
        [*] --> DeliverInstantFallback
        DeliverInstantFallback --> Wait5Minutes
    }

    Wait5Minutes --> CheckStoppingRulesStage1

    state CheckStoppingRulesStage1 <<choice>>
    CheckStoppingRulesStage1 --> Halted_OrderPaid: If Order Status == PAID
    CheckStoppingRulesStage1 --> Halted_OtherOrderPlaced: If Customer Bought Another Order Since Failure
    CheckStoppingRulesStage1 --> Stage1_5MinReminder: If Still Unpaid

    Stage1_5MinReminder --> Wait3Hours
    Wait3Hours --> CheckStoppingRulesStage2

    state CheckStoppingRulesStage2 <<choice>>
    CheckStoppingRulesStage2 --> Halted_OrderPaid: If Paid
    CheckStoppingRulesStage2 --> Halted_OtherOrderPlaced: If Alternative Purchase Made
    CheckStoppingRulesStage2 --> Stage2_3HrHold: Priority Cart Reservation Message

    Stage2_3HrHold --> Wait3HoursMore
    Wait3HoursMore --> CheckStoppingRulesStage3

    state CheckStoppingRulesStage3 <<choice>>
    CheckStoppingRulesStage3 --> Halted_OrderPaid: If Paid
    CheckStoppingRulesStage3 --> Stage3_VIP_Incentive: If Amount >= ₹10,000 (Attach 10% Auto Coupon)
    CheckStoppingRulesStage3 --> Stage3_Standard_Offer: If Amount < ₹10,000 (Free Priority Shipping)

    Stage3_VIP_Incentive --> WaitFinal3Hours
    Stage3_Standard_Offer --> WaitFinal3Hours

    WaitFinal3Hours --> Stage4_9HrFinalWarning: Reservation Expiration Warning
    Stage4_9HrFinalWarning --> EscalatedToHuman: Max Cadence Complete -> Human VIP Desk
`

---

## 💡 Key Innovations & Architecture Highlights

| Feature | Description | Business Impact |
| :--- | :--- | :--- |
| 🧠 **Granular Root Cause Diagnoser** | Classifies failures into GATEWAY_TIMEOUT, UPI_DECLINED, INSUFFICIENT_FUNDS, and CHECKOUT_DISMISSED. | Replaces generic Payment Failed errors with actionable recovery paths. |
| ⚡ **1-Click Smart Recovery Fallback** | Instant /checkout/recovery/{orderId} page + Razorpay Payment Links (zp.io/i/...). | Frictionless checkout resumption without re-entering customer details. |
| 🔄 **4-Stage Progressive Cadence** | Timed notifications: Stage 0 (Instant), Stage 1 (5 min), Stage 2 (+3 hr), Stage 3 (+3 hr VIP offer), Stage 4 (+3 hr final warning). | Recovers up to **49.3%** of dropped transactions over a 9-hour window. |
| 🎁 **Dynamic VIP Incentive Engine** | Generates dynamic 10% win-back promo codes (WINBACK-XXXX) for orders $\ge$ ₹10,000 at Stage 3. | Protects margins on low-value items while salvaging high-ticket carts. |
| 🛡️ **Anti-Fatigue Guardrails** | **Rule 1**: Halts all notifications when the order is paid.<br>**Rule 2**: Halts notifications if the customer placed any *other* paid order. | **0% customer spam** and complete brand reputation protection. |
| 🧪 **Evaluator Simulation Lab** | Built-in test panel allowing evaluators to simulate payment failures and advance cadence stages on demand. | Enables live, deterministic demonstration during hackathon evaluation. |
| 📜 **Immutable Event Audit Trail** | Chronological record of every event ingestion, diagnosis, dispatch, and settlement. | Enterprise-grade transparency and compliance for finance teams. |

---

## 🛠️ Technical Stack

- **Backend Framework**: Java 17, Spring Boot 3.x
- **Security & Auth**: Spring Security 6, Google OAuth2, Form Login, Role-Based Access (ROLE_USER, ROLE_ADMIN), HMAC-SHA256 Webhook Verification
- **Payment & Media**: Razorpay Java SDK, Razorpay Standard Checkout & Payment Links API, Cloudinary CDN
- **Data & ORM**: Spring Data JPA, Hibernate ORM, MySQL / H2 Embedded DB
- **Task Scheduling**: Spring Task Scheduler (@Scheduled cron loop running every 15s)
- **Frontend & UI**: Thymeleaf 3, Tailwind CSS, Flowbite Component Library, Chart.js, Vanilla JavaScript
- **Build & Package**: Maven, Dockerfile ready

---

## 🔌 API & Webhook Endpoints

### 1. Razorpay Webhook Ingestion
- **POST /api/webhooks/razorpay**
  - Ingests payment.failed, payment.authorized, and payment_link.paid events.
  - Automatically triggers root cause diagnosis, smart link generation, or payment settlement.

### 2. Evaluator Simulation Suite
- **POST /api/recovery/simulate-gateway-timeout**: Simulates an issuer bank server outage.
- **POST /api/recovery/simulate-upi-error**: Simulates a UPI decline / invalid VPA error.
- **POST /api/recovery/simulate-insufficient-funds**: Simulates card limit reached / low balance.
- **POST /api/recovery/simulate-advance-cadence**: Manually steps an order through its 4 recovery stages.
- **POST /api/recovery/simulate-payment-success**: Simulates customer settlement via fallback link.

### 3. Smart Recovery Route
- **GET /checkout/recovery/{orderNumber}**
  - Renders the responsive 1-click fallback checkout page with reserved countdown timer and direct Razorpay trigger.

---

## 🚀 Getting Started & Local Installation

### 1. Clone the Repository
`ash
git clone https://github.com/your-username/FlowerShop.git
cd FlowerShop/shop
`

### 2. Configure Environment Variables
Create a .env file inside the shop directory (refer to .env.example):
`env
RAZORPAY_KEY_ID=rzp_test_your_key_id
RAZORPAY_KEY_SECRET=your_razorpay_key_secret
CLOUDINARY_URL=cloudinary://your_api_key:your_api_secret@your_cloud_name
`

### 3. Build & Run the Application
`ash
# Using Maven Wrapper (Windows PowerShell)
.\mvnw.cmd clean spring-boot:run

# Or standard Maven
mvn clean spring-boot:run
`

The application will launch on: **http://localhost:8080**

### 4. Experience the Platform
- **Storefront & Catalog**: http://localhost:8080/
- **Customer Checkout**: http://localhost:8080/checkout
- **Finance Agent Control Center**: http://localhost:8080/admin/revenue-recovery *(Admin login required)*
- **Admin Dashboard**: http://localhost:8080/admin/dashboard

---

## 🔮 Future Scope & Roadmap

1. **AI Voice & Conversational Assistant**: Integrate generative voice agents (via WhatsApp Business & Twilio) to assist customers encountering bank failures and send instant personalized Razorpay payment links.
2. **Predictive Multi-PSP Smart Routing**: Dynamically switch gateway routes before a transaction fails based on real-time bank health metrics.
3. **Dynamic Split-Payment Fallbacks**: Automatically suggest 50-50 card/UPI split or EMI when high-value orders fail due to card limits.
4. **Autonomous Customer Loyalty Engine**: Grant instant loyalty cashback rewards on successfully recovered orders to reinforce repeat purchases.

---

## 👨‍💻 Author & Acknowledgements

- **Developed for**: Razorpay Hackathon 2026
- **Project**: RevWin Autonomous Finance Recovery Agent (*The Arts Arcade*)
- **License**: Released under the [MIT License](LICENSE).
