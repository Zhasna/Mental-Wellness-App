# Mental Wellness Journal App

A simple Jakarta Servlet-based web app for journaling, goals and mood tracking, backed by H2.

## Tech
- Java 17, Maven, Jakarta Servlet 6
- H2 embedded database
- Gson, BCrypt

## Setup
1. Ensure Java 17 and Maven are installed.
2. Build:
```bash
mvn clean package
```
3. Run on a Servlet container supporting Jakarta 6 (e.g., Tomcat 10.1+). Deploy the generated WAR from `target/MentalJournalApp.war`.

## Configuration
- Database stored under `./data/mental_journal` relative to working dir.
- Session cookies are HttpOnly and Secure by default (requires HTTPS to send cookies).

## Project Structure
- `src/main/webapp` static assets and HTML
- `src/main/java` DAOs, models, servlets, utils

## Notes
- Users table uses `password_hash` for BCrypt hashes.
- API endpoints under `/api/*` as configured in `WEB-INF/web.xml`.
- Security: global security headers filter and auth filter (rate limited login). Sessions are HttpOnly/Secure cookies.
- UI: cat-themed styling using CSS variables; header and badges updated with paw icons.
