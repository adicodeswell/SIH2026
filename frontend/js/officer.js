/**
 * MahaSetu — Officer Portal Logic
 */

// ── Toast notifications ──────────────────────────────────
function toast(msg, type = 'info') {
  const icons = { success: '✅', error: '❌', info: 'ℹ️', warn: '⚠️' };
  const container = document.getElementById('toast-container');
  const el = document.createElement('div');
  el.className = `toast toast-${type}`;
  el.innerHTML = `<span>${icons[type] || '•'}</span><span>${msg}</span>`;
  container.appendChild(el);
  setTimeout(() => el.remove(), 4500);
}

// ── Status → Badge class map ─────────────────────────────
function statusBadge(status) {
  const map = {
    PENDING: 'badge-pending', CLAIMED: 'badge-in-progress',
    COMPLETED: 'badge-completed'
  };
  const cls = map[status] || 'badge-draft';
  const label = (status || 'UNKNOWN').replace(/_/g, ' ');
  return `<span class="badge ${cls}">${label}</span>`;
}

// ── Format date ──────────────────────────────────────────
function fmtDate(d) {
  if (!d) return '—';
  return new Date(d).toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'short' });
}

// ── SPA page navigation ──────────────────────────────────
let currentPage = null;
function showPage(id) {
  document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
  document.querySelectorAll('.nav-link').forEach(l => l.classList.remove('active'));
  const page = document.getElementById(`page-${id}`);
  if (page) page.classList.add('active');
  const nav = document.querySelector(`[data-page="${id}"]`);
  if (nav) nav.classList.add('active');
  currentPage = id;
}

// ── Auth state ───────────────────────────────────────────
let currentUser = null;

function updateNavUser() {
  const el = document.getElementById('nav-user');
  if (currentUser) {
    el.innerHTML = `
      <div class="nav-avatar">${currentUser.name[0]}</div>
      <span>${currentUser.name}</span>
      <button class="btn btn-secondary btn-sm" onclick="doLogout()">Logout</button>
    `;
  } else {
    el.innerHTML = '';
  }
}

function doLogout() {
  Auth.logout();
  currentUser = null;
  updateNavUser();
  showPage('login');
  toast('Logged out successfully.', 'info');
}

// ── Login ────────────────────────────────────────────────
async function doLogin() {
  const session = Auth.loginAs('officer');
  currentUser = session.user;
  updateNavUser();
  toast(`Welcome, ${currentUser.name}!`, 'success');
  showPage('dashboard');
  await loadDashboard();
}

// ── Dashboard ─────────────────────────────────────────────
let pendingTasks = [];

async function loadDashboard() {
  const tbody = document.getElementById('tasks-tbody');
  tbody.innerHTML = `<tr><td colspan="5" style="text-align:center;padding:30px;"><div class="spinner"></div></td></tr>`;
  try {
    pendingTasks = await API.getPendingReviews() || [];
    renderDashboardStats();
    renderTasksTable();
  } catch(e) {
    tbody.innerHTML = `<tr><td colspan="5" style="text-align:center;color:var(--crimson);padding:20px;">Failed to load: ${e.message}</td></tr>`;
  }
}

function renderDashboardStats() {
  const total = pendingTasks.length;
  const myTasks = pendingTasks.filter(t => t.assignee === currentUser.sub).length;
  document.getElementById('stat-total-pending').textContent = total;
  document.getElementById('stat-my-tasks').textContent = myTasks;
}

function renderTasksTable() {
  const tbody = document.getElementById('tasks-tbody');
  if (!pendingTasks.length) {
    tbody.innerHTML = `<tr><td colspan="5"><div class="empty-state"><div class="empty-icon">📂</div><p>No pending review tasks.</p></div></td></tr>`;
    return;
  }
  tbody.innerHTML = pendingTasks.slice().reverse().map(task => `
    <tr>
      <td style="font-weight:600;">${task.applicationNumber}</td>
      <td>${task.serviceCode}</td>
      <td>${statusBadge(task.state)}</td>
      <td style="color:var(--text-muted);">${fmtDate(task.createdAt)}</td>
      <td>
        <button class="btn btn-primary btn-sm" onclick="openReviewModal('${task.taskId}')">Review</button>
      </td>
    </tr>
  `).join('');
}

// ── Review Modal ──────────────────────────────────────────
let currentTask = null;

async function openReviewModal(taskId) {
  currentTask = pendingTasks.find(t => t.taskId === taskId);
  if (!currentTask) return;

  document.getElementById('review-app-num').textContent = currentTask.applicationNumber;
  document.getElementById('review-service-code').textContent = currentTask.serviceCode;
  document.getElementById('review-citizen-id').textContent = currentTask.citizenId || '—';
  
  updateReviewActions();
  document.getElementById('review-modal').classList.add('open');
}

function closeReviewModal() {
  document.getElementById('review-modal').classList.remove('open');
  currentTask = null;
}

function updateReviewActions() {
  const container = document.getElementById('review-actions');
  
  if (currentTask.state === 'PENDING') {
    container.innerHTML = `
      <p style="margin-bottom:12px;font-size:0.9rem;color:var(--text-secondary);">This task is unassigned. Claim it to proceed.</p>
      <button class="btn btn-primary" onclick="claimCurrentTask()">Claim Task</button>
    `;
  } else if (currentTask.state === 'CLAIMED' && currentTask.assignee === currentUser.sub) {
    container.innerHTML = `
      <div style="margin-bottom:16px;">
        <label class="form-label">Decision Reason</label>
        <textarea id="decision-reason" class="form-control" placeholder="Required for rejection, optional for approval"></textarea>
      </div>
      <div class="flex" style="gap:10px;">
        <button class="btn btn-success" style="flex:1;" onclick="submitTaskDecision('APPROVE')">Approve</button>
        <button class="btn btn-danger" style="flex:1;" onclick="submitTaskDecision('REJECT')">Reject</button>
      </div>
      <div style="margin-top:16px;text-align:center;">
        <button class="btn btn-secondary btn-sm" onclick="unclaimCurrentTask()">Unclaim Task</button>
      </div>
    `;
  } else if (currentTask.state === 'CLAIMED') {
     container.innerHTML = `
      <div class="empty-state">
        <p>This task is currently claimed by another officer (${currentTask.assignee}).</p>
      </div>
    `;
  }
}

async function claimCurrentTask() {
  if (!currentTask) return;
  try {
    const updated = await API.claimTask(currentTask.taskId);
    currentTask = updated || currentTask; // Handle demo mock where it updates in place
    toast('Task claimed successfully.', 'success');
    updateReviewActions();
    loadDashboard(); // Refresh background
  } catch(e) {
    toast(`Failed to claim task: ${e.message}`, 'error');
  }
}

async function unclaimCurrentTask() {
  if (!currentTask) return;
  try {
    const updated = await API.unclaimTask(currentTask.taskId);
    currentTask = updated || currentTask;
    toast('Task unclaimed.', 'info');
    updateReviewActions();
    loadDashboard();
  } catch(e) {
    toast(`Failed to unclaim task: ${e.message}`, 'error');
  }
}

async function submitTaskDecision(decision) {
  if (!currentTask) return;
  const reason = document.getElementById('decision-reason').value.trim();
  
  if (decision === 'REJECT' && !reason) {
    toast('A reason is required when rejecting an application.', 'warn');
    return;
  }

  try {
    await API.submitDecision(currentTask.taskId, decision, reason);
    toast(`Application ${decision.toLowerCase()}d successfully.`, 'success');
    closeReviewModal();
    loadDashboard();
  } catch(e) {
    toast(`Failed to submit decision: ${e.message}`, 'error');
  }
}

// ── Demo Events ──────────────────────────────────────────
window.addEventListener('officer:newTask', () => {
    if (currentPage === 'dashboard') {
        loadDashboard();
        toast('New review task available.', 'info');
    }
});


// ── Init ─────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
  showPage('login');

  // Nav links
  document.querySelectorAll('[data-page]').forEach(el => {
    el.addEventListener('click', async (e) => {
      e.preventDefault();
      if (!Auth.isLoggedIn() && el.dataset.page !== 'login') {
        toast('Please log in first.', 'warn'); return;
      }
      showPage(el.dataset.page);
      if (el.dataset.page === 'dashboard') await loadDashboard();
    });
  });
});
