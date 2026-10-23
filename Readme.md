# 🎬 Book Your Show

A desktop application built with **Java 7 or above, Java Swing, MySQL, and JDBC** that manages movie screenings, theater seating, and ticket reservations — designed to automate the box office workflow, prevent double-booking, and give cinema management a clear view of theater occupancy across multiple locations.

---

## 📌 Project Overview

Book Your Show provides an interface for staff to book tickets for walk-in customers and manage show timings. It replaces manual seat tracking with a transaction-safe, database-driven booking engine and a visual seat map, and supports multiple theatres and locations.

**Objective:** Automate the ticketing process, prevent double-booking of seats, and provide a clear overview of theater occupancy for cinema management.

---

## ✨ Main Features

| Feature | Description |
|---|---|
| **Multi-Theatre Support** | Manage multiple theatre locations, each with their own screens |
| **Location Filtering** | Browse movies with showtimes at a selected theatre location |
| **Movie Scheduling** | Set up showtimes and assign movies to specific screens |
| **Account Access** | Register new accounts in MySQL and authenticate existing users against the database |
| **Seat Management** | Grid-based graphical view of seat availability (Booked vs. Available) |
| **Dynamic Pricing** | Configurable rates across Gold, Silver, and Platinum seating tiers |
| **Booking Receipts** | Auto-generated summary of movie, seat numbers, and total cost |
| **Booking History** | Full record of past transactions per show/clerk |

---

## 👥 User Roles

- **Admin** — Manages movie listings, theatres, screens, and schedules
- **Ticket Clerk** — Handles seat selection and payment for customers

---

## 🧩 Modules

1. **Movie Management** — Add new releases, remove old ones
2. **Booking Engine** — Core logic for seat selection and database updates
3. **Showtime Module** — Manages timing and screen allocation

---

## 🗄️ Database Schema

**Theatres**
| Column | Type |
|---|---|
| TheatreID | PK |
| TheatreName | VARCHAR |
| Location | VARCHAR |
| City | VARCHAR |

**Screens**
| Column | Type |
|---|---|
| ScreenID | PK |
| TheatreID | FK → Theatres |
| ScreenName | VARCHAR |
| TotalSeats | INT |

**Movies**
| Column | Type |
|---|---|
| MovieID | PK |
| Title | VARCHAR |
| Duration | INT |
| Genre | VARCHAR |

**Shows**
| Column | Type |
|---|---|
| ShowID | PK |
| MovieID | FK → Movies |
| ScreenID | FK → Screens |
| Time | TIME |
| Date | DATE |

**SeatCategory**
| Column | Type |
|---|---|
| CategoryID | PK |
| CategoryName | VARCHAR (Gold / Silver / Platinum) |
| Price | DECIMAL |

**Seats**
| Column | Type |
|---|---|
| SeatID | PK |
| ShowID | FK → Shows |
| CategoryID | FK → SeatCategory |
| Row | VARCHAR |
| Number | INT |
| IsBooked | BOOLEAN |

**Users**
| Column | Type |
|---|---|
| UserID | PK |
| Name | VARCHAR |
| Username | VARCHAR |
| Password | VARCHAR |
| Role | ENUM (Admin / Clerk) |

**Bookings**
| Column | Type |
|---|---|
| BookingID | PK |
| ShowID | FK → Shows |
| SeatID | FK → Seats |
| ClerkID | FK → Users |
| CustomerName | VARCHAR |
| TotalAmount | DECIMAL |
| BookingTime | DATETIME |

---

## 🔄 Project Workflow

1. Admin adds a theatre, screen, movie, and schedules a show.
2. Customer selects a movie and time at the counter.
3. Clerk selects available seats from the UI grid.
4. A JDBC transaction marks seats as `Booked` and records the sale in `Bookings`.
5. Ticket/receipt is printed or displayed.

---

## 🛠️ Technologies Used

- **Language:** Java
- **UI Framework:** Java Swing (`GridBagLayout` for the seat grid)
- **Database:** MySQL
- **Connectivity:** JDBC

---

## 📂 Suggested Project Structure

```
SmartCinemaBookingSystem/
├── src/
│   ├── main/
│   │   ├── model/          # Theatre, Screen, Movie, Show, Seat, Booking, User
│   │   ├── dao/             # JDBC data access objects
│   │   ├── ui/               # Swing forms (Admin, Clerk, Seat Grid)
│   │   ├── service/       # Booking engine / business logic
│   │   └── util/            # DB connection, helpers
├── database/
│   └── schema.sql          # Theatres, Screens, Movies, Shows, SeatCategory, Seats, Users, Bookings
├── resources/
├── README.md
└── .gitignore
```

---

## 🚀 Getting Started

1. Install **JDK 17 or later**, **Maven**, and **MySQL Server**.
2. Run `src/main/resources/cinema_booking_system.sql` in MySQL to create the schema.
3. Configure the connection with `CINEMA_DB_URL`, `CINEMA_DB_USER`, and
   `CINEMA_DB_PASSWORD` environment variables. The defaults are
   `jdbc:mysql://localhost:3306/Cinema_booking_System`, `root`, and an empty password.
   If your local MySQL root account has a password, set `CINEMA_DB_PASSWORD` in your
   terminal/session rather than hardcoding the password in Java. Avoid committing credentials.
   In PowerShell, for example, run `$env:CINEMA_DB_PASSWORD = "your-local-password"` before
   launching the app. You can also set `CINEMA_DB_URL` and `CINEMA_DB_USER` the same way.
4. Run `mvn test` to compile and test the project, or `mvn package` to build it.
5. Launch the application with `mvn exec:java`. Choose **Create a new account** to register, or
   sign in with an existing username and password. New accounts are saved in the `USERS` table;
   sign-in credentials are checked against that table.

The JDBC DAOs use the model classes in `src/main/java/model` and expect the schema
created by the SQL script above.

---

## 🎯 Learning Outcomes

- Implementing database transactions to ensure data integrity during booking
- Advanced UI layout management for seat grids using Java Swing
- Designing a normalized relational schema across multiple related entities

---

## 🔮 Future Enhancements

- Loyalty point system for frequent moviegoers
- Food and beverage add-on booking during ticket purchase
- Online booking portal alongside the counter-based system

---

## 📝 Conclusion

Book Your Show modernizes the box office experience, ensuring accurate seating and efficient management of movie schedules across multiple theatres through a robust Java–JDBC backend.
