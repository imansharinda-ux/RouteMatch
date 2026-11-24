# RouteMatch 🚗🛺🏍️

**Real-Time Shared Travel Coordination System**

RouteMatch is a real-time **ride-sharing** desktop application built with Java socket programming and multithreading. It connects people who are already travelling on a route with others who need to go the same way, so they can share spare seats and split the travel cost.

> **Note:** RouteMatch is **not** a taxi-hailing app like PickMe or Uber. In those apps, a driver comes to the rider and goes wherever the rider wants. In RouteMatch, the driver is **already making their own journey** and simply offers their empty seats to people heading the same direction — similar to the carpooling idea behind BlaBlaCar.

---

## 📌 How It Works

**Example:**
Kasun is driving from Colombo to Kandy at 5:00 PM. Nimal needs to go from Colombo to Kandy at around 5:10 PM but has no vehicle.

1. Kasun posts an **offer**: `Colombo → Kandy, 5:00 PM, Car, 1 seat`
2. Nimal posts a **request**: `Colombo → Kandy, 5:10 PM, ±30 minutes`
3. The **server** finds all offers on the same route inside Nimal's time window and sends them back, sorted by the closest time.
4. Nimal picks Kasun's offer and books the seat.
5. Kasun gets an instant notification, and the seat count updates live on everyone's screen.

RouteMatch works with **any vehicle** — car, tuk-tuk, van or motorbike.

Every screen shows **real data** created by users through the server. Nothing is hard-coded.

---

## ✨ Features

- **Sign in** with a username, server IP and port
- **Home** — live list of ride offers with a Sri Lanka map that draws the selected route
- **Offer a ride** — start, destination, departure time, vehicle, seats and a pickup note, with a live route preview
- **Any town in Sri Lanka** — type a town or city name (2,400+ places) in the From / To fields and pick it from the suggestions
- **Find a ride** — start, destination, preferred time and a flexible window (±15 / 30 / 60 minutes)
- **Time-window matching** — the server returns every offer inside the window, closest departure first
- **Seat booking** — book or cancel a seat in one click
- **Safe concurrent booking** — if two riders try to book the last seat at the same time, only one succeeds (`ReentrantLock` per offer)
- **Live notifications** — drivers get notified when someone books or cancels, riders get notified when a new matching ride is posted
- **Live updates** — seat counts and offers update on all connected clients in real time
- **My rides** — your bookings, your offers with passengers, and the notification list
- **Page loading animation** between every page
- **Server dashboard** (password protected) — online users, active offers, open requests, matches, worker threads, connected clients and a live activity log

---

## 🧠 Operating System & Network Concepts Used

| Concept | Where it is used in RouteMatch |
|---|---|
| **Socket programming (TCP)** | All communication between clients and the server uses `ServerSocket` and `Socket` |
| **Client-server architecture** | One central server, many client applications |
| **Multithreading** | The server runs each connected client on its own worker thread from a thread pool (`ExecutorService`) |
| **Synchronization** | Seat booking is protected with a `ReentrantLock` per offer; shared data uses `ConcurrentHashMap` and `CopyOnWriteArrayList` |
| **Custom protocol design** | Plain-text message protocol, e.g. `OFFER\|col\|kan\|17:00\|CAR\|3\|Main gate` |
| **File system** | Server activity is written to `server.log` |

### Message protocol

| Client → Server | Server → Client |
|---|---|
| `LOGIN\|name` | `WELCOME\|name` |
| `OFFER\|from\|to\|HH:mm\|vehicle\|seats\|note` | `OFFER_OK\|id` |
| `FIND\|from\|to\|HH:mm\|window` | `MATCHES\|offers` |
| `BOOK\|id` | `BOOK_OK\|id` or `BOOK_FAIL\|id\|reason` |
| `CANCEL_BOOKING\|id` | `OFFERS\|offers` (broadcast) |
| `CANCEL_OFFER\|id` | `STATS\|online\|offers\|requests\|matches` (broadcast) |
| `LOGOUT` | `NOTIFY\|kind\|text`, `ERROR\|message` |

---

## 🛠️ Technologies

- **Java 17**
- **java.net** — `ServerSocket`, `Socket` (TCP)
- **java.util.concurrent** — `ExecutorService`, `ConcurrentHashMap`, `ReentrantLock`, `AtomicInteger`
- **java.time** — time-window calculations
- **Java Swing + Java2D** — custom-painted desktop GUI (no external libraries)
- **Inter Tight** font (SIL Open Font License)
- Sri Lankan towns data from [Geomelon](https://geomelon.dev) / Wikidata (CC0)
- **IntelliJ IDEA**, **Git & GitHub**

---

## 📁 Project Structure

```
RouteMatch/
├── src/
│   ├── common/   # Protocol, Offer, Request, Vehicle, Places (towns + road routes), lk_places.txt (Sri Lankan towns)
│   ├── server/   # Server, ClientHandler, MatchEngine, ServerLog, ServerDashboard
│   ├── ui/       # Theme, fonts, icons, logo, buttons, fields, loader, toast
│   └── client/   # ClientApp, Connection, NavBar, MapPanel and the pages
│                 # LoginScreen, HomeScreen, OfferScreen, RequestScreen (Find a ride), MatchListScreen (My rides)
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
You can also add `server.port=5050` to change the port (the default is `5000`). This file is listed in `.gitignore` and is never pushed to GitHub.

### 3. Compile
Open a terminal in the project folder and run:
```powershell
javac -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
```

### 4. Start the server
```powershell
java -cp "out;src" server.Server
```
The dashboard opens and asks for the admin password. The header shows the server's IP address (for example `Started 6:56 PM · 172.20.10.2`).

### 5. Start one or more clients
Open a new terminal for each client:
```powershell
java -cp "out;src" client.ClientApp
```
Enter a username, the server IP (`localhost` on the same computer) and the port, then press **Connect**.
Every user must use a different username.

### 6. Connect from other laptops
1. Connect all laptops to the same Wi-Fi network or phone hotspot.
2. Run the server on one laptop only.
3. On the other laptops, run only the client and enter the server laptop's IP address shown on the dashboard.
4. If the client cannot connect, allow the port through Windows Firewall on the server laptop (PowerShell as administrator):
   ```powershell
   New-NetFirewallRule -DisplayName "RouteMatch 5050" -Direction Inbound -Protocol TCP -LocalPort 5050 -Action Allow
   ```
5. Check the connection from a client laptop:
   ```powershell
   Test-NetConnection <server-ip> -Port 5050
   ```
   Some university Wi-Fi networks block connections between laptops. Use a phone hotspot in that case.

### Troubleshooting
| Problem | Fix |
|---|---|
| Dashboard shows `config.properties with admin.password was not found` | Create `config.properties` in the project folder and start the server from that folder |
| Log shows `Address already in use` | Another program (for example Docker) is using the port. Add `server.port=5050` to `config.properties` and use port `5050` in the client |
| Client shows `Could not reach a server` | Start the server first, and check the IP, port and firewall |
| `package common does not exist` when running | Compile the whole `src` folder with the command in step 3, not a single file |

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
