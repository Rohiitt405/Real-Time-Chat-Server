# Real-Time Chat Server & Client

> A lightweight, multi-threaded real-time chat application built entirely with **pure Java** — no frameworks, no external libraries.

Connect multiple users simultaneously over TCP sockets, broadcast messages to the room, send private direct messages, and discover who's online — all from the terminal.

---

## Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Supported Commands](#supported-commands)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Example Session](#example-session)
- [Configuration](#configuration)
- [License](#license)

---

## Features

| Feature | Details |
|---|---|
| 🔗 **Multi-Client Support** | Handles many simultaneous connections, each served by a dedicated thread |
| 📢 **Broadcast Messaging** | Messages are instantly delivered to every connected participant |
| 🔒 **Private Messaging** | Send `/pm` messages visible only to the intended recipient |
| 👤 **Username Validation** | Alphanumeric + underscore usernames, case-insensitive uniqueness enforced |
| 🏷️ **Display Names** | Each user sets a separate display name shown alongside messages |
| 🔔 **Presence Notifications** | Room-wide announcements when anyone joins or leaves |
| 💬 **Slash Commands** | Built-in `/help`, `/users`, `/pm`, `/exit` command dispatcher |
| 🛡️ **Graceful Disconnection** | Handles both clean `/exit` and abrupt socket closures safely |
| ☕ **Zero Dependencies** | Uses only standard Java (`java.net`, `java.io`, `java.util.concurrent`) |

---

## Architecture

### High-Level Diagram

```
                      ┌─────────────────────────────────────┐
                      │         Server  (port 5000)          │
                      │     ServerSocket.accept()  loop      │
                      └──────────────────┬──────────────────┘
                                         │  spawns one thread per client
               ┌─────────────────────────┼─────────────────────────┐
               ▼                         ▼                         ▼
   ┌─────────────────────┐   ┌─────────────────────┐   ┌─────────────────────┐
   │   ClientHandler     │   │   ClientHandler     │   │   ClientHandler     │
   │  (Thread – User A)  │   │  (Thread – User B)  │   │  (Thread – User C)  │
   └──────────┬──────────┘   └──────────┬──────────┘   └──────────┬──────────┘
              └────────────────────┬──────────────────────────────┘
                                   │  shared, thread-safe access
               ┌───────────────────┼───────────────────┐
               ▼                   ▼                   ▼
   ┌───────────────────┐ ┌───────────────────┐ ┌───────────────────┐
   │     ChatRoom      │ │    UserService    │ │    User  Model    │
   │  (Registry  &     │ │ (Lookup  &        │ │  (username  &     │
   │   Broadcast)      │ │  Registration)    │ │   display name)   │
   └───────────────────┘ └───────────────────┘ └───────────────────┘
```

### Threading Model

**Server side**
- The main server thread blocks on `ServerSocket.accept()`.
- Each accepted socket is immediately handed to a new `ClientHandler` thread.
- `ConcurrentHashMap` and `ConcurrentHashMap.newKeySet()` keep shared state thread-safe across all handlers.

**Client side**
- The **main thread** reads `System.in` and forwards input to the server.
- A dedicated **receiver thread** continuously reads server output and prints it to the terminal — allowing the user to type and receive messages at the same time.

---

## Project Structure

```
RealTimeChatServer/
├── src/
│   └── com/
│       └── chat/
│           ├── Server.java             # Entry point — opens ServerSocket on port 5000
│           ├── Client.java             # Terminal client — connects, reads input, displays output
│           ├── ClientHandler.java      # Per-client Runnable: I/O loop, command parsing
│           ├── ChatRoom.java           # Thread-safe client registry & broadcast engine
│           ├── model/
│           │   └── User.java           # User entity (username + display name)
│           └── service/
│               └── UserService.java    # Thread-safe user registration & lookup
├── .gitignore
├── RealTimeChatServer.iml
└── README.md
```

---

## Supported Commands

Commands are entered directly in the client terminal while connected.

| Command | Syntax | What it does |
|---|---|---|
| `/help` | `/help` | Prints all available commands |
| `/users` | `/users` | Lists every currently connected user |
| `/pm` | `/pm <username> <message>` | Sends a private message — only the recipient sees it |
| `/exit` | `/exit` or `exit` | Disconnects cleanly and notifies the room |

---

## Prerequisites

- **Java Development Kit (JDK) 17 or higher** (developed and tested on OpenJDK 21)

Verify your installation:

```bash
java -version
javac -version
```

---

## Quick Start

### 1. Compile

From the project root, compile all source files into an `out/` directory:

```bash
# Windows (PowerShell / Command Prompt)
javac -d out src/com/chat/*.java src/com/chat/model/*.java src/com/chat/service/*.java

# macOS / Linux
javac -d out src/com/chat/*.java src/com/chat/model/*.java src/com/chat/service/*.java
```

### 2. Start the Server

```bash
java -cp out com.chat.Server
```

Expected output:
```
Chat server started...
Listening on port: 5000
```

Keep this terminal open. The server must be running before any clients connect.

### 3. Connect Clients

Open a **new terminal window** for each client and run:

```bash
java -cp out com.chat.Client
```

You can open as many client terminals as you like — each becomes an independent chat participant.

---

## Example Session

**Client 1 — Alice**
```
Connected to server!
Enter your username: alice
Enter your display name: Alice Cooper
You can now start chatting. Type 'exit' to leave.

Bob joined the chat.

> Hello everyone!
Alice Cooper: Hello everyone!

> /users
Online users:
 - alice
 - bob

> /pm bob Hey Bob, private message!
[Private] You -> Bob: Hey Bob, private message!
```

**Client 2 — Bob**
```
Connected to server!
Enter your username: bob
Enter your display name: Bob
You can now start chatting. Type 'exit' to leave.

Alice Cooper: Hello everyone!

[Private] Alice Cooper -> Bob: Hey Bob, private message!

> /pm alice Got it, thanks Alice!
[Private] You -> Alice Cooper: Got it, thanks Alice!

> exit
GoodBye!
Disconnected from server.
```

---

## Configuration

All runtime settings are simple constants in their respective source files.

| Setting | Default | File | Notes |
|---|---|---|---|
| **Server Port** | `5000` | [`Server.java`](src/com/chat/Server.java) | Port the server listens on |
| **Client Host** | `localhost` | [`Client.java`](src/com/chat/Client.java) | Server address the client connects to |
| **Client Port** | `5000` | [`Client.java`](src/com/chat/Client.java) | Must match the server port |

### Connecting Over a Local Network

Change `HOST` in `Client.java` to the server machine's local IP (e.g., `192.168.1.100`), ensure port `5000` is open in your firewall, and start clients on any machine in the same network.

---

## License

This project is open-source and available under the [MIT License](LICENSE).
