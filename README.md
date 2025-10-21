# Mental Wellness Journal App

🌐 **Live Demo:** https://mental-wellness-app-xg4t.onrender.com

A beautiful, calming web application for tracking your mental wellness journey.

![Java](https://img.shields.io/badge/Java-17-orange)
![Tomcat](https://img.shields.io/badge/Tomcat-10.1-yellow)
![H2](https://img.shields.io/badge/Database-H2-blue)
![License](https://img.shields.io/badge/License-MIT-green)

---

## ✨ Features

### 📝 **Journal Entries**
- Daily mood tracking with emoji indicators
- Rich text journal entries
- Date-based organization
- Gratitude-specific entries separated from regular journal

### 🎯 **Goals Management**
- Create and track personal goals
- Set target dates
- Mark goals as complete
- Visual progress indicators

### 📊 **Statistics & Analytics**
- Mood distribution charts (Chart.js)
- Mood trends over time
- Entry count tracking
- Visual insights into your wellness journey

### 📅 **Calendar View**
- Monthly calendar with mood indicators
- Quick entry lookup by date
- Multi-entry days supported
- Clean, intuitive interface

### 🍫 **Gratitude Box of Chocolates**
- Unique chocolate-themed gratitude jar
- Each gratitude represented as a chocolate piece
- 4 chocolate flavors (dark, milk, white, accent)
- Beautiful animations and hover effects
- Side panel for full gratitude view

### 🧘 **Meditation Timer**
- Preset durations (1, 3, 5, 10, 15 minutes)
- Custom time input
- Background meditation sound
- Completion chime
- Calming UI with animations

### 👤 **User Profile**
- Secure account management
- Password change functionality
- BCrypt password encryption
- Session-based authentication

---

## 🎨 Design

**Color Palette:** Soft Peach Theme
- Primary: `#FFCBB3` (Soft peach)
- Secondary: `#FFF0E6` (Light peach)
- Accent: `#FFB89A` (Warm peach)
- Background: Gradient from `#FFFAF7` to `#FFE6D9`

**Typography:** Inter font family

**UI/UX Principles:**
- Pastel color scheme for calm, relaxing experience
- Smooth animations and transitions
- Responsive design (mobile-friendly)
- Subtle shadows and glassmorphism effects
- Professional spacing and alignment

---

## 🛠️ Tech Stack

### Backend
- **Java 17** - Modern Java features
- **Jakarta Servlets** - Web request handling
- **H2 Database** - Embedded file-based database
- **BCrypt** - Password hashing
- **Gson** - JSON serialization

### Frontend
- **HTML5** - Semantic markup
- **CSS3** - Modern styling with variables, gradients, animations
- **Vanilla JavaScript** - No framework dependencies
- **Chart.js** - Data visualization
- **Fetch API** - Async HTTP requests

### Build & Deployment
- **Maven** - Dependency management & build
- **Apache Tomcat 10.1** - Servlet container
- **Docker** - Containerization
- **Render** - Cloud platform (recommended)

---

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+
- Git

### Local Development

```bash
# 1. Clone repository
git clone https://github.com/YOUR_USERNAME/mental-wellness-journal.git
cd mental-wellness-journal

# 2. Build WAR file
mvn clean package

# 3. Deploy to Tomcat
# Copy target/MentalJournalApp.war to Tomcat webapps/
# OR use Docker:

# 4. Run with Docker
docker build -t mental-wellness .
docker run -p 8080:8080 mental-wellness

# 5. Access app
http://localhost:8080
```

### Deploy to Render

See **[DEPLOYMENT.md](./DEPLOYMENT.md)** for complete deployment guide.

**Quick Deploy:**
1. Push code to GitHub
2. Create Blueprint on Render using `render.yaml`
3. Auto-deploys in 5-10 minutes!

---

## 📁 Project Structure

```
Mental_Wellness_JournalApp/
├── src/main/
│   ├── java/com/journal/
│   │   ├── dao/              # Database access layer
│   │   ├── filters/          # Security & auth filters
│   │   ├── models/           # Data models
│   │   ├── servlets/         # API endpoints
│   │   └── utils/            # Helper utilities
│   └── webapp/
│       ├── css/              # Stylesheets
│       ├── js/               # JavaScript
│       ├── assets/           # Audio, images, fonts
│       ├── WEB-INF/          # Web config
│       └── *.html            # Pages
├── target/
│   └── MentalJournalApp.war  # Deployable artifact
├── Dockerfile                # Docker configuration
├── render.yaml               # Render deployment config
├── pom.xml                   # Maven configuration
└── DEPLOYMENT.md             # Deployment guide
```

---

## 🔒 Security

- ✅ **BCrypt Password Hashing** - Industry-standard encryption
- ✅ **HTTP-only Cookies** - XSS protection
- ✅ **Session Management** - Secure user sessions
- ✅ **Content Security Policy** - Script injection prevention
- ✅ **Prepared Statements** - SQL injection protection
- ✅ **HTTPS** - Encrypted connections (on Render)

---

## 🗄️ Database Schema

### Users Table
```sql
CREATE TABLE users (
  id IDENTITY PRIMARY KEY,
  name VARCHAR(255),
  email VARCHAR(255) UNIQUE,
  password_hash VARCHAR(255),
  created_at TIMESTAMP
);
```

### Entries Table
```sql
CREATE TABLE entries (
  id IDENTITY PRIMARY KEY,
  user_id BIGINT,
  entry_date DATE,
  mood VARCHAR(50),
  content TEXT,
  created_at TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### Goals Table
```sql
CREATE TABLE goals (
  id IDENTITY PRIMARY KEY,
  user_id BIGINT,
  title VARCHAR(255),
  description TEXT,
  target_date DATE,
  completed BOOLEAN,
  created_at TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### Moods Table
```sql
CREATE TABLE moods (
  id IDENTITY PRIMARY KEY,
  user_id BIGINT,
  mood VARCHAR(50),
  logged_at TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id)
);
```

---

## 🧪 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/register` | Create new user account |
| POST | `/api/login` | Authenticate user |
| GET | `/api/entries` | Get user entries |
| POST | `/api/entries` | Create new entry |
| PUT | `/api/entries` | Update entry |
| DELETE | `/api/entries` | Delete entry |
| GET | `/api/goals` | Get user goals |
| POST | `/api/goals` | Create new goal |
| PUT | `/api/goals` | Update/toggle goal |
| DELETE | `/api/goals` | Delete goal |
| GET | `/api/stats` | Get user statistics |
| GET | `/api/profile` | Get user profile |
| PUT | `/api/profile` | Update profile |

---

## 🤝 Contributing

Contributions welcome! Please follow these steps:

1. Fork the repository
2. Create feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open Pull Request

---

## 📝 License

This project is licensed under the MIT License.

---

## 🙏 Acknowledgments

- **Chart.js** - Data visualization
- **Inter Font** - Google Fonts
- **BCrypt** - at.favre.lib
- **H2 Database** - Lightweight embedded database
- **Apache Tomcat** - Servlet container

---

## 📧 Contact

For questions or support, please open an issue on GitHub.

---

**Made with ❤️**

*A calming space for your mental wellness journey*
