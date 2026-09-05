/**
 * MahaSetu API Client
 * ────────────────────
 * Wraps fetch() with:
 *   - Auth header injection
 *   - Demo-mode mock responses (when ?demo=true or backend unreachable)
 *   - Unified error handling
 */

const API = (() => {
  const APP_BASE  = 'http://localhost:8081';
  const WF_BASE   = 'http://localhost:8083';

  // Check if running in demo mode (no backend)
  const isDemoMode = () =>
    new URLSearchParams(window.location.search).get('demo') === 'true';

  // ── HTTP Helper ──────────────────────────────────────────
  async function request(baseUrl, path, options = {}) {
    const token = window.Auth && window.Auth.getToken();
    const headers = {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(options.headers || {}),
    };
    const url = `${baseUrl}${path}`;
    try {
      const res = await fetch(url, { ...options, headers });
      if (!res.ok) {
        let errBody = {};
        try { errBody = await res.json(); } catch {}
        throw { status: res.status, message: errBody.message || res.statusText, body: errBody };
      }
      if (res.status === 204) return null;
      return res.json();
    } catch (err) {
      if (err.status) throw err;
      // Network error — fall through to demo mock if demo mode
      if (isDemoMode()) return null; // let caller handle
      throw err;
    }
  }

  const appGet    = (path) => request(APP_BASE, path, { method: 'GET' });
  const appPost   = (path, body) => request(APP_BASE, path, { method: 'POST', body: JSON.stringify(body) });
  const appPatch  = (path, body) => request(APP_BASE, path, { method: 'PATCH', body: JSON.stringify(body) });
  const wfGet     = (path) => request(WF_BASE, path, { method: 'GET' });
  const wfPost    = (path, body) => request(WF_BASE, path, { method: 'POST', body: body ? JSON.stringify(body) : undefined });

  // ── Demo Mock Data ───────────────────────────────────────
  const MOCK = {
    services: [
      { serviceCode: 'EDU-SCHOLARSHIP-001', serviceName: 'State Merit Scholarship', description: 'Financial assistance for meritorious students based on board results.', departmentCode: 'DEPT-EDU', departmentName: 'Department of Education', active: true },
      { serviceCode: 'EMP-SKILL-CERT-001',  serviceName: 'Skill Development Certificate', description: 'Govt-recognized certificate for vocational training programs.', departmentCode: 'DEPT-EMP', departmentName: 'Department of Employment', active: true },
      { serviceCode: 'REV-INCOME-CERT-001', serviceName: 'Income Certificate', description: 'Official document certifying annual family income for scheme eligibility.', departmentCode: 'DEPT-REV', departmentName: 'Department of Revenue', active: true },
      { serviceCode: 'SOC-DISABILITY-CERT-001', serviceName: 'Disability Certificate', description: 'Certificate for persons with disabilities for welfare scheme benefits.', departmentCode: 'DEPT-SOC', departmentName: 'Department of Social Welfare', active: true },
      { serviceCode: 'HEALTH-CARD-001',     serviceName: 'Ayushman Maharashtra Health Card', description: 'State health insurance covering ₹5 lakh per family at empanelled hospitals.', departmentCode: 'DEPT-HEALTH', departmentName: 'Department of Health', active: true },
    ],
    applications: [],
    consents: [],
    officerTasks: [],
    _nextAppNum: 1001,
    _nextConsentId: 1,
  };

  // ── Public API — Services ────────────────────────────────
  async function getServices() {
    if (isDemoMode()) return MOCK.services;
    try { return await appGet('/api/v1/services'); }
    catch { return MOCK.services; }
  }

  // ── Public API — Applications ────────────────────────────
  async function createApplication(citizenId, serviceCode) {
    if (isDemoMode()) {
      const app = {
        applicationNumber: `APP-MH-2026-${MOCK._nextAppNum++}`,
        status: 'SUBMITTED',
        citizenId,
        serviceCode,
        submittedAt: new Date().toISOString(),
      };
      MOCK.applications.push(app);
      // Simulate progression
      simulateWorkflow(app);
      return app;
    }
    return appPost('/api/v1/applications', { citizenId, serviceCode });
  }

  async function getApplications(citizenId) {
    if (isDemoMode()) return MOCK.applications.filter(a => a.citizenId === citizenId);
    // Backend doesn't have filter-by-citizen endpoint on collection yet; fetch known IDs
    try { return await appGet(`/api/v1/applications?citizenId=${citizenId}`); }
    catch { return MOCK.applications.filter(a => a.citizenId === citizenId); }
  }

  async function getApplication(appNumber) {
    if (isDemoMode()) return MOCK.applications.find(a => a.applicationNumber === appNumber) || null;
    return appGet(`/api/v1/applications/${appNumber}`);
  }

  async function getTimeline(appNumber) {
    if (isDemoMode()) {
      const app = MOCK.applications.find(a => a.applicationNumber === appNumber);
      if (!app) return [];
      return (app._timeline || []);
    }
    return appGet(`/api/v1/applications/${appNumber}/timeline`);
  }

  // ── Public API — Consents ────────────────────────────────
  async function getConsents() {
    if (isDemoMode()) return MOCK.consents;
    try { return await wfGet('/api/v1/consents'); }
    catch { return MOCK.consents; }
  }

  async function grantConsent(dataScope, purpose, requestingDepartmentId) {
    if (isDemoMode()) {
      const c = { id: `demo-consent-${MOCK._nextConsentId++}`, citizenId: window.Auth.getUser()?.sub, dataScope, purpose, requestingDepartmentId, status: 'ACTIVE', createdAt: new Date().toISOString() };
      MOCK.consents.push(c);
      return c;
    }
    return wfPost('/api/v1/consents', { dataScope, purpose, requestingDepartmentId });
  }

  async function revokeConsent(consentId) {
    if (isDemoMode()) {
      const c = MOCK.consents.find(x => x.id === consentId);
      if (c) c.status = 'REVOKED';
      return null;
    }
    return wfPost(`/api/v1/consents/${consentId}/revoke`);
  }

  // ── Public API — Officer Reviews ─────────────────────────
  async function getPendingReviews() {
    if (isDemoMode()) return MOCK.officerTasks.filter(t => t.state !== 'COMPLETED');
    try { return await wfGet('/api/v1/officer/reviews'); }
    catch { return MOCK.officerTasks.filter(t => t.state !== 'COMPLETED'); }
  }

  async function claimTask(taskId) {
    if (isDemoMode()) {
      const t = MOCK.officerTasks.find(x => x.taskId === taskId);
      if (t) { t.assignee = window.Auth.getUser()?.sub; t.state = 'CLAIMED'; }
      return t;
    }
    return wfPost(`/api/v1/officer/reviews/${taskId}/claim`);
  }

  async function unclaimTask(taskId) {
    if (isDemoMode()) {
      const t = MOCK.officerTasks.find(x => x.taskId === taskId);
      if (t) { t.assignee = null; t.state = 'PENDING'; }
      return t;
    }
    return wfPost(`/api/v1/officer/reviews/${taskId}/unclaim`);
  }

  async function submitDecision(taskId, decision, reason) {
    if (isDemoMode()) {
      const t = MOCK.officerTasks.find(x => x.taskId === taskId);
      if (t) {
        t.state = 'COMPLETED'; t.decision = decision; t.reason = reason;
        // Update linked application
        const app = MOCK.applications.find(a => a.applicationNumber === t.applicationNumber);
        if (app) {
          app.status = decision === 'APPROVE' ? 'APPROVED' : 'REJECTED';
          app._timeline = app._timeline || [];
          app._timeline.push({ eventType: `OFFICER_${decision}D`, description: reason || `Application ${decision.toLowerCase()}d by officer.`, occurredAt: new Date().toISOString() });
        }
      }
      return t;
    }
    return wfPost(`/api/v1/officer/reviews/${taskId}/decision`, { decision, reason });
  }

  // ── Demo Workflow Simulation ─────────────────────────────
  function simulateWorkflow(app) {
    app._timeline = [{ eventType: 'SUBMITTED', description: 'Application submitted by citizen.', occurredAt: new Date().toISOString() }];

    const progress = (status, event, desc, delay) => {
      setTimeout(() => {
        app.status = status;
        app._timeline.push({ eventType: event, description: desc, occurredAt: new Date().toISOString() });
        window.dispatchEvent(new CustomEvent('app:statusChanged', { detail: { appNumber: app.applicationNumber, status } }));
      }, delay);
    };

    progress('IN_PROGRESS',          'WORKFLOW_STARTED',       'Automated verification initiated.',          2000);
    progress('PENDING_VERIFICATION',  'CONSENT_VERIFIED',       'Citizen consent verified successfully.',     5000);
    progress('PENDING_OFFICER_REVIEW','OFFICER_REVIEW_PENDING', 'Application queued for officer review.', 9000);

    // Create officer task in demo store
    setTimeout(() => {
      MOCK.officerTasks.push({
        taskId: `TASK-${app.applicationNumber}`,
        applicationNumber: app.applicationNumber,
        citizenId: app.citizenId,
        serviceCode: app.serviceCode,
        state: 'PENDING',
        assignee: null,
        createdAt: new Date().toISOString(),
      });
      window.dispatchEvent(new CustomEvent('officer:newTask', {}));
    }, 9500);
  }

  return {
    isDemoMode,
    getServices,
    createApplication, getApplications, getApplication, getTimeline,
    getConsents, grantConsent, revokeConsent,
    getPendingReviews, claimTask, unclaimTask, submitDecision,
    MOCK,
  };
})();

window.API = API;
