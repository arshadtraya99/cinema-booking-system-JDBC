# 🎬 Smart Cinema Booking System

A desktop application built with **Java, Java Swing, MySQL, and JDBC** that manages movie screenings, theater seating, and ticket reservations — designed to automate the box office workflow, prevent double-booking, and give cinema management a clear view of theater occupancy.

---

## 📌 Project Overview

The Smart Cinema Booking System provides an interface for staff to book tickets for walk-in customers and manage show timings. It replaces manual seat tracking with a transaction-safe, database-driven booking engine and a visual seat map.

**Objective:** Automate the ticketing process, prevent double-booking of seats, and provide a clear overview of theater occupancy for cinema management.

---

## ✨ Main Features

| Feature | Description |
|---|---|
| **Movie Scheduling** | Set up showtimes and assign movies to specific screens |
| **Seat Management** | Grid-based graphical view of seat availability (Booked vs. Available) |
| **Dynamic Pricing** | Configurable rates across Gold, Silver, and Platinum seating tiers |
| **Booking Receipts** | Auto-generated summary of movie, seat numbers, and total cost |

---

## 👥 User Roles

- **Admin** — Manages movie listings and screen schedules
- **Ticket Clerk** — Handles seat selection and payment for customers

---

## 🧩 Modules

1. **Movie Management** — Add new releases, remove old ones
2. **Booking Engine** — Core logic for seat selection and database updates
3. **Showtime Module** — Manages timing and screen allocation

---

## 🗄️ Database Schema

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
| ScreenID | INT |
| Time | TIME |
| Date | DATE |

**Seats**
| Column | Type |
|---|---|
| SeatID | PK |
| ShowID | FK → Shows |
| Row | VARCHAR |
| Number | INT |
| IsBooked | BOOLEAN |

---

## 🔄 Project Workflow

1. Admin adds a new movie and schedules a show.
2. Customer selects a movie and time at the counter.
3. Clerk selects available seats from the UI grid.
4. A JDBC transaction marks seats as `Booked` and records the sale.
5. Ticket is printed/displayed.

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
│   │   ├── model/          # Movie, Show, Seat, Booking classes
│   │   ├── dao/            # JDBC data access objects
│   │   ├── ui/             # Swing forms (Admin, Clerk, Seat Grid)
│   │   ├── service/        # Booking engine / business logic
│   │   └── util/           # DB connection, helpers
├── database/
│   └── schema.sql          # Movies, Shows, Seats tables
├── resources/
├── README.md
└── .gitignore
```

---

## 🚀 Getting Started

1. Install **Java JDK** and **MySQL Server**.
2. Create the database using `database/schema.sql`.
3. Update JDBC connection details (URL, username, password) in the DB config class.
4. Compile and run the application.

---

## 🎯 Learning Outcomes

- Implementing database transactions to ensure data integrity during booking
- Advanced UI layout management for seat grids using Java Swing

---

## 🔮 Future Enhancements

- Loyalty point system for frequent moviegoers
- Food and beverage add-on booking during ticket purchase

---

## 📝 Conclusion

The Smart Cinema Booking System modernizes the box office experience, ensuring accurate seating and efficient management of movie schedules through a robust Java–JDBC backend.
