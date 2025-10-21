# 🚀 Mental Wellness Journal App - Deployment Guide

## 📋 Prerequisites
- GitHub account
- Render account (free tier available)
- Git installed locally
- Maven installed locally
- Java 17 installed

---

## 🔧 Pre-Deployment Setup

### 1. Build the WAR File
```bash
cd C:\Users\ASUS\OneDrive\Desktop\hehe\Mental_Wellness_JournalApp
mvn clean package
```

**Verify WAR exists:**
```bash
dir target\MentalJournalApp.war
```

### 2. Push to GitHub

#### First Time Setup:
```bash
# Initialize git (if not already done)
git init

# Add all files
git add .

# Commit
git commit -m "Deploy: Mental Wellness Journal App with soft peach theme"

# Create repository on GitHub first, then:
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/mental-wellness-journal.git
git push -u origin main
```

#### Subsequent Updates:
```bash
# Rebuild WAR
mvn clean package

# Add changes
git add .
git commit -m "Update: Description of changes"
git push
```

---

## 🌐 Deploy to Render

### Option A: Using render.yaml (Automatic - Recommended)

1. **Push Code to GitHub** (including `render.yaml` in root)

2. **Create New Web Service on Render:**
   - Go to [render.com](https://render.com)
   - Click **"New +"** → **"Blueprint"**
   - Connect your GitHub repository
   - Render will automatically detect `render.yaml`
   - Click **"Apply"**

3. **Wait for Deployment** (5-10 minutes)
   - Render will build the Docker image
   - Deploy the container
   - Provision persistent disk for database

4. **Access Your App:**
   - URL: `https://mental-wellness-journal.onrender.com`
   - First load may take 30-60 seconds (free tier cold start)

---

### Option B: Manual Setup

1. **Create New Web Service:**
   - Click **"New +"** → **"Web Service"**
   - Connect GitHub repository

2. **Configure Service:**
   - **Name:** `mental-wellness-journal`
   - **Runtime:** `Docker`
   - **Region:** Choose closest to you
   - **Branch:** `main`
   - **Plan:** `Free`

3. **Environment Variables:**
   - `DB_PATH` = `/opt/render/project/data/mental_journal`
   - `JAVA_OPTS` = `-Djava.security.egd=file:/dev/./urandom -Xms256m -Xmx512m`

4. **Add Persistent Disk:**
   - Click **"Add Disk"**
   - **Name:** `mental-journal-data`
   - **Mount Path:** `/opt/render/project/data`
   - **Size:** `1 GB`

5. **Deploy:**
   - Click **"Create Web Service"**

---

## 🗄️ Database Information

- **Type:** H2 Database (embedded)
- **Mode:** File-based with persistent storage
- **Location:** `/opt/render/project/data/mental_journal.mv.db`
- **Persistence:** Data survives restarts via Render disk mount

### Database Tables:
- `users` - User accounts (BCrypt hashed passwords)
- `entries` - Journal entries and gratitude notes
- `goals` - User goals
- `moods` - Mood logs

---

## 🔐 Security Features

✅ **BCrypt Password Hashing** - All passwords encrypted  
✅ **Session Management** - Secure HTTP-only cookies  
✅ **Content Security Policy** - XSS protection  
✅ **HTTPS** - Automatic on Render  
✅ **SQL Injection Protection** - Prepared statements  

---

## 🧪 Testing After Deployment

### 1. Test Registration:
- Go to `/register.html`
- Create a new account
- Verify redirection to login

### 2. Test Login:
- Go to `/login.html`
- Login with created account
- Verify redirection to dashboard

### 3. Test Features:
- ✅ Create journal entry
- ✅ Add goal
- ✅ View stats
- ✅ Add gratitude chocolate
- ✅ Use meditation timer
- ✅ Check calendar
- ✅ Update profile

### 4. Test Persistence:
- Add data
- Restart service (Render dashboard)
- Verify data still exists

---

## 📊 Monitoring

### Render Dashboard:
- **Logs:** Real-time application logs
- **Metrics:** CPU, Memory usage
- **Events:** Deploy history

### Application Logs:
Check for these success messages:
```
H2 Driver loaded successfully
Database tables created/verified successfully at: /opt/render/project/data/mental_journal
```

---

## 🐛 Troubleshooting

### Issue: 404 Not Found
**Solution:** 
- Check WAR deployed as `ROOT.war`
- Verify logs: `ls -la /usr/local/tomcat/webapps/`

### Issue: Login Doesn't Work
**Solution:**
- Check database initialization in logs
- Verify BCrypt dependency loaded
- Check session cookie settings

### Issue: Database Not Persisting
**Solution:**
- Verify disk is mounted: `/opt/render/project/data`
- Check DB_PATH environment variable
- Look for file permissions errors in logs

### Issue: Slow Cold Starts (Free Tier)
**Expected:** First request after 15min inactivity takes 30-60 seconds
**Solution:** Upgrade to paid tier for always-on service

### Issue: WAR Not Found During Build
**Solution:**
```bash
# Rebuild WAR locally
mvn clean package

# Verify it exists
ls -la target/MentalJournalApp.war

# Commit and push
git add target/MentalJournalApp.war
git commit -m "Add WAR file for deployment"
git push
```

---

## 🔄 Update Workflow

### Code Changes:
```bash
# 1. Make changes to source code
# 2. Rebuild WAR
mvn clean package

# 3. Test locally (optional)
# Deploy to local Tomcat and test

# 4. Commit and push
git add .
git commit -m "Feature: Your feature description"
git push

# 5. Render auto-deploys (if enabled)
```

### Force Rebuild on Render:
1. Go to Render dashboard
2. Click your service
3. Click **"Manual Deploy"** → **"Clear build cache & deploy"**

---

## 💰 Cost Breakdown

### Free Tier:
- ✅ 750 hours/month
- ✅ 512 MB RAM
- ✅ 1 GB persistent disk
- ⚠️ Cold starts after 15min inactivity
- ⚠️ Custom domain requires payment

### Paid Tier ($7/month):
- ✅ Always-on (no cold starts)
- ✅ Custom domains
- ✅ More resources

---

## 🌟 Production Checklist

Before going live:

- [ ] WAR file built and committed
- [ ] All tests passing locally
- [ ] Environment variables configured
- [ ] Persistent disk added
- [ ] HTTPS enabled (automatic on Render)
- [ ] Custom domain configured (optional)
- [ ] Monitoring set up
- [ ] Backup strategy planned
- [ ] Error pages customized

---

## 📞 Support

### Resources:
- [Render Documentation](https://render.com/docs)
- [Tomcat Documentation](https://tomcat.apache.org/tomcat-10.1-doc/)
- [H2 Database Docs](https://h2database.com/html/main.html)

### Common Commands:
```bash
# Check WAR contents
jar -tf target/MentalJournalApp.war

# Test Docker build locally
docker build -t mental-wellness-test .
docker run -p 8080:8080 mental-wellness-test

# View Render logs
# Go to Render Dashboard → Logs tab
```

---

## 🎉 Success!

Your Mental Wellness Journal App is now live! 🍑

**Share your app:** `https://your-service-name.onrender.com`

---

*Last Updated: October 2025*

