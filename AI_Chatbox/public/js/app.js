const form = document.getElementById("chatForm");
const input = document.getElementById("messageInput");
const messages = document.getElementById("messages");
const welcome = document.getElementById("welcome");
const typing = document.getElementById("typing");
const sendBtn = document.getElementById("sendBtn");

function addMessage(text, who) {
  welcome.style.display = "none";
  const row = document.createElement("div");
  row.className = `message ${who}`;
  const avatar = document.createElement("div");
  avatar.className = "message-avatar";
  avatar.textContent = who === "user" ? "●" : "✦";
  const bubble = document.createElement("div");
  bubble.className = "bubble";
  bubble.textContent = text;
  row.append(avatar, bubble);
  messages.appendChild(row);
  window.scrollTo({ top: document.body.scrollHeight, behavior: "smooth" });
}

async function sendMessage(text) {
  const message = text.trim();
  if (!message) return;
  addMessage(message, "user");
  input.value = "";
  resizeInput();
  typing.hidden = false;
  sendBtn.disabled = true;

  try {
    const response = await fetch("/api/chat", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8" },
      body: new URLSearchParams({ message })
    });
    const data = await response.json();
    if (!response.ok) throw new Error(data.error || "The message could not be sent.");
    addMessage(data.reply, "bot");
  } catch (error) {
    addMessage("I could not connect to the Java server. Please make sure the terminal is still running and try again.", "bot");
  } finally {
    typing.hidden = true;
    sendBtn.disabled = false;
    input.focus();
  }
}

function resizeInput() {
  input.style.height = "auto";
  input.style.height = Math.min(input.scrollHeight, 120) + "px";
}

form.addEventListener("submit", event => {
  event.preventDefault();
  sendMessage(input.value);
});
input.addEventListener("input", resizeInput);
input.addEventListener("keydown", event => {
  if (event.key === "Enter" && !event.shiftKey) {
    event.preventDefault();
    form.requestSubmit();
  }
});

document.querySelectorAll("[data-prompt]").forEach(button => {
  button.addEventListener("click", () => sendMessage(button.dataset.prompt));
});

document.getElementById("newChatBtn").addEventListener("click", () => {
  messages.replaceChildren();
  welcome.style.display = "";
  input.focus();
});
document.getElementById("clearBtn").addEventListener("click", () => {
  messages.replaceChildren();
  welcome.style.display = "";
});
