const API = '/api/admin/puzzles';

function setStatus(msg, isError) {
  const el = document.getElementById('admin-status');
  if (!el) return;
  el.textContent = msg;
  el.className = isError ? 'warn' : 'muted';
}

async function api(path, options = {}) {
  const res = await fetch(API + path, {
    headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
    credentials: 'same-origin',
    ...options,
  });
  if (res.status === 401 || res.status === 403) {
    setStatus('You must log in as an admin. Redirecting to login…', true);
    setTimeout(() => { window.location.href = '/login'; }, 1200);
    throw new Error('Unauthorized');
  }
  const text = await res.text();
  let body = null;
  if (text) {
    try {
      body = JSON.parse(text);
    } catch {
      body = { error: text };
    }
  }
  if (!res.ok) {
    const msg = body?.error || body?.message || `Request failed (${res.status})`;
    throw new Error(msg);
  }
  return body;
}

function tagsToList(raw) {
  if (!raw || !raw.trim()) return [];
  return raw.split(',').map((t) => t.trim()).filter(Boolean);
}

function tagsToString(tags) {
  if (!tags || !tags.length) return '';
  return Array.from(tags).join(', ');
}

function readForm() {
  const scheduled = document.getElementById('scheduledDate').value;
  return {
    slug: document.getElementById('slug').value,
    answer: document.getElementById('answer').value,
    language: document.getElementById('language').value,
    category: document.getElementById('category').value,
    difficulty: document.getElementById('difficulty').value,
    codeContent: document.getElementById('codeContent').value,
    shortClue: document.getElementById('shortClue').value || null,
    explanation: document.getElementById('explanation').value || null,
    scheduledDate: scheduled || null,
    tags: tagsToList(document.getElementById('tags').value),
  };
}

function resetForm() {
  document.getElementById('puzzle-form').reset();
  document.getElementById('puzzle-id').value = '';
  document.getElementById('form-title').textContent = 'Add puzzle';
  document.getElementById('btn-save').textContent = 'Save puzzle';
}

function fillForm(p) {
  document.getElementById('puzzle-id').value = p.id;
  document.getElementById('slug').value = p.slug;
  document.getElementById('answer').value = p.answer;
  document.getElementById('language').value = p.language;
  document.getElementById('category').value = p.category;
  document.getElementById('difficulty').value = p.difficulty;
  document.getElementById('shortClue').value = p.shortClue || '';
  document.getElementById('codeContent').value = p.codeContent;
  document.getElementById('explanation').value = p.explanation || '';
  document.getElementById('scheduledDate').value = p.scheduledDate || '';
  document.getElementById('tags').value = tagsToString(p.tags);
  document.getElementById('form-title').textContent = 'Edit puzzle #' + p.id;
  document.getElementById('btn-save').textContent = 'Update puzzle';
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

function renderList(puzzles) {
  const root = document.getElementById('puzzle-list');
  if (!puzzles.length) {
    root.innerHTML = '<p class="muted">No puzzles yet. Add one above.</p>';
    return;
  }
  root.innerHTML = puzzles.map((p) => {
    const date = p.scheduledDate ? `<span class="chip good">Daily: ${p.scheduledDate}</span>` : '<span class="muted">Not scheduled</span>';
    return `
      <article class="admin-puzzle-row" data-id="${p.id}">
        <div>
          <strong>#${p.id} ${escapeHtml(p.answer)}</strong>
          <span class="muted"> · ${escapeHtml(p.language)} · ${p.category} · ${p.difficulty}</span>
          <div>${date}</div>
          <p class="muted">${escapeHtml(p.shortClue || '')}</p>
        </div>
        <div class="admin-row-actions">
          <button type="button" class="btn secondary btn-edit" data-id="${p.id}">Edit</button>
          <button type="button" class="btn secondary btn-unschedule" data-id="${p.id}">Unschedule</button>
          <button type="button" class="btn secondary btn-delete" data-id="${p.id}">Delete</button>
        </div>
      </article>`;
  }).join('');

  root.querySelectorAll('.btn-edit').forEach((btn) => {
    btn.addEventListener('click', async () => {
      const p = await api('/' + btn.dataset.id);
      fillForm(p);
    });
  });
  root.querySelectorAll('.btn-delete').forEach((btn) => {
    btn.addEventListener('click', async () => {
      if (!confirm('Delete puzzle #' + btn.dataset.id + '?')) return;
      await api('/' + btn.dataset.id, { method: 'DELETE' });
      setStatus('Puzzle deleted.');
      await loadPuzzles();
      resetForm();
    });
  });
  root.querySelectorAll('.btn-unschedule').forEach((btn) => {
    btn.addEventListener('click', async () => {
      await api('/' + btn.dataset.id + '/unschedule', { method: 'POST' });
      setStatus('Schedule cleared.');
      await loadPuzzles();
    });
  });
}

function escapeHtml(s) {
  return String(s)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

async function loadPuzzles() {
  const puzzles = await api('');
  renderList(puzzles);
  setStatus(`${puzzles.length} puzzle(s) in database.`);
  return puzzles;
}

document.getElementById('puzzle-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  const id = document.getElementById('puzzle-id').value;
  const payload = readForm();
  try {
    if (id) {
      await api('/' + id, { method: 'PUT', body: JSON.stringify(payload) });
      setStatus('Puzzle updated.');
    } else {
      await api('', { method: 'POST', body: JSON.stringify(payload) });
      setStatus('Puzzle created.');
    }
    resetForm();
    await loadPuzzles();
  } catch (err) {
    setStatus(err.message, true);
  }
});

document.getElementById('btn-reset').addEventListener('click', resetForm);

document.getElementById('btn-bulk-schedule').addEventListener('click', async () => {
  const startDate = document.getElementById('schedule-start').value;
  const raw = document.getElementById('schedule-ids').value;
  const puzzleIds = raw.split(',').map((s) => s.trim()).filter(Boolean).map(Number);
  if (!startDate || !puzzleIds.length) {
    setStatus('Pick a start date and at least one puzzle ID.', true);
    return;
  }
  try {
    const updated = await api('/schedule', {
      method: 'POST',
      body: JSON.stringify({ startDate, puzzleIds }),
    });
    setStatus(`Scheduled ${updated.length} puzzle(s) from ${startDate}.`);
    await loadPuzzles();
  } catch (err) {
    setStatus(err.message, true);
  }
});

loadPuzzles().catch((err) => setStatus(err.message, true));
