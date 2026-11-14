# 🏥 Jeevanta — Real-Time Emergency Healthcare Platform

> An integrated emergency healthcare and hospital bed management system with real-time bed reservations, one-tap ambulance dispatch, interactive map tracking, and an AI-powered medical consultation assistant.

---

## 🌟 Key Features

1. **Atomic Hospital Bed Reservation**:
   - Concurrency-safe hospital bed reservations using JDBC row-level locking (`SELECT ... FOR UPDATE`).
   - Prevents double-booking even under simultaneous user requests.
2. **One-Tap Emergency Ambulance Dispatch**:
   - Automatically identifies nearest hospitals, reserves a bed, and dispatches an ambulance.
   - Interactive live ambulance route simulation and real-time tracking powered by Leaflet & OpenStreetMap.
3. **AI Medical Consultant (Google Gemini)**:
   - Evaluates patient symptoms and cross-references them against their stored medical history (allergies, chronic conditions).
   - Generates critical safety warnings if a query conflicts with known allergies, and recommends medical specialists.
4. **Doctor Directory**:
   - Browse doctors by specialization, experience, hospital affiliation, consultation fees, and availability.
5. **Admin Management Console**:
   - Standalone CLI application allowing hospital administrators to view and update bed availability in real time.
6. **Patient Authentication & Profiles**:
   - Secure login, signup, and storage of patient medical history (past diagnoses, reports, allergies).

---

## 🏗️ Architecture & Tech Stack

```
                  ┌─────────────────────────────────────────┐
                  │       React 19 Frontend (Jeevanta)      │
                  │   Leaflet Maps • React Router • CSS3    │
                  └────────────────────┬────────────────────┘
                                       │ HTTP / REST (JSON)
                                       ▼
                  ┌─────────────────────────────────────────┐
                  │          Java Servlet Backend           │
                  │ Tomcat 9 • Java 11/17 • Gson • Servlets │
                  └──────┬───────────────────────────┬──────┘
                         │                           │
                         ▼                           ▼
            ┌────────────────────────┐  ┌─────────────────────────┐
            │      MySQL 8 / 5.7     │  │   Google Gemini API     │
            │   ACID Transactions    │  │ (gemini-1.5-flash AI)   │
            │     hackathon_db       │  └─────────────────────────┘
            └────────────────────────┘
```

- **Frontend**: React 19, React Router v7, Leaflet & React-Leaflet, OpenStreetMap
- **Backend**: Java 11/17, Java Servlets (JSR 369), Apache Maven, Google Gson
- **Database**: MySQL (InnoDB engine with row-level locks & transactions)
- **AI / LLM**: Google Gemini API (`gemini-1.5-flash`)

---

## 📂 Project Organization

```
Hackathon/
├── database/
│   └── schema.sql                  # MySQL database schema and seed data
├── src/
│   └── main/
│       ├── java/com/hackathon/
│       │   ├── admin/
│       │   │   └── AdminConsole.java   # Admin CLI tool for bed management
│       │   ├── dao/
│       │   │   ├── DoctorDAO.java      # Doctor queries
│       │   │   ├── HospitalDAO.java    # Atomic bed booking & hospital queries
│       │   │   └── UserDAO.java        # User authentication & profiles
│       │   ├── filter/
│       │   │   └── CorsFilter.java     # Global CORS filter for frontend integration
│       │   ├── model/
│       │   │   ├── Doctor.java         # Doctor entity model
│       │   │   ├── Hospital.java       # Hospital entity model
│       │   │   └── User.java           # User entity model
│       │   ├── service/
│       │   │   └── GeminiService.java  # Google Gemini AI integration
│       │   ├── servlet/
│       │   │   ├── AIConsultServlet.java
│       │   │   ├── AmbulanceServlet.java
│       │   │   ├── BookBedServlet.java
│       │   │   ├── DoctorServlet.java
│       │   │   ├── HospitalServlet.java
│       │   │   └── UserServlet.java
│       │   └── util/
│       │       └── DBUtil.java         # Centralized JDBC connection manager
│       └── webapp/
│           ├── index.html              # Backend API status page
│           ├── META-INF/context.xml    # Tomcat deployment context (/Hackathon)
│           └── WEB-INF/web.xml         # Web application deployment descriptor
├── Jeevanta/                           # React Frontend Application
│   ├── public/                         # Public assets & icons
│   ├── src/
│   │   ├── components/                 # Reusable UI components (Tracker, Header, etc.)
│   │   ├── hooks/                      # Custom hooks (useNearbyHospitals, useDoctors)
│   │   ├── pages/                      # Application views (BedBooking, Ambulance, etc.)
│   │   ├── App.js                      # Application routes
│   │   └── index.css                   # Global styling
│   ├── package.json                    # Frontend dependencies & scripts
│   └── .env.example                    # Frontend environment configuration
├── pom.xml                             # Maven build definition (builds Hackathon.war)
├── .gitignore                          # Clean Git ignore rules
└── .env.example                        # Backend environment template
```

---

## 🚀 Getting Started

### 1. Prerequisites
- **Java JDK**: 11 or higher
- **Apache Maven**: 3.8+ (or NetBeans / Eclipse with Tomcat 9)
- **Node.js**: 18+ and `npm`
- **MySQL Server**: 5.7+ or 8.0+

---

### 2. Database Setup

1. Start your local MySQL server.
2. Run the initialization script:
   ```bash
   mysql -u root -p < database/schema.sql
   ```
   *(Default password in script is `root`, configurable via environment variables).*

---

### 3. Backend Setup

#### Using Maven (Recommended):
```bash
# Package the WAR file
mvn clean package

# Deploy the generated target/Hackathon.war to your Tomcat webapps/ directory,
# or run with your preferred servlet container.
```

#### Environment Variables (Optional):
You can customize database connection and API keys using environment variables:
| Variable | Description | Default |
|----------|-------------|---------|
| `DB_URL` | JDBC Connection URL | `jdbc:mysql://localhost:3306/hackathon_db` |
| `DB_USER` | MySQL Username | `root` |
| `DB_PASS` | MySQL Password | `root` |
| `GEMINI_API_KEY` | Google Gemini API Key | *(Optional, for AI Consult)* |
| `GEMINI_MODEL` | Gemini Model Identifier | `gemini-1.5-flash` |

---

### 4. Admin CLI Console

To run the interactive Admin Console for viewing and updating bed counts:
```bash
# Compile and run
mvn compile
java -cp "target/classes;target/Hackathon/WEB-INF/lib/*" com.hackathon.admin.AdminConsole
```

---

### 5. Frontend Setup

```bash
cd Jeevanta

# Install dependencies
npm install

# Start development server
npm start
```
The React application will open at `http://localhost:3000`.

---

## 📡 REST API Reference

All endpoints run on `http://localhost:8084/Hackathon` (or your configured Tomcat port).

| Method | Endpoint | Request Body | Description |
|--------|----------|--------------|-------------|
| `GET` | `/api/hospitals` | *None* | List hospitals with available beds |
| `POST` | `/api/book-bed` | `{"hospitalId": 1, "userId": 1}` | Atomically book a bed |
| `POST` | `/api/book-ambulance` | `{"hospitalId": 1, "userId": 1, "userLat": 23.25, "userLng": 77.41}` | Reserve bed & dispatch ambulance |
| `POST` | `/api/ai-consult` | `{"patientId": 1, "query": "Can I take Amoxicillin?"}` | AI consultation with allergy check |
| `GET` | `/doctors` | *None* | List all registered doctors |
| `POST` | `/api/users` | `{"action": "login", "email": "...", "password": "..."}` | Patient authentication |
| `POST` | `/api/users` | `{"action": "create", "user": {"name": "...", "email": "...", "password": "..."}}` | Patient registration |

---

## 🔒 Security Best Practices

- API keys and sensitive credentials are read from environment variables (`GEMINI_API_KEY`, `DB_PASS`) rather than hardcoded in the codebase.
- Passwords are never returned in user API responses.
- Centralized CORS filter protects and manages allowed origins.

---

## 📄 License
This project is open-source and created for hackathon demonstration.

