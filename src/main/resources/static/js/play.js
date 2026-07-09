async function postJson(url, payload) {
  const res = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload || {})
  });
  if (!res.ok) throw new Error("Request failed");
  return res.json();
}

function escapeHtml(s) {
  return String(s)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}

function setPlayLocked(finished) {
  ["guessInput", "guessSubmit", "randomRevealBtn", "finalInput", "finalSubmit", "giveUpBtn"].forEach((id) => {
    const el = document.getElementById(id);
    if (el) el.disabled = finished;
  });
}

function renderSummary(result) {
  const solved = result.solved;
  const title = solved ? "You got it!" : "Here's the answer";
  const vibe = solved ? "summary-win" : "summary-reveal";
  document.getElementById("summary").innerHTML = `
    <div class="summary-card ${vibe}">
      <h2 class="summary-title">${title}</h2>
      <p class="summary-answer">Answer: <strong>${escapeHtml(result.correctAnswer)}</strong></p>
      <p class="summary-stats">Decoded ${result.revealPercent}% · Wrong guesses ${result.wrongGuesses} · Score ${result.score}</p>
      ${result.explanation ? `<p class="summary-explain">${escapeHtml(result.explanation)}</p>` : ""}
    </div>
  `;
}

function renderGuessChips(containerId, words, chipClass, emptyText) {
  const el = document.getElementById(containerId);
  if (!el) return;
  const list = [...(words || [])].sort((a, b) => a.localeCompare(b));
  if (!list.length) {
    el.innerHTML = `<span class="guess-history-empty muted">${emptyText}</span>`;
    return;
  }
  el.innerHTML = list.map((w) => `<span class="chip ${chipClass}">${escapeHtml(w)}</span>`).join("");
}

function renderView(view) {
  document.getElementById("code").textContent = view.code;
  document.getElementById("reveal").textContent = `${view.revealPercent}%`;
  document.getElementById("wrongCount").textContent = String(view.wrongGuessCount);
  document.getElementById("guessCount").textContent = String(view.guessedWords);
  document.getElementById("hintsUsed").textContent = String(view.hintsUsed);
  renderGuessChips("correctGuesses", view.correctGuesses, "chip-good", "None yet");
  renderGuessChips("wrongGuesses", view.wrongGuesses, "chip-bad", "None yet");
  document.getElementById("meta").textContent = view.language || "—";
  document.getElementById("finalWrap").style.display =
    view.finished ? "none" : view.canFinalGuess ? "block" : "none";
  setPlayLocked(view.finished);
}

async function initPlay(puzzleId) {
  let currentView = await postJson(`/api/game/${puzzleId}/start`);
  renderView(currentView);

  document.getElementById("guessForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    if (currentView.finished) return;
    const guess = document.getElementById("guessInput").value.trim();
    if (!guess) return;
    currentView = await postJson(`/api/game/${puzzleId}/guess`, { guess });
    document.getElementById("guessInput").value = "";
    renderView(currentView);
  });

  document.getElementById("randomRevealBtn").addEventListener("click", async () => {
    if (currentView.finished) return;
    currentView = await postJson(`/api/game/${puzzleId}/hint`, { type: "REVEAL_TOKEN" });
    renderView(currentView);
  });

  document.getElementById("giveUpBtn").addEventListener("click", async () => {
    if (currentView.finished) return;
    const ok = window.confirm("Show the answer and end this round? Your score will be 0.");
    if (!ok) return;
    const result = await postJson(`/api/game/${puzzleId}/giveup`);
    renderSummary(result);
    currentView = await postJson(`/api/game/${puzzleId}/start`);
    renderView(currentView);
  });

  document.getElementById("finalForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    if (currentView.finished) return;
    const answer = document.getElementById("finalInput").value.trim();
    if (!answer) return;
    const result = await postJson(`/api/game/${puzzleId}/final`, { answer });
    renderSummary(result);
    currentView = await postJson(`/api/game/${puzzleId}/start`);
    renderView(currentView);
  });
}
