# Task 3 - Java Artificial Intelligence Chatbot Website

This is the complete project folder, not just a single Java file.

## Project structure

```text
Java_AI_Chatbot_Task3_Complete/
├── main.java
├── README.txt
├── Run_Chatbot.bat
└── public/
    ├── index.html
    ├── css/
    │   └── style.css
    └── js/
        └── app.js
```

## Requirements
- JDK 17 or later
- Visual Studio Code or another code editor
- No Maven, Spring Boot, database, or external Java libraries required

## Run in VS Code
1. Extract the ZIP file.
2. In VS Code choose File -> Open Folder.
3. Select `Java_AI_Chatbot_Task3_Complete`.
4. Open Terminal -> New Terminal.
5. Make sure the terminal path is the folder containing `main.java`.
6. Run:

```powershell
java --add-modules jdk.httpserver main.java
```

7. Keep the terminal open.
8. Open Chrome and visit:

```text
http://localhost:8080
```

To stop the server, return to the terminal and press Ctrl+C.

You can also double-click `Run_Chatbot.bat` on Windows.

## What the project demonstrates
- Java-based chatbot backend
- NLP-style text normalization and keyword/intent matching
- Rule-based response generation
- FAQ knowledge base
- Website GUI for real-time chat
- Separate HTML, CSS, and JavaScript files
- Java HTTP server API for browser-to-Java communication

## Add or change FAQ answers
Open `main.java` and find the `loadKnowledgeBase()` method.
Add another line using this pattern:

```java
faq.put("your question keyword", "Your chatbot answer.");
```

Save the file, stop the running server with Ctrl+C, and start it again.

## Important project note
This is a beginner-friendly rule-based educational chatbot. It demonstrates NLP-style preprocessing and intent/keyword matching; it is not a trained machine-learning model or a connection to an online AI service.
