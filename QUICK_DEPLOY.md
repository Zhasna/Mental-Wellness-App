# 🚀 Quick Deploy to Render

## ✅ Pre-Deployment Checklist

- [x] Fixed API URLs (removed `/MentalJournalApp/` prefix)
- [x] Fixed cookie security (`secure=false` for Render)
- [x] Added JSON response validation
- [x] Enhanced error logging
- [x] Updated cache-busting versions (v=5)
- [x] WAR file rebuilt

## 📦 Deploy Steps

### 1. Commit and Push
```bash
git add .
git commit -m "Fix: API endpoints and cache busting for Render"
git push
```

### 2. Monitor Render Deployment
1. Go to https://dashboard.render.com
2. Find your service: `mental-wellness-app-xg4t`
3. Watch the deployment logs
4. Look for:
   - ✅ `Build successful`
   - ✅ `H2 Driver loaded successfully`
   - ✅ `Database tables created/verified`

### 3. Test After Deployment (Wait 5-10 minutes)
```
URL: https://mental-wellness-app-xg4t.onrender.com
```

#### Test Registration:
1. Go to `/register.html`
2. Open browser DevTools (F12) → Console tab
3. Fill in form:
   - Name: Test User
   - Email: test@example.com
   - Password: testpass123
   - Confirm: testpass123
4. Click Register
5. **Check console** - should NOT see JSON parse error
6. Should see success message

#### Test Login:
1. Go to `/login.html`
2. Enter credentials
3. Should redirect to dashboard

## 🐛 If Still Having Issues

### Check Browser Console
Press F12 → Console tab and look for:

**Good signs:**
```
Sending registration request...
Registration response: {message: "User registered successfully"}
```

**Bad signs (and what they mean):**
```
Non-JSON response: <html>...
Status: 404 Not Found
→ API endpoint not found - check Render logs

Status: 500 Internal Server Error  
→ Server error - check Render logs for stack trace

CORS error
→ Need to add CORS headers to SecurityHeadersFilter
```

### Check Render Logs
1. Render Dashboard → Your Service → Logs
2. Look for:
```
=== RegisterServlet: POST /api/register ===
Register attempt - Email: test@example.com
✓ User registered successfully: test@example.com
```

**OR errors:**
```
✗ SQL Error during registration: ...
✗ General error during registration: ...
```

### Force Browser Cache Clear
1. Hard refresh: `Ctrl + Shift + R` (Windows) or `Cmd + Shift + R` (Mac)
2. Or clear cache manually:
   - Chrome: DevTools → Network tab → Disable cache (checkbox)
   - Firefox: DevTools → Network tab → Disable HTTP cache

### Force Render Rebuild
If auto-deploy didn't work:
1. Render Dashboard → Your Service
2. **Manual Deploy** → **Clear build cache & deploy**
3. Wait 5-10 minutes

## 📊 What Changed

### Before (Broken):
```javascript
// auth.js (OLD)
fetch('/MentalJournalApp/api/login', ...) // ❌ 404 Error
const data = await res.json(); // ❌ Tries to parse HTML as JSON
```

### After (Fixed):
```javascript
// auth.js (NEW)
fetch('/api/login', ...) // ✅ Correct URL
if (!contentType.includes('application/json')) { // ✅ Validates response
    console.error('Non-JSON response:', text);
    return;
}
const data = await res.json(); // ✅ Only parses if JSON
```

### Cache Busting:
```html
<!-- Before -->
<script src="js/auth.js?v=4"></script>

<!-- After -->
<script src="js/auth.js?v=5"></script>  ← Forces reload
```

## 🎯 Expected Results

### Registration Flow:
1. User fills form
2. Browser sends POST to `/api/register`
3. Server logs: `=== RegisterServlet: POST /api/register ===`
4. Database creates user
5. Server responds: `{"message":"User registered successfully"}`
6. Browser shows alert, redirects to login

### Login Flow:
1. User enters credentials
2. Browser sends POST to `/api/login`
3. Server validates password with BCrypt
4. Server creates session
5. Server responds: `{"userId":1,"name":"Test User","email":"test@example.com"}`
6. Browser stores in sessionStorage, redirects to dashboard

## 📞 Need Help?

### Get Diagnostics:
1. **Browser Console Screenshot** (F12)
2. **Network Tab** (F12 → Network → Failed request)
3. **Render Logs** (last 50 lines)

### Common Fixes:

**Issue: Still seeing old JavaScript**
```bash
# Update version to v=6 and rebuild
# Find and replace v=5 with v=6 in all HTML files
```

**Issue: 404 on /api/register**
```
Check Render logs for:
- WAR deployment successful
- Servlets registered
```

**Issue: Database errors**
```
Check environment variables:
DB_PATH=/opt/render/project/data/mental_journal
```

---

**Last Updated:** Just now  
**Status:** Ready to deploy! 🚀

