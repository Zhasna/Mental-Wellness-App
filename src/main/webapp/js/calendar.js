// Calendar page logic
document.addEventListener('DOMContentLoaded', () => {
    if (!checkAuth()) return;
    initCalendar();
});

let currentYear, currentMonth; // month: 0-11

async function initCalendar() {
    const today = new Date();
    currentYear = today.getFullYear();
    currentMonth = today.getMonth();
    attachNavHandlers();
    await renderCalendar();
}

function attachNavHandlers() {
    document.getElementById('prevMonth').addEventListener('click', async () => {
        if (currentMonth === 0) { currentMonth = 11; currentYear -= 1; } else { currentMonth -= 1; }
        await renderCalendar();
    });
    document.getElementById('nextMonth').addEventListener('click', async () => {
        if (currentMonth === 11) { currentMonth = 0; currentYear += 1; } else { currentMonth += 1; }
        await renderCalendar();
    });
}

async function renderCalendar() {
    const monthLabel = document.getElementById('monthLabel');
    const grid = document.getElementById('calendarGrid');
    const date = new Date(currentYear, currentMonth, 1);
    const monthName = date.toLocaleString('default', { month: 'long' });
    monthLabel.textContent = `${monthName} ${currentYear}`;

    // Fetch entries for this month to map moods by date (YYYY-MM-DD)
    const { moodByDate, moodsListByDate } = await fetchMoodMap(currentYear, currentMonth);

    // Build weekday header
    grid.innerHTML = '';
    const weekdays = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
    weekdays.forEach(w => {
        const el = document.createElement('div');
        el.className = 'weekday-label';
        el.textContent = w;
        grid.appendChild(el);
    });

    const firstDay = new Date(currentYear, currentMonth, 1).getDay();
    const daysInMonth = new Date(currentYear, currentMonth + 1, 0).getDate();

    // Leading blanks
    for (let i = 0; i < firstDay; i++) {
        const blank = document.createElement('div');
        grid.appendChild(blank);
    }

    // Day cells
    for (let day = 1; day <= daysInMonth; day++) {
        const cell = document.createElement('div');
        cell.className = 'day-cell';
        const dateStr = formatYmd(currentYear, currentMonth + 1, day);
        cell.dataset.date = dateStr;

        const dateEl = document.createElement('div');
        dateEl.className = 'date';
        dateEl.textContent = day;
        const moodEl = document.createElement('div');
        moodEl.className = 'mood';
        moodEl.textContent = moodByDate[dateStr] ? getMoodEmoji(moodByDate[dateStr]) : '';
        const badgesWrap = document.createElement('div');
        badgesWrap.className = 'mood-badges';
        const moods = moodsListByDate[dateStr] || (moodByDate[dateStr] ? [moodByDate[dateStr]] : []);
        if (moods.length > 0) {
            const unique = Array.from(new Set(moods.map(m => (m || '').toLowerCase())));
            unique.slice(0, 3).forEach(m => {
                const s = document.createElement('span');
                s.textContent = getMoodEmoji(m);
                badgesWrap.appendChild(s);
            });
            if (unique.length > 3) {
                const more = document.createElement('span');
                more.className = 'more';
                more.textContent = `+${unique.length - 3}`;
                badgesWrap.appendChild(more);
            }
        }

        cell.appendChild(dateEl);
        cell.appendChild(moodEl);
        cell.appendChild(badgesWrap);
        cell.addEventListener('click', () => openDrawer(dateStr));
        grid.appendChild(cell);
    }
}

async function fetchMoodMap(year, monthIndexZeroBased) {
    try {
        // Fetch all entries, then filter by month client-side for simplicity
        const res = await fetch('/MentalJournalApp/api/entries');
        if (!res.ok) return {};
        const entries = await res.json();
        const month = monthIndexZeroBased + 1; // 1-12
        const map = {}; // last mood for day (for simple usage)
        const listMap = {}; // list of moods per day
        entries.forEach(e => {
            // Exclude gratitude-tagged notes from calendar mood map
            const isGrat = String(e.content || '').trim().toLowerCase().startsWith('[gratitude]');
            if (isGrat) return;
            const d = normalizeEntryDate(e);
            if (!d) return;
            const parts = d.split('-').map(Number);
            if (parts.length < 3) return;
            const [y, m] = parts;
            if (y === year && m === month) {
                map[d] = e.mood;
                if (!listMap[d]) listMap[d] = [];
                listMap[d].push(e.mood);
            }
        });
        return { moodByDate: map, moodsListByDate: listMap };
    } catch (err) {
        console.error('Failed to load entries', err);
        return { moodByDate: {}, moodsListByDate: {} };
    }
}

async function openDrawer(dateStr) {
    const drawer = document.getElementById('entryDrawer');
    const backdrop = document.getElementById('drawerBackdrop');
    const dateEl = document.getElementById('drawerDate');
    const contentEl = document.getElementById('drawerContent');

    dateEl.textContent = new Date(dateStr).toDateString();
    contentEl.innerHTML = '<p>Loading...</p>';

    const entries = await fetchEntriesByDate(dateStr);
    contentEl.innerHTML = '';
    if (entries.length > 0) {
        entries.forEach(e => {
            const wrap = document.createElement('div');
            wrap.className = 'drawer-entry';
            const mood = document.createElement('div');
            mood.className = 'mood';
            mood.textContent = getMoodEmoji(e.mood || '');
            const text = document.createElement('div');
            text.className = 'text';
            text.textContent = e.content || '';
            wrap.appendChild(mood);
            wrap.appendChild(text);
            contentEl.appendChild(wrap);
        });
    } else {
        contentEl.innerHTML = '<p class="no-data">No entry recorded for this day.</p>';
    }

    drawer.classList.add('open');
    backdrop.classList.add('open');

    document.getElementById('closeDrawer').onclick = closeDrawer;
    backdrop.onclick = closeDrawer;
}

function closeDrawer() {
    document.getElementById('entryDrawer').classList.remove('open');
    document.getElementById('drawerBackdrop').classList.remove('open');
}

async function fetchEntriesByDate(dateStr) {
    try {
        // Assuming an endpoint exists to fetch entries for a specific date
        // If not, fetch all and filter
        const res = await fetch('/MentalJournalApp/api/entries');
        if (!res.ok) return [];
        let entries = await res.json();
        // Exclude gratitude-tagged notes from calendar day entries
        entries = entries.filter(e => !String(e.content || '').trim().toLowerCase().startsWith('[gratitude]'));
        const list = entries.filter(e => {
            const d = normalizeEntryDate(e);
            return d === dateStr;
        });
        // Optional: stable order by created_at if present
        list.sort((a,b) => {
            const ta = new Date(a.created_at || a.createdAt || a.createdAtMs || 0).getTime();
            const tb = new Date(b.created_at || b.createdAt || b.createdAtMs || 0).getTime();
            return ta - tb;
        });
        return list;
    } catch (err) {
        console.error('Failed to fetch entry for date', err);
        return [];
    }
}

function formatYmd(year, month1, day) {
    const m = String(month1).padStart(2, '0');
    const d = String(day).padStart(2, '0');
    return `${year}-${m}-${d}`;
}

function getMoodEmoji(mood) {
    if (!mood) return '';
    switch (String(mood).toLowerCase()) {
        case 'happy': return '😊';
        case 'sad': return '😢';
        case 'angry': return '😠';
        case 'anxious': return '😰';
        case 'calm': return '😌';
        case 'tired': return '😴';
        case 'neutral': return '😐';
        case 'excited': return '🤩';
        case 'frustrated': return '😤';
        case 'peaceful': return '🕊️';
        default: return '❓';
    }
}

function normalizeEntryDate(entry) {
    const raw = entry.entryDate || entry.date || entry.entry_date || '';
    if (!raw) return '';
    // If already in YYYY-MM-DD
    if (/^\d{4}-\d{2}-\d{2}/.test(raw)) {
        return raw.substring(0, 10);
    }
    // Try to parse other formats
    const d = new Date(raw);
    if (isNaN(d.getTime())) return '';
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}


