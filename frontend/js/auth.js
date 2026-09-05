/**
 * MahaSetu Auth Module (Demo Mode)
 * ─────────────────────────────────
 * In real production this would use Keycloak PKCE.
 * For the SIH demo, we generate a compact signed-looking token that carries
 * the required role claims so the frontend can demonstrate the full flow.
 *
 * The token payload is base64url-encoded and the backend security tests run
 * against WireMock/mock auth anyway, so this is strictly for UI demo.
 */

const Auth = (() => {
  const STORAGE_KEY = 'mahasetu_demo_auth';

  // Demo users
  const DEMO_USERS = {
    citizen: {
      sub: 'citizen-demo-001',
      name: 'Ramesh Patil',
      email: 'ramesh.patil@citizen.demo',
      citizenId: 'MH-CIT-2026-001',
      roles: ['ROLE_CITIZEN'],
      realm_access: { roles: ['CITIZEN'] },
    },
    officer: {
      sub: 'officer-demo-001',
      name: 'Priya Deshmukh',
      email: 'priya.deshmukh@mahasetu.gov.in',
      roles: ['ROLE_OFFICER'],
      realm_access: { roles: ['OFFICER'] },
    },
  };

  /** Build a demo JWT-like token (header.payload.fakesig) */
  function buildDemoToken(userObj) {
    const header = { alg: 'RS256', typ: 'JWT' };
    const now = Math.floor(Date.now() / 1000);
    const payload = {
      ...userObj,
      iss: 'http://localhost:8080/realms/mahasetu',
      aud: 'application-service',
      iat: now,
      exp: now + 3600,
    };
    const b64 = (obj) =>
      btoa(JSON.stringify(obj))
        .replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '');
    return `${b64(header)}.${b64(payload)}.demo-signature`;
  }

  function loginAs(role) {
    const user = DEMO_USERS[role];
    if (!user) throw new Error('Unknown demo role: ' + role);
    const token = buildDemoToken(user);
    const session = { token, user, role };
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify(session));
    return session;
  }

  function getSession() {
    const raw = sessionStorage.getItem(STORAGE_KEY);
    return raw ? JSON.parse(raw) : null;
  }

  function getToken() {
    const s = getSession();
    return s ? s.token : null;
  }

  function getUser() {
    const s = getSession();
    return s ? s.user : null;
  }

  function isLoggedIn() {
    return !!getSession();
  }

  function hasRole(role) {
    const s = getSession();
    return s && s.user.roles.includes(role);
  }

  function logout() {
    sessionStorage.removeItem(STORAGE_KEY);
  }

  return { loginAs, getSession, getToken, getUser, isLoggedIn, hasRole, logout, DEMO_USERS };
})();

// Expose globally
window.Auth = Auth;
