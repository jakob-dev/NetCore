# 🚧 Network Core Plugin

> **Status:** Under Development  
> *This project is currently under heavy development. Features and APIs are subject to change.*

A modular all-in-one core system for Minecraft networks, designed to run seamlessly on both **Velocity** and **Spigot**. It provides essential network features such as a punishment system, scoreboards, and chat management, while offering a centralized API and database handling (MySQL & Redis) for other developers.

---

## 📂 Project Structure

The project is organized into several Maven modules:

* **`api`**: Contains interfaces and DTOs for developers.
* **`common`**: Shared logic, database implementations (MySQL/Redis).
* **`velocity`**: Proxy-side modules (e.g., Punishments).
* **`spigot`**: Backend-side modules (e.g., Scoreboards).

## 🚀 Planned Features

* [x] Developer API for Database Access
* [x] MySQL & Redis Implementation
* [x] Custom Scoreboard (Spigot)
* [x] Basic Chat Manager (Spigot)
* [x] PAPI and LuckPerms integration (Spigot)
* [ ] User management (API)
* [ ] Tablist Messages (Velocity)
* [ ] Global Punishment System (Velocity)

---

*Further documentation regarding installation and API usage will follow soon.*
