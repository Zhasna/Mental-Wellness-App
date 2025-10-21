# 🔧 Render Deployment Troubleshooting

## ❌ Login/Register Not Working

### Step 1: Check Render Logs

1. Go to your Render dashboard
2. Click on your service
3. Click **"Logs"** tab
4. Look for these key messages:

**✅ Good logs:**
```
H2 Driver loaded successfully
Database tables created/verified successfully at: /opt/render/project/data/mental_journal
```

**❌ Bad logs:**
```
H2 Driver not found
Cannot initialize database
Database initialization error
Permission denied
```

---

### Step 2: Common Issues & Fixes

#### Issue 1: Database Path Not Writable

**Symptoms:**
- Register button doesn't respond
- Console shows "Permission denied" or "Read-only file system"

**Fix:**
The database needs to be in the mounted disk, not the container filesystem.

1. Check `render.yaml` disk configuration:
```yaml
disk:
  name: mental-journal-data
  mountPath: /opt/render/project/data  # ← Must match this
  sizeGB: 1
```

2. Check environment variable `DB_PATH`:
```
DB_PATH=/opt/render/project/data/mental_journal
```

3. **Redeploy** with "Clear build cache & deploy"

---

#### Issue 2: HTTPS Cookie Issue

**Symptoms:**
- Login succeeds but redirects back to login
- Session not persisting
- Browser console shows cookie warnings

**Fix:**
Update `web.xml` cookie configuration:

```xml
<session-config>
    <session-timeout>30</session-timeout>
    <cookie-config>
        <http-only>true</http-only>
        <secure>false</secure>  <!-- Change to false for Render -->
        <name>MWJSESSIONID</name>
    </cookie-config>
</session-config>
```

**Then rebuild:**
```bash
mvn clean package
git add .
git commit -m "Fix: Update cookie config for Render"
git push
```

---

#### Issue 3: WAR Not Deploying Correctly

**Symptoms:**
- Render build succeeds but app shows 404
- Logs show "No such file: MentalJournalApp.war"

**Fix:**

1. Verify WAR exists locally:
```bash
dir target\MentalJournalApp.war
```

2. Check it's tracked by git:
```bash
git ls-files target/MentalJournalApp.war
```

3. If not tracked, fix .gitignore:
```gitignore
/target/*
!target/MentalJournalApp.war  # ← This line is critical
```

4. Force add the WAR:
```bash
git add -f target/MentalJournalApp.war
git commit -m "Add WAR file for deployment"
git push
```

---

#### Issue 4: CORS / API Errors

**Symptoms:**
- Browser console shows "CORS policy" errors
- Network tab shows failed API calls
- 500 Internal Server Error

**Fix:**

Check browser console (F12) for exact error. Common fixes:

1. **CORS Headers** - Add to `SecurityHeadersFilter.java`:
```java
response.setHeader("Access-Control-Allow-Origin", "*");
response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
```

2. **Base URL Issue** - Ensure frontend calls use relative URLs:
```javascript
// ✅ Good
fetch('/api/login', {...})

// ❌ Bad
fetch('http://localhost:8080/api/login', {...})
```

---

#### Issue 5: H2 Driver Not Found

**Symptoms:**
- Logs show "ClassNotFoundException: org.h2.Driver"
- Database initialization fails

**Fix:**

1. Verify H2 is in `pom.xml`:
```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <version>2.2.224</version>
</dependency>
```

2. Check WAR includes H2 jar:
```bash
jar -tf target/MentalJournalApp.war | findstr h2
```

Should show:
```
WEB-INF/lib/h2-2.2.224.jar
```

3. If missing, rebuild:
```bash
mvn clean package
```

---

### Step 3: Test Locally with Docker

Before pushing to Render, test Docker build locally:

```bash
# Build Docker image
docker build -t mental-wellness-test .

# Run container
docker run -p 8080:8080 -v ${PWD}/data:/opt/render/project/data mental-wellness-test

# Test in browser
http://localhost:8080
```

---

### Step 4: Enable Debug Logging

Add logging to servlets for debugging:

**In `RegisterServlet.java`:**
```java
@Override
protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
    
    System.out.println("=== REGISTER SERVLET CALLED ===");
    
    try {
        String body = request.getReader().lines()
                .collect(Collectors.joining(System.lineSeparator()));
        
        System.out.println("Request body: " + body);
        
        // ... rest of code
        
    } catch (Exception e) {
        System.err.println("Register error: " + e.getMessage());
        e.printStackTrace();
    }
}
```

Rebuild, push, and check Render logs for these messages.

---

### Step 5: Check Environment Variables on Render

1. Go to Render dashboard → Your service
2. Click **"Environment"** tab
3. Verify these are set:

| Key | Value |
|-----|-------|
| `DB_PATH` | `/opt/render/project/data/mental_journal` |
| `JAVA_OPTS` | `-Djava.security.egd=file:/dev/./urandom -Xms256m -Xmx512m` |

4. If missing or wrong, **update and manually deploy**

---

### Step 6: Force Render Rebuild

Sometimes Render caches old builds:

1. Go to your service on Render
2. Click **"Manual Deploy"** dropdown
3. Select **"Clear build cache & deploy"**
4. Wait 5-10 minutes for fresh build

---

### Step 7: Check Browser Developer Tools

Open browser console (F12) and check:

#### Network Tab:
- Look for failed requests (red)
- Check request/response details
- Verify endpoints are `/api/register`, `/api/login`

#### Console Tab:
- Look for JavaScript errors
- Check for CORS errors
- Verify fetch requests are made

#### Application Tab:
- Check cookies are being set
- Look for `MWJSESSIONID` cookie

---

## 🧪 Quick Test Commands

### Test Register Endpoint:
```bash
curl -X POST https://your-app.onrender.com/api/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","email":"test@test.com","password":"testpass123"}'
```

**Expected:** 
```json
{"message":"Registration successful"}
```

### Test Login Endpoint:
```bash
curl -X POST https://your-app.onrender.com/api/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"testpass123"}' \
  -c cookies.txt
```

**Expected:**
```json
{"message":"Login successful","userId":1,"name":"test"}
```

---

## 📝 Checklist Before Asking for Help

- [ ] WAR file exists in `target/` directory
- [ ] WAR file is committed to git
- [ ] `.gitignore` allows `target/MentalJournalApp.war`
- [ ] `render.yaml` exists in repo root
- [ ] Disk is configured in `render.yaml`
- [ ] Environment variables set on Render
- [ ] Latest code is pushed to GitHub
- [ ] Render logs show successful database initialization
- [ ] Browser console shows no JavaScript errors
- [ ] Tested register/login with curl commands

---

## 🆘 Still Not Working?

### Get Full Diagnostics:

1. **Download Render logs:**
   - Render Dashboard → Logs → Download (icon in top right)

2. **Check WAR contents:**
   ```bash
   jar -tf target/MentalJournalApp.war > war-contents.txt
   ```

3. **Test database locally:**
   ```bash
   # Start local H2 server
   java -cp target/MentalJournalApp/WEB-INF/lib/h2-*.jar org.h2.tools.Server
   ```

4. **Verify all dependencies:**
   ```bash
   mvn dependency:tree
   ```

Share these files when asking for help:
- Render logs
- `war-contents.txt`
- Browser console errors (screenshot)
- Network tab (screenshot of failed request)

---

**Last Updated:** October 2025

