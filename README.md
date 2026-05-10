
# 🎓 Smart Campus AI Project

An AI-powered Campus Management System that integrates **LLaMA** (via Ollama) with role-based dashboards for Students, Faculty, and Administrators.

---

## 📌 Tech Stack

| Category       | Technology                          |
|----------------|-------------------------------------|
| **Backend**    | Spring Boot                         |
| **Frontend**   | Thymeleaf + Bootstrap               |
| **Database**   | MySQL                               |
| **AI Model**   | LLaMA (via Ollama API)              |
| **Build Tool** | Maven                               |
| **Server**     | Embedded Tomcat                     |

---

## 🧩 Core Modules

| Module | Features |
|--------|----------|
| **User Module** | Admin, Student, Faculty roles |
| **Student Module** | Attendance, Results, Timetable, AI Study Tools |
| **Faculty Module** | Mark Attendance, Upload Materials, Enter Marks |
| **Admin Module** | Manage Users, Courses, Notices |
| **AI Study Assistant** | Notes Generator, Doubt Solver, Summary, MCQ Generator |

---

## 🏗️ System Architecture

```
Frontend (Thymeleaf UI)
        ↓
Spring Boot Controllers
        ↓
Service Layer
        ↓
AI Service Integration
        ↓
LLaMA Model (via Ollama API)
        ↓
Response returned to UI
```

---

## 🤖 AI Endpoints

| Endpoint | Purpose |
|----------|---------|
| `/api/ai/notes` | Generate structured notes from text |
| `/api/ai/doubt` | Explain questions like a teacher |
| `/api/ai/summary` | Convert long text into bullet points |
| `/api/ai/mcq` | Generate MCQ questions from study material |

---

## 👥 User Flows

### 🧑‍🎓 Student Flow
```
Login → Dashboard → View Attendance / Results / Timetable → AI Assistant → Generate Notes / Solve Doubts / Summary / MCQ
```

### 👨‍🏫 Faculty Flow
```
Login → Dashboard → Mark Attendance → Upload Materials → Use AI to Generate Questions/Notes
```

### 👑 Admin Flow
```
Login → Dashboard → Manage Students / Faculty / Departments / Courses / Notices
```

---

## 📁 Folder Structure

```
controller/
├── LoginController.java
├── StudentController.java
├── FacultyController.java
├── AdminController.java
└── AIController.java

service/
├── UserService.java
├── StudentService.java
├── FacultyService.java
├── AdminService.java
└── AIService.java

repository/
├── UserRepository.java
├── StudentRepository.java
└── NoticeRepository.java

model/
├── User.java
├── Student.java
├── Faculty.java
├── Admin.java
└── Notice.java

template/
├── login.html
├── student-dashboard.html
├── faculty-dashboard.html
├── admin-dashboard.html
└── ai-tools.html
```

---

## ✨ AI Features

- 🤖 AI Chatbot for student queries
- 📝 AI Notes Generator
- 📄 AI Summary Generator
- ❓ AI MCQ Generator
- 🧠 AI Doubt Solver

---

## 🚀 Quick Setup

```bash
# 1. Clone the repository
git clone https://github.com/SubhashreeACC/SmartCampus.git
cd SmartCampus

# 2. Configure MySQL (create database 'smartcampus')

# 3. Install Ollama and pull LLaMA
ollama pull llama2

# 4. Run the application
mvn clean install
mvn spring-boot:run
```

**Access:** `http://localhost:8080`

---

## 🔮 Future Enhancements

- Face Recognition Attendance
- Mobile App Integration
- Smart Campus Map
- Event Registration System
- Real‑time Notifications

---
## 👥 Contributors

Subhashree Ray Mohapatra
```
