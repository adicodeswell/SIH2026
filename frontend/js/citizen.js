/**
 * MahaSetu — Citizen Portal Logic
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
    SUBMITTED: 'badge-submitted', IN_PROGRESS: 'badge-in-progress',
    PENDING_VERIFICATION: 'badge-pending', PENDING_OFFICER_REVIEW: 'badge-pending',
    PENDING_REVIEW: 'badge-pending', APPROVED: 'badge-approved',
    REJECTED: 'badge-rejected', CONSENT_DENIED: 'badge-consent-denied',
    FAILED: 'badge-failed', COMPLETED: 'badge-completed',
    DRAFT: 'badge-draft', CANCELLED: 'badge-cancelled',
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
  const session = Auth.loginAs('citizen');
  currentUser = session.user;
  updateNavUser();
  toast(`Welcome, ${currentUser.name}!`, 'success');
  showPage('dashboard');
  await loadDashboard();
}

// ── Dashboard ─────────────────────────────────────────────
let myApplications = [];

async function loadDashboard() {
  await loadApplications();
  renderDashboardStats();
}

function renderDashboardStats() {
  const total    = myApplications.length;
  const pending  = myApplications.filter(a => ['SUBMITTED','IN_PROGRESS','PENDING_VERIFICATION','PENDING_OFFICER_REVIEW','PENDING_REVIEW'].includes(a.status)).length;
  const approved = myApplications.filter(a => a.status === 'APPROVED').length;
  const rejected = myApplications.filter(a => a.status === 'REJECTED').length;
  document.getElementById('stat-total').textContent    = total;
  document.getElementById('stat-pending').textContent  = pending;
  document.getElementById('stat-approved').textContent = approved;
  document.getElementById('stat-rejected').textContent = rejected;
  renderRecentApplications();
}

function renderRecentApplications() {
  const list = document.getElementById('recent-apps');
  if (!myApplications.length) {
    list.innerHTML = `<div class="empty-state"><div class="empty-icon">📂</div><p>No applications yet. Browse the service catalog to get started.</p></div>`;
    return;
  }
  list.innerHTML = myApplications.slice().reverse().slice(0, 5).map(app => `
    <div class="flex-between" style="padding:14px 0;border-bottom:1px solid var(--border);">
      <div>
        <div style="font-weight:600;font-size:0.9rem;">${app.applicationNumber}</div>
        <div style="font-size:0.78rem;color:var(--text-muted);">${app.serviceCode} • ${fmtDate(app.submittedAt)}</div>
      </div>
      <div style="display:flex;align-items:center;gap:10px;">
        ${statusBadge(app.status)}
        <button class="btn btn-secondary btn-sm" onclick="openTimeline('${app.applicationNumber}')">Timeline</button>
      </div>
    </div>
  `).join('');
}

// ── Service Catalog ──────────────────────────────────────
let serviceList = [];

async function loadServiceCatalog() {
  const grid = document.getElementById('service-grid');
  grid.innerHTML = `<div class="flex-center" style="padding:40px;gap:12px;"><div class="spinner"></div> Loading services…</div>`;
  try {
    serviceList = await API.getServices();
    if (!serviceList || !serviceList.length) {
      grid.innerHTML = `<div class="empty-state"><div class="empty-icon">📋</div><p>No services found.</p></div>`;
      return;
    }
    grid.innerHTML = serviceList.map(s => `
      <div class="card">
        <div style="display:flex;align-items:flex-start;justify-content:space-between;margin-bottom:12px;">
          <div>
            <h3 style="margin-bottom:4px;">${s.serviceName}</h3>
            <span class="chip">${s.departmentName || s.departmentCode}</span>
          </div>
          <span class="badge ${s.active ? 'badge-approved' : 'badge-failed'}">${s.active ? 'ACTIVE' : 'INACTIVE'}</span>
        </div>
        <p style="color:var(--text-secondary);font-size:0.85rem;margin-bottom:18px;">${s.description || ''}</p>
        <button class="btn btn-primary btn-sm" onclick="openApplyModal('${s.serviceCode}', '${s.serviceName.replace(/'/g,"\\'")}')">
          Apply Now →
        </button>
      </div>
    `).join('');
  } catch(e) {
    grid.innerHTML = `<div class="empty-state"><div class="empty-icon">⚠️</div><p>Could not load services. ${e.message || ''}</p></div>`;
  }
}

// ── Apply Modal ──────────────────────────────────────────
function openApplyModal(serviceCode, serviceName) {
  document.getElementById('apply-service-code').value   = serviceCode;
  document.getElementById('apply-service-name').textContent = serviceName;
  document.getElementById('apply-citizen-id').value     = currentUser?.citizenId || currentUser?.sub || '';
  document.getElementById('apply-modal').classList.add('open');
}

function closeApplyModal() {
  document.getElementById('apply-modal').classList.remove('open');
}

async function submitApplication() {
  const citizenId   = document.getElementById('apply-citizen-id').value.trim();
  const serviceCode = document.getElementById('apply-service-code').value;
  const btn = document.getElementById('apply-submit-btn');
  if (!citizenId) { toast('Please enter your Citizen ID.', 'warn'); return; }

  btn.disabled = true;
  btn.innerHTML = '<div class="spinner"></div> Submitting…';
  try {
    const app = await API.createApplication(citizenId, serviceCode);
    myApplications.push(app);
    closeApplyModal();
    toast(`Application ${app.applicationNumber} submitted successfully!`, 'success');
    renderDashboardStats();
    showPage('my-applications');
    await loadApplications();
  } catch(e) {
    toast(`Submission failed: ${e.message || 'Unknown error'}`, 'error');
  } finally {
    btn.disabled = false;
    btn.innerHTML = 'Submit Application';
  }
}

// ── My Applications ──────────────────────────────────────
async function loadApplications() {
  const tbody = document.getElementById('apps-tbody');
  tbody.innerHTML = `<tr><td colspan="5" style="text-align:center;padding:30px;"><div class="spinner"></div></td></tr>`;
  try {
    const citizenId = currentUser?.citizenId || currentUser?.sub || '';
    myApplications = await API.getApplications(citizenId) || [];
    renderApplicationsTable();
  } catch(e) {
    tbody.innerHTML = `<tr><td colspan="5" style="text-align:center;color:var(--crimson);padding:20px;">Failed to load: ${e.message}</td></tr>`;
  }
}

function renderApplicationsTable() {
  const tbody = document.getElementById('apps-tbody');
  if (!myApplications.length) {
    tbody.innerHTML = `<tr><td colspan="5"><div class="empty-state"><div class="empty-icon">📂</div><p>No applications yet.</p></div></td></tr>`;
    return;
  }
  tbody.innerHTML = myApplications.slice().reverse().map(app => `
    <tr>
      <td style="font-weight:600;">${app.applicationNumber}</td>
      <td>${app.serviceCode}</td>
      <td>${statusBadge(app.status)}</td>
      <td style="color:var(--text-muted);">${fmtDate(app.submittedAt)}</td>
      <td>
        <button class="btn btn-secondary btn-sm" onclick="openTimeline('${app.applicationNumber}')">Timeline</button>
      </td>
    </tr>
  `).join('');
}

// ── Timeline Modal ────────────────────────────────────────
async function openTimeline(appNumber) {
  document.getElementById('timeline-app-num').textContent = appNumber;
  const container = document.getElementById('timeline-content');
  container.innerHTML = `<div class="flex-center" style="padding:30px;"><div class="spinner"></div></div>`;
  document.getElementById('timeline-modal').classList.add('open');
  try {
    const app      = await API.getApplication(appNumber) || myApplications.find(a => a.applicationNumber === appNumber);
    const timeline = await API.getTimeline(appNumber);
    container.innerHTML = `
      <div style="margin-bottom:16px;" class="flex-between">
        <div>
          <div style="font-size:0.8rem;color:var(--text-muted);">Service</div>
          <div style="font-weight:600;">${app?.serviceCode || '—'}</div>
        </div>
        <div>${statusBadge(app?.status || 'UNKNOWN')}</div>
      </div>
      <div class="divider"></div>
      ${timeline && timeline.length
        ? `<div class="timeline">${timeline.map(e => `
            <div class="timeline-item">
              <div class="timeline-time">${fmtDate(e.occurredAt)}</div>
              <div class="timeline-event">${e.eventType.replace(/_/g,' ')}</div>
              ${e.description ? `<div class="timeline-desc">${e.description}</div>` : ''}
            </div>`).join('')}
           </div>`
        : `<div class="empty-state"><div class="empty-icon">📋</div><p>No timeline events yet.</p></div>`
      }
    `;
  } catch(e) {
    container.innerHTML = `<div class="empty-state"><div class="empty-icon">⚠️</div><p>Could not load timeline.</p></div>`;
  }
}

function closeTimeline() {
  document.getElementById('timeline-modal').classList.remove('open');
}

// ── Consent Management ────────────────────────────────────
let consentList = [];

async function loadConsents() {
  const list = document.getElementById('consent-list');
  list.innerHTML = `<div class="flex-center" style="padding:30px;"><div class="spinner"></div></div>`;
  try {
    consentList = await API.getConsents() || [];
    renderConsentList();
  } catch(e) {
    list.innerHTML = `<div class="empty-state"><div class="empty-icon">⚠️</div><p>Could not load consents.</p></div>`;
  }
}

function renderConsentList() {
  const list = document.getElementById('consent-list');
  if (!consentList.length) {
    list.innerHTML = `<div class="empty-state"><div class="empty-icon">🤝</div><p>No active consents. Grant consent below to allow departments to access your data.</p></div>`;
    return;
  }
  list.innerHTML = consentList.map(c => `
    <div class="card card-sm flex-between" style="margin-bottom:12px;">
      <div>
        <div style="font-weight:600;font-size:0.9rem;">${c.dataScope} — ${c.purpose}</div>
        <div style="font-size:0.78rem;color:var(--text-muted);">${c.requestingDepartmentId || c.departmentId || '—'} • ${fmtDate(c.createdAt)}</div>
      </div>
      <div style="display:flex;align-items:center;gap:8px;">
        <span class="badge ${c.status === 'ACTIVE' ? 'badge-approved' : 'badge-rejected'}">${c.status}</span>
        ${c.status === 'ACTIVE'
          ? `<button class="btn btn-danger btn-sm" onclick="revokeConsent('${c.id}')">Revoke</button>`
          : ''}
      </div>
    </div>
  `).join('');
}

async function grantConsentSubmit() {
  const scope  = document.getElementById('consent-scope').value;
  const purpose= document.getElementById('consent-purpose').value.trim();
  const deptId = document.getElementById('consent-dept').value.trim();
  if (!purpose || !deptId) { toast('Please fill all fields.', 'warn'); return; }
  const btn = document.getElementById('consent-grant-btn');
  btn.disabled = true;
  try {
    await API.grantConsent(scope, purpose, deptId);
    toast('Consent granted successfully.', 'success');
    document.getElementById('consent-purpose').value = '';
    document.getElementById('consent-dept').value = '';
    await loadConsents();
  } catch(e) {
    toast(`Failed to grant consent: ${e.message}`, 'error');
  } finally {
    btn.disabled = false;
  }
}

async function revokeConsent(id) {
  try {
    await API.revokeConsent(id);
    toast('Consent revoked.', 'info');
    await loadConsents();
  } catch(e) {
    toast(`Failed to revoke: ${e.message}`, 'error');
  }
}

// ── Status polling (demo mode) ────────────────────────────
window.addEventListener('app:statusChanged', async (e) => {
  const { appNumber, status } = e.detail;
  const app = myApplications.find(a => a.applicationNumber === appNumber);
  if (app) {
    app.status = status;
    renderApplicationsTable();
    renderDashboardStats();
    toast(`Application ${appNumber} status → ${status.replace(/_/g,' ')}`, 'info');
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
      if (el.dataset.page === 'catalog')         await loadServiceCatalog();
      if (el.dataset.page === 'my-applications') await loadApplications();
      if (el.dataset.page === 'consents')        await loadConsents();
    });
  });
});
