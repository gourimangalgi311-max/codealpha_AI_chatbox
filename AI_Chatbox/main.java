import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Task 3: Java Artificial Intelligence Chatbot
 * Java backend + static website files in public/
 *
 * Run from this folder:
 *   java --add-modules jdk.httpserver main.java
 */
class main {
    private static final int PORT = 8080;
    private static final Map<String, String> faq = new LinkedHashMap<>();
    private static final List<String> chatLog = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) throws Exception {
        loadKnowledgeBase();

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/api/chat", main::handleChat);
        server.createContext("/api/faqs", main::handleFaqs);
        server.createContext("/", main::serveWebsite);
        server.setExecutor(null);
        server.start();

        System.out.println("======================================");
        System.out.println("       JAVA AI CHATBOT WEBSITE");
        System.out.println("======================================");
        System.out.println("Website running at: http://localhost:" + PORT);
        System.out.println("Keep this terminal open while using it.");
        System.out.println("Press Ctrl+C to stop the server.");
    }

    private static void loadKnowledgeBase() {
        faq.put("hello", "Hello! Welcome to Java AI Chatbot. How can I help you today?");
        faq.put("hi", "Hi there! What would you like to know?");
        faq.put("hey", "Hey! I am here to help. Ask me a question.");
        faq.put("your name", "I am Java AI Chatbot, a rule-based virtual assistant.");
        faq.put("who are you", "I am a Java-based chatbot created for an Artificial Intelligence project.");
        faq.put("what can you do", "I can answer common questions, explain basic programming topics, and respond to greetings.");
        faq.put("artificial intelligence", "Artificial Intelligence (AI) is the ability of computer systems to perform tasks that usually require human intelligence.");
        faq.put("what is ai", "AI stands for Artificial Intelligence. It helps computers perform tasks such as learning, reasoning, and understanding language.");
        faq.put("machine learning", "Machine Learning is a part of AI in which computers learn patterns from data and use them to make predictions.");
        faq.put("nlp", "Natural Language Processing (NLP) helps computers process, understand, and respond to human language.");
        faq.put("what is java", "Java is a high-level, object-oriented programming language used to build desktop, web, and mobile applications.");
        faq.put("what is oop", "OOP stands for Object-Oriented Programming. Its main concepts include encapsulation, inheritance, polymorphism, and abstraction.");
        faq.put("html", "HTML stands for HyperText Markup Language. It defines the structure of a web page.");
        faq.put("css", "CSS stands for Cascading Style Sheets. It controls the appearance and layout of web pages.");
        faq.put("javascript", "JavaScript adds interaction and dynamic behaviour to websites.");
        faq.put("thank you", "You're welcome! Feel free to ask me another question.");
        faq.put("thanks", "Happy to help!");
        faq.put("bye", "Goodbye! Have a wonderful day.");
        faq.put("help", "You can ask me about AI, machine learning, NLP, Java, OOP, HTML, CSS, or JavaScript.");
        faq.put("time", "The current server time is " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")) + ".");
    }

    private static void handleChat(HttpExchange exchange) throws IOException {
        addCors(exchange);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            send(exchange, 204, "", "text/plain; charset=utf-8");
            return;
        }
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, "{\"error\":\"Use POST to send a message.\"}");
            return;
        }

        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> values = parseForm(body);
        String message = values.getOrDefault("message", "").trim();

        if (message.isEmpty()) {
            sendJson(exchange, 400, "{\"error\":\"Please type a message first.\"}");
            return;
        }

        String response = generateResponse(message);
        String safeMessage = jsonEscape(message);
        String safeResponse = jsonEscape(response);

        chatLog.add("You: " + message);
        chatLog.add("Bot: " + response);
        if (chatLog.size() > 100) {
            chatLog.subList(0, chatLog.size() - 100).clear();
        }

        sendJson(exchange, 200,
            "{\"reply\":\"" + safeResponse + "\",\"message\":\"" + safeMessage + "\"}");
    }

    private static String generateResponse(String input) {
        String normalized = input.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        // Exact or phrase matching against the FAQ knowledge base.
        for (Map.Entry<String, String> entry : faq.entrySet()) {
            if (normalized.equals(entry.getKey()) ||
                normalized.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        // Simple NLP-style keyword and intent matching.
        if (containsAny(normalized, "good morning", "morning")) {
            return "Good morning! I hope you have a productive day. What can I help you with?";
        }
        if (containsAny(normalized, "good evening", "evening")) {
            return "Good evening! How may I assist you?";
        }
        if (containsAny(normalized, "how are you", "how r u")) {
            return "I am working properly and ready to answer your questions. How are you?";
        }
        if (containsAny(normalized, "weather", "temperature")) {
            return "I cannot access live weather data, but I can explain weather-related concepts.";
        }
        if (containsAny(normalized, "project", "task 3", "internship")) {
            return "This project demonstrates a Java chatbot with NLP-style text normalization, rule-based responses, an FAQ knowledge base, and a web interface.";
        }
        if (containsAny(normalized, "not understand", "don't understand", "meaning")) {
            return "Could you rephrase your question using a few simple words? I will try to find a matching answer.";
        }

        return "I am not sure about that yet. Try asking about Artificial Intelligence, Machine Learning, NLP, Java, OOP, HTML, CSS, or JavaScript.";
    }

    private static boolean containsAny(String text, String... words) {
        for (String word : words) {
            if (text.contains(word)) return true;
        }
        return false;
    }

    private static void handleFaqs(HttpExchange exchange) throws IOException {
        addCors(exchange);
        StringBuilder json = new StringBuilder("[");
        boolean first = true;
        for (String key : faq.keySet()) {
            if (!first) json.append(",");
            first = false;
            json.append("\"").append(jsonEscape(key)).append("\"");
        }
        json.append("]");
        sendJson(exchange, 200, json.toString());
    }

    private static void serveWebsite(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path == null || path.equals("/")) path = "/index.html";

        // Prevent path traversal outside the public directory.
        if (path.contains("..") || path.contains("\\")) {
            send(exchange, 400, "Bad request", "text/plain; charset=utf-8");
            return;
        }

        File file = new File("public" + path).getCanonicalFile();
        File publicDir = new File("public").getCanonicalFile();
        if (!file.toPath().startsWith(publicDir.toPath()) || !file.isFile()) {
            send(exchange, 404, "File not found", "text/plain; charset=utf-8");
            return;
        }

        String contentType = contentType(file.getName());
        byte[] data = java.nio.file.Files.readAllBytes(file.toPath());
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(200, data.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(data);
        }
    }

    private static String contentType(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".html")) return "text/html; charset=utf-8";
        if (lower.endsWith(".css")) return "text/css; charset=utf-8";
        if (lower.endsWith(".js")) return "application/javascript; charset=utf-8";
        if (lower.endsWith(".json")) return "application/json; charset=utf-8";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        return "application/octet-stream";
    }

    private static Map<String, String> parseForm(String body) {
        Map<String, String> result = new HashMap<>();
        for (String pair : body.split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2) {
                result.put(
                    URLDecoder.decode(parts[0], StandardCharsets.UTF_8),
                    URLDecoder.decode(parts[1], StandardCharsets.UTF_8)
                );
            }
        }
        return result;
    }

    private static String jsonEscape(String value) {
        return value.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }

    private static void addCors(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    private static void sendJson(HttpExchange exchange, int status, String body) throws IOException {
        send(exchange, status, body, "application/json; charset=utf-8");
    }

    private static void send(HttpExchange exchange, int status, String body, String type) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", type);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }
}
