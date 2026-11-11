# RouteMatch 🚗🛺🏍️

**Real-Time Shared Travel Coordination System**

RouteMatch is a real-time **ride-sharing** desktop application built with Java socket programming and multithreading. It connects people who are already travelling on a route with others who need to go the same way, so they can share spare seats and split the travel cost.

> **Note:** RouteMatch is **not** a taxi-hailing app like PickMe or Uber. In those apps, a driver comes to the rider and goes wherever the rider wants. In RouteMatch, the driver is **already making their own journey** and simply offers their empty seats to people heading the same direction — similar to the carpooling idea behind BlaBlaCar.

---

## 📌 How It Works

**Example:**
Kasun is riding his bike from Campus to Galle at 4:30 PM. Nimal needs to go from Campus to Galle at around 4:00 PM but has no vehicle.

1. Kasun posts an **offer**: `Campus → Galle, 4:30 PM, Bike, 1 seat`
2. Nimal posts a **request**: `Campus → Galle, 4:00 PM, ±30 minutes`
3. The **server** finds all offers on the same route within Nimal's time window and shows them, sorted by the closest time.
4. Nimal picks Kasun's offer and books the seat.
5. Kasun gets an instant notification, and the seat count updates live on everyone's screen.

RouteMatch works with **any vehicle** — car, tuk-tuk, van, motorbike, etc.

---

## ✨ Features

- **User login** with a username
- **Post a ride offer** — start, destination, departure time, vehicle type, available seats
- **Post a ride request** — start, destination, preferred time, flexible time window (±15 / 30 / 60 minutes)
- **Time-window matching** — shows all offers inside the rider's time window, sorted by the closest departure time
- **Seat booking** — the rider chooses an offer and books a seat
- **Safe concurrent booking** — if two riders try to book the last seat at the same time, only one succeeds (handled with locks)
- **Live notifications** — the driver is notified instantly when someone books
- **Live updates** — seat counts update on all connected clients in real time
- **Cancellation** — riders can cancel bookings and drivers can cancel offers; affected users are notified
- **Server dashboard** (password protected) — shows online users, active offers and requests, matches, and a live activity log

---

## 🧠 Operating System & Network Concepts Used

| Concept | Where it is used in RouteMatch |
|---|---|
| **Socket programming (TCP)** | All communication between clients and the server uses `ServerSocket` and `Socket` |
| **Client-server architecture** | One central server, many client applications |
| **Multithreading** | The server creates a separate thread for each connected client |
| **Synchronization** | Shared offer/request lists and seat booking are protected with locks to prevent race conditions |
| **Custom protocol design** | Plain-text message protocol, e.g. `OFFER\|Campus\|Galle\|16:30\|BIKE\|1` |
| **File system** | Server activity logs are written to files |

---

## 🛠️ Technologies

- **Java 17**
- **java.net** — `ServerSocket`, `Socket` (TCP)
- **java.util.concurrent** — `ExecutorService`, `ConcurrentHashMap`, `ReentrantLock`
- **java.time** — time-window calculations
- **Java Swing** + **FlatLaf** — desktop GUI
- **IntelliJ IDEA**
- **Git & GitHub**

---

## 📁 Project Structure

```
RouteMatch/
├── src/
│   ├── server/      # Server, ClientHandler, MatchEngine, ServerDashboard
│   ├── client/      # ClientApp and GUI screens (Login, Offer, Request, Match List)
│   └── common/      # Shared classes: Offer, Request, Protocol
├── config.properties   # Local only — not pushed to GitHub
├── .gitignore
└── README.md
```

---

## ▶️ How to Run

### 1. Clone the repository
```bash
git clone https://github.com/imansharinda-ux/RouteMatch.git
cd RouteMatch
```

### 2. Create the config file
Create a file named `config.properties` in the project root:
```
admin.password=<your-admin-password>
```
> This file is listed in `.gitignore` and is never pushed to GitHub. Ask a team member for the password.

### 3. Start the server
Run `server/Server.java` (enter the admin password to open the dashboard).

### 4. Start one or more clients
Run `client/ClientApp.java`. Open several clients to test multiple users at the same time.

---

## 👥 Team

| Name | Registration Number |
|---|---|
| Sharinda W.G.I. | EG/2023/5889 |
| Walakanda W.K.C.D. | EG/2023/5939 |
| Nilwakke N.M.S.S.B. | EG/2022/5217 |

---

## 🎓 Course Information

**EC5205 – Operating Systems and Network Programming**
Department of Electrical and Information Engineering
Faculty of Engineering, University of Ruhuna

---

## 🚧 Status

This project is currently **under development**.
