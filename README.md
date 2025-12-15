# 💣 Super Bomberman Remake

![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-Used-4285F4?style=for-the-badge&logo=java&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-Build-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![Status](https://img.shields.io/badge/Status-Completed-success?style=for-the-badge)

> A robust, feature-rich recreation of the classic arcade game Bomberman, engineered with Java and JavaFX using a Model-View-Controller (MVC) architecture.

---

## 📖 Overview

**Super Bomberman Remake** is a strategic arcade game developed to demonstrate advanced Object-Oriented Programming (OOP) concepts and modern Java UI development. Beyond simple gameplay, this project features a custom game engine, a dynamic level editor, and a user management system.

This project was built to showcase clean architecture, design pattern implementation (Factory, Singleton, Observer), and rigorous testing.

## ✨ Key Features

### 🎮 Gameplay Mechanics
* **Classic Arcade Action:** Place bombs, destroy soft blocks, and eliminate enemies.
* **Smart Enemy AI:** Enemies with basic pathfinding capabilities.
* **Dynamic Power-Up System:** Includes `SpeedUp`, `BombUp`, `RangeUp`, `WallPass`, `BombPass`, and the dangerous `SkullMalus`.
* **Scoring System:** Persistent high scores tracked per user.

### 🛠️ Tools & Customization
* **Level Editor:** A fully functional GUI tool allowing users to design, save, and play custom maps (`.txt` serialization).
* **Theme Engine:** Switch dynamically between visual themes (Classic, Winter, Purple) impacting sprites and UI assets via the Options menu.
* **User Management:** Authentication system (Register/Login) with data persistence stored in properties files.

### ⚙️ Technical Highlights
* **MVC Architecture:** Strict separation of concerns between Game Logic (`model`), UI (`view/fxml`), and User Input (`controller`).
* **Design Patterns:**
    * **Factory Pattern:** Used for generating various Power-Ups (`PowerUpFactory`).
    * **Singleton Pattern:** Management of game states and configurations.
    * **Observer Pattern:** Handling UI updates and theme switching events.
* **JavaFX Integration:** Rich UI components using FXML and CSS styling.

---

## 🚀 Getting Started

### Prerequisites
* **Java Development Kit (JDK):** Version 17 or higher.
* **Maven:** For dependency management and building.

### Installation & Run

1.  **Clone the repository**
    ```bash
    git clone git@github.com:julesfuselier/S201-SuperBomberman-Remake.git
    cd s201-superbomberman-remake
    ```

2.  **Build the project**
    ```bash
    mvn clean install
    ```

3.  **Run the application**
    ```bash
    mvn javafx:run
    ```

---

## 🏗️ Project Structure

The project follows a standard Maven directory structure organized by logic layers:

````

src/main/java/com/superbomberman/
├── controller/       \# Handles user inputs and FXML logic (Menu, Game, Editor)
├── model/            \# Core game logic (Player, Bomb, Enemies, Map)
│   └── powerup/      \# PowerUp hierarchy and Factory
├── view/             \# Visual rendering logic
├── service/          \# Authentication and data persistence services
└── game/             \# Game loop, State management, and Input handling

````

## 🧪 Testing

Unit tests are implemented using **JUnit 5** to ensure the reliability of core mechanics (collisions, scoring, auth services).

To run tests:
```bash
mvn test
````

## 👨‍💻 Authors

**Jules Fuselier**

  * **Portfolio:** [Jules's portfolio](https://julesfuselier.fr)
  * **LinkedIn:** [Jules's Linkedin profile](https://www.linkedin.com/in/julesfuselier/)

**Hugo Brest-Lestrade (Yogoxxx)**

  * **Linkedin:** [Hugo's Linkedin profile](https://www.linkedin.com/in/hugo-brest-lestrade-44b6b5384/)

-----

*This project was developed as part of a computer science curriculum to master Java application development concepts.*
