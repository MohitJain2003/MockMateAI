# MockMate-AI 🎙️🤖

MockMate-AI is an interactive desktop application designed to simulate mock job interviews. It automatically generates tailored interview questions based on your specific job role, tech stack, and years of experience, allowing you to answer using real-time voice recognition and get immediate feedback.

---

## 🌟 Key Features

1. **User Authentication**: Simple login and registration screens linked to a database backend.
2. **Custom Interview Setup**: Specify your target Job Role, Tech Stack (e.g. React, MySQL), and Experience Level.
3. **AI-Powered & Fallback Question Generator**: Queries custom search APIs on the server-side to generate realistic questions, with intelligent fallback questions to ensure a smooth interview process.
4. **Voice-to-Text Transcription**: Leverages the Web Speech API (`webkitSpeechRecognition`) to let you answer by speaking directly to your microphone.
5. **Detailed Evaluation Reports**: Grades your responses based on elaboration and logs a feedback report with scores and recommended focus areas.

---

## 🛠️ Technology Stack

* **Frontend**: Electron, HTML5, Vanilla CSS3, Javascript (ES6)
* **Backend**: Spring Boot, Spring Data JPA, Spring Web
* **Database**: H2 Database (in-memory, no external installation required)

---

## 🚀 Running the Project Locally

### 1. Spring Boot Backend
The backend runs on **Java 17+** and utilizes Maven.

1. Open the `/backend` folder in your preferred Java IDE (such as IntelliJ IDEA or Eclipse) and import it as a Maven project.
2. Run the main class `com.mockmate.MockMateApplication`.
3. The server will run on `http://localhost:8080`.
4. You can view the live database tables by navigating to `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:mockmatedb`, Username: `sa`, Password: `password`).

### 2. Electron Frontend
1. Open a terminal in the root project folder.
2. Install npm dependencies:
   ```bash
   npm install
   ```
3. Run the desktop app:
   ```bash
   npm start
   ```

---

## 📂 Project Directory Structure

```
MockMateAI/
├── backend/                       # Spring Boot Backend Code
│   ├── src/main/java/com/mockmate/
│   │   ├── controller/            # Auth and Interview REST API controllers
│   │   ├── entity/                # Database Entities (User, Interview, etc.)
│   │   └── repository/            # Spring Data JPA Repositories
│   └── src/main/resources/        # application.properties configuration
│
└── resources/app/                 # Electron Frontend Code
    ├── index.html                 # Login / Register Splash Page
    ├── dashboard.html             # User dashboard & interview configuration form
    ├── interview.html             # Webcam preview, voice recorder, & submit answers
    ├── script.js                  # Frontend API client logic
    └── style.css                  # UI styling and slide animations
```
