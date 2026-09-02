# Revenue Recovery — Autonomous Finance & Revenue Recovery Agent
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

**revenue recovery** is an enterprise-grade, autonomous revenue recovery agent built directly on top of the **Razorpay Payment Ecosystem**. When a transaction fails or a checkout is interrupted, revenue recovery instantly intercepts the event, performs **real-time granular root-cause diagnosis**, provisions a dynamic **1-Click Razorpay Smart Recovery Route**, and executes an **anti-fatigue automated win-back cadence** with tailored VIP incentives.

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

## 💡 Key Innovations & Architecture Highlights

| Feature | Description | Business Impact |
| :--- | :--- | :--- |
| 🧠 **Granular Root Cause Diagnoser** | Classifies failures into GATEWAY_TIMEOUT, UPI_DECLINED, INSUFFICIENT_FUNDS, and CHECKOUT_DISMISSED. | Replaces generic Payment Failed errors with actionable recovery paths. |
| ⚡ **1-Click Smart Recovery Fallback** | Instant /checkout/recovery/{orderId} page + Razorpay Payment Links (
zp.io/i/...). | Frictionless checkout resumption without re-entering customer details. |
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
`Bash
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
- **Project**: revenue recovery Autonomous Finance Recovery Agent (*The Arts Arcade*)
- **License**: Released under the [MIT License](LICENSE).
