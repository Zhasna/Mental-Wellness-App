# 🔧 Database Initialization Fix

## ❌ Root Cause Found!

The error `Table "USERS" not found (this database is empty)` means:
- Database is NOT initializing on Render
- Static block in `DBConnection.java` might not be running
- Or database files not persisting properly

## ✅ Fixes Applied:

### 1. **H2 Database URL Settings**
Added case-insensitive mode:
```java
// OLD
jdbc:h2:file:PATH;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1;MODE=MySQL

// NEW
jdbc:h2:file:PATH;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_UPPER=false;CASE_INSENSITIVE_IDENTIFIERS=TRUE
```

### 2. **Enhanced Logging**
```java
System.out.println("Initializing database at: " + DB_PATH);
System.out.println("JDBC URL: " + JDBC_URL);
```

### 3. **Startup Script**
Created `startup.sh` to ensure database directory exists:
```bash
mkdir -p /opt/render/project/data
chmod 755 /opt/render/project/data
```

### 4. **Updated Dockerfile**
Now runs startup script before Tomcat

---

## 🚀 Deploy Instructions:

```bash
# 1. Rebuild WAR
mvn clean package

# 2. Add all changes
git add .

# 3. Commit
git commit -m "Fix: Database initialization and H2 case sensitivity"

# 4. Push to GitHub
git push
```

---

## 📊 After Deployment - Check Render Logs:

Look for these messages:

### ✅ Good Logs:
```
Creating database directory...
Database directory ready at: /opt/render/project/data
H2 Driver loaded successfully
Initializing database at: /opt/render/project/data/mental_journal
JDBC URL: jdbc:h2:file:/opt/render/project/data/mental_journal...
Database tables created/verified successfully at: /opt/render/project/data/mental_journal
```

### ❌ Bad Logs:
```
Cannot initialize database - H2 driver not loaded
Permission denied: /opt/render/project/data
Database initialization error: ...
```

---

## 🧪 Test After Deployment:

### Test 1: Check API
```bash
curl https://mental-wellness-app-xg4t.onrender.com/api/login \
  -X POST \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"test123"}'
```

**Expected (user not found):**
```json
{"message":"Invalid credentials"}
```

**NOT expected:**
```json
{"message":"Database error: Table USERS not found..."}
```

### Test 2: Register
1. Go to `/register.html`
2. Fill form
3. Submit
4. Should see success

### Test 3: Login
1. Use registered credentials
2. Should redirect to dashboard

---

## 🔍 Debugging If Still Failing:

### Check Render Environment Variables:
1. Render Dashboard → Your Service → Environment
2. Verify:
   - `DB_PATH` = `/opt/render/project/data/mental_journal`

### Check Disk Mount:
1. Render Dashboard → Your Service → Disks
2. Should see:
   - Name: `mental-journal-data`
   - Mount Path: `/opt/render/project/data`
   - Size: 1 GB

### Manual Database Test:
Add this servlet to test database directly:

```java
// TestDBServlet.java
@WebServlet("/test-db")
public class TestDBServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("text/plain");
        try {
            Connection conn = DBConnection.getConnection();
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rs = meta.getTables(null, null, "%", new String[]{"TABLE"});
            
            response.getWriter().println("=== Database Tables ===");
            while (rs.next()) {
                response.getWriter().println("Table: " + rs.getString("TABLE_NAME"));
            }
            conn.close();
        } catch (Exception e) {
            response.getWriter().println("ERROR: " + e.getMessage());
            e.printStackTrace(response.getWriter());
        }
    }
}
```

Access: `https://your-app.onrender.com/test-db`

---

## 📝 Files Changed:

1. `DBConnection.java` - Added case-insensitive mode + logging
2. `Dockerfile` - Added startup script execution
3. `startup.sh` - NEW - Creates database directory
4. `LoginServlet.java` - Fixed JSON response (from before)

---

## ⏱️ Expected Timeline:

1. **Push to GitHub:** Immediate
2. **Render Build:** 3-5 minutes
3. **Render Deploy:** 2-3 minutes
4. **Total:** ~5-10 minutes

---

## 🎯 Success Criteria:

- [ ] Render logs show "Database tables created/verified"
- [ ] No "Table not found" errors
- [ ] Registration works
- [ ] Login works  
- [ ] Dashboard loads
- [ ] Data persists after restart

---

**Status:** Ready to deploy!  
**Next Step:** Run the deploy commands above

