document.addEventListener('DOMContentLoaded', async () => {
    if (!checkAuth()) return;
    bindAddControls();
    await renderBox();
});

async function renderBox() {
    const grid = document.getElementById('chocoGrid');
    grid.innerHTML = '';
    const notes = await fetchGratitudes();
    notes.forEach((n, idx) => {
        const cup = document.createElement('div');
        cup.className = 'cup';
        const c = document.createElement('div');
        c.className = `choco bob ${pickChocoPairClass(idx)}`;
        c.title = (n.date || '').substring(0,10);
        c.dataset.date = (n.date || '').substring(0,10);
        c.dataset.text = n.text || n.content || '';
        c.dataset.id = n.id;
        c.innerHTML = `<div class="hint">${truncate(c.dataset.text, 28)}</div>`;
        c.addEventListener('click', () => openGratDrawer(c.dataset.id, c.dataset.date, c.dataset.text));
        cup.appendChild(c);
        grid.appendChild(cup);
    });
}

function placeToken(existing, bounds, size, minYPad, maxYJitter) {
    // naive placement with attempts
    for (let tries = 0; tries < 120; tries++) {
        const x = bounds.x + Math.random() * (bounds.w - size);
        const y = bounds.y + Math.random() * (bounds.h - size);
        const ok = existing.every(s => dist(x+size/2, y+size/2, s.cx, s.cy) > (size/2 + s.r + 6));
        if (ok) {
            const spot = { cx: x+size/2, cy: y+size/2, r: size/2 };
            existing.push(spot);
            return { x, y: y + randomBetween(0, maxYJitter) };
        }
    }
    return { x: bounds.x + Math.random() * (bounds.w - size), y: bounds.y + Math.random() * (bounds.h - size) };
}

function dist(ax, ay, bx, by) { return Math.hypot(ax-bx, ay-by); }
function randomBetween(a,b){ return a + Math.random()*(b-a); }
function clamp(v, a, b){ return Math.max(a, Math.min(b, v)); }

async function fetchGratitudes() {
    try {
        // Reuse entries API: treat entries with mood 'peaceful' and short content as gratitude? Better: dedicated endpoint if exists.
        // For now, look for entries tagged via a convention: content starting with "[gratitude]" or mood == 'peaceful'.
        const res = await fetch('/api/entries');
        if (!res.ok) return [];
        const entries = await res.json();
        // Only include entries explicitly tagged as gratitude to avoid overlap with normal journal entries
        const notes = entries.filter(e => {
            const text = (e.content || '').trim();
            const isTag = text.toLowerCase().startsWith('[gratitude]');
            return isTag;
        }).map(e => ({
            id: e.id,
            date: (e.entryDate || e.date || '').substring(0,10),
            text: (e.content || '').replace(/^\[gratitude\]\s*/i, '')
        }));
        return notes;
    } catch (e) {
        console.error('Failed to fetch gratitude notes', e);
        return [];
    }
}

function openGratDrawer(id, dateStr, text) {
    const drawer = document.getElementById('gratDrawer');
    const backdrop = document.getElementById('gratBackdrop');
    const dateRow = `<div class="drawer-date"><span>📅</span><span>${new Date(dateStr).toLocaleDateString(undefined, { year: 'numeric', month: 'long', day: 'numeric' })}</span></div>`;
    const quote = `<div class="drawer-quote">"Gratitude turns what we have into enough."</div>`;
    const actions = `
        <div class="drawer-actions">
            <button class="edit-btn" onclick="editGratitude(${id}, '${escapeForJs(text)}')">✏️ Edit</button>
            <button class="delete-btn" onclick="deleteGratitude(${id})">🗑️ Delete</button>
        </div>
    `;
    document.getElementById('gratContent').innerHTML = `<div class="drawer-entry">${escapeHtml(text)}</div>${dateRow}${quote}${actions}`;
    drawer.classList.add('open');
    backdrop.classList.add('open');
    document.getElementById('closeGrat').onclick = closeGratDrawer;
    backdrop.onclick = closeGratDrawer;
}

function escapeForJs(text) {
    return text.replace(/\\/g, '\\\\').replace(/'/g, "\\'").replace(/"/g, '\\"').replace(/\n/g, '\\n');
}

async function editGratitude(id, text) {
    closeGratDrawer();
    const input = document.getElementById('gratInput');
    const btn = document.getElementById('gratAddBtn');
    
    // Populate input with current text
    input.value = text;
    input.focus();
    
    // Change button text and style
    const originalText = btn.textContent;
    btn.textContent = 'Update';
    btn.style.background = 'linear-gradient(135deg, #FFA07A 0%, #FF8C69 100%)';
    
    // Create cancel button if it doesn't exist
    let cancelBtn = document.getElementById('cancelGratBtn');
    if (!cancelBtn) {
        cancelBtn = document.createElement('button');
        cancelBtn.id = 'cancelGratBtn';
        cancelBtn.textContent = 'Cancel';
        cancelBtn.className = 'cancel-btn';
        cancelBtn.style.cssText = 'padding: 0.9rem 1.25rem; margin-left: 0.5rem; border: 2px solid var(--accent-color); border-radius: 999px; background: transparent; color: var(--accent-color); cursor: pointer; transition: all 0.3s;';
        btn.parentNode.appendChild(cancelBtn);
    }
    
    // Cancel handler
    const resetForm = () => {
        input.value = '';
        btn.textContent = originalText;
        btn.style.background = '';
        if (cancelBtn) cancelBtn.remove();
        btn.onclick = null;
    };
    
    cancelBtn.onclick = resetForm;
    
    // Update handler
    btn.onclick = async () => {
        const updatedText = input.value.trim();
        if (!updatedText) return;
        
        try {
            const today = new Date();
            const y = today.getFullYear();
            const m = String(today.getMonth()+1).padStart(2,'0');
            const d = String(today.getDate()).padStart(2,'0');
            const payload = { 
                entryId: id.toString(), 
                date: `${y}-${m}-${d}`, 
                mood: 'peaceful', 
                content: `[gratitude] ${updatedText}` 
            };
            
            const res = await fetch('/api/entries', { 
                method: 'PUT', 
                headers: {'Content-Type': 'application/json'}, 
                body: JSON.stringify(payload) 
            });
            
            if (res.ok) {
                resetForm();
                await renderBox();
                alert('Gratitude updated successfully!');
            } else {
                alert('Failed to update gratitude');
            }
        } catch(e) { 
            console.error('Failed to update gratitude', e); 
            alert('Error updating gratitude');
        }
    };
}

async function deleteGratitude(id) {
    if (!confirm('Are you sure you want to delete this gratitude? This action cannot be undone.')) {
        return;
    }
    
    closeGratDrawer();
    
    try {
        const res = await fetch(`/api/entries?entryId=${id}`, { method: 'DELETE' });
        
        if (res.ok) {
            await renderBox();
            alert('Gratitude deleted successfully!');
        } else {
            alert('Failed to delete gratitude');
        }
    } catch(e) { 
        console.error('Failed to delete gratitude', e); 
        alert('Error deleting gratitude');
    }
}

function closeGratDrawer() {
    document.getElementById('gratDrawer').classList.remove('open');
    document.getElementById('gratBackdrop').classList.remove('open');
}

function escapeHtml(s){
    return String(s).replace(/[&<>"']/g, m => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;','\'':'&#39;'}[m]));
}

function truncate(s, n){ s = String(s||''); return s.length > n ? s.slice(0,n-1)+'…' : s; }

function bindAddControls(){
    const input = document.getElementById('gratInput');
    const btn = document.getElementById('gratAddBtn');
    if (!input || !btn) return;
    const add = async () => {
        const text = (input.value || '').trim();
        if (!text) return;
        // Persist using entries API with a gratitude tag
        const today = new Date();
        const y = today.getFullYear();
        const m = String(today.getMonth()+1).padStart(2,'0');
        const d = String(today.getDate()).padStart(2,'0');
        const payload = { date: `${y}-${m}-${d}`, mood: 'peaceful', content: `[gratitude] ${text}` };
        try {
            const res = await fetch('/api/entries', { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify(payload) });
            if (res.ok){
                input.value='';
                // Re-render with a drop-in animation for last node
                await renderBox();
                const grid = document.getElementById('chocoGrid');
                const last = grid.lastElementChild;
                if (last){ last.classList.add('choco-drop-once'); setTimeout(()=> last.classList.remove('choco-drop-once'), 600); }
            }
        } catch(e){ console.error('Failed to add gratitude', e); }
    };
    btn.addEventListener('click', add);
    input.addEventListener('keydown', (e)=>{ if (e.key==='Enter'){ e.preventDefault(); add(); } });
}

function pickChocoClass(i){
    const classes = ['dark','milk','white','accent'];
    return classes[i % classes.length];
}

function pickChocoPairClass(i){
    const pairs = ['dark','dark','milk','milk','white','white','accent','accent'];
    return pairs[i % pairs.length];
}


