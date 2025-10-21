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
        c.innerHTML = `<div class="hint">${truncate(c.dataset.text, 28)}</div>`;
        c.addEventListener('click', () => openGratDrawer(c.dataset.date, c.dataset.text));
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
        const res = await fetch('/MentalJournalApp/api/entries');
        if (!res.ok) return [];
        const entries = await res.json();
        // Only include entries explicitly tagged as gratitude to avoid overlap with normal journal entries
        const notes = entries.filter(e => {
            const text = (e.content || '').trim();
            const isTag = text.toLowerCase().startsWith('[gratitude]');
            return isTag;
        }).map(e => ({
            date: (e.entryDate || e.date || '').substring(0,10),
            text: (e.content || '').replace(/^\[gratitude\]\s*/i, '')
        }));
        return notes;
    } catch (e) {
        console.error('Failed to fetch gratitude notes', e);
        return [];
    }
}

function openGratDrawer(dateStr, text) {
    const drawer = document.getElementById('gratDrawer');
    const backdrop = document.getElementById('gratBackdrop');
    const dateRow = `<div class="drawer-date"><span>📅</span><span>${new Date(dateStr).toLocaleDateString(undefined, { year: 'numeric', month: 'long', day: 'numeric' })}</span></div>`;
    const quote = `<div class="drawer-quote">"Gratitude turns what we have into enough."</div>`;
    document.getElementById('gratContent').innerHTML = `<div class="drawer-entry">${escapeHtml(text)}</div>${dateRow}${quote}`;
    drawer.classList.add('open');
    backdrop.classList.add('open');
    document.getElementById('closeGrat').onclick = closeGratDrawer;
    backdrop.onclick = closeGratDrawer;
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
            const res = await fetch('/MentalJournalApp/api/entries', { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify(payload) });
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


