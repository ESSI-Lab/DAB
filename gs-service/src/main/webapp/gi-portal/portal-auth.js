/**
 * Portal authentication: who the current user is and how requests to DAB are authenticated.
 *
 * Two kinds of users:
 *  - Keycloak users (config.keycloakLogin): log in with the Keycloak realm trusted by DAB (read from
 *    ../services/support/auth/keycloak). Requests are authenticated with the access token in the
 *    "Authorization: Bearer" header; the token is kept in memory only and refreshed when needed.
 *  - users with a personal token (config.apiKeyLogin, default true): email + personal token login,
 *    the token is sent in the request path, as before.
 *
 * Keycloak users who already logged in are recognized on page load with a quick redirect to Keycloak
 * (only when a previous Keycloak session is remembered, so anonymous visitors are never redirected).
 */
import Keycloak from './lib/keycloak.js';

const KEYCLOAK_INFO_URL = '../services/support/auth/keycloak';
const CURRENT_USER_URL = '../services/support/auth/me';

/** remembers that the user logged in with Keycloak, to restore the session on page load */
const SESSION_FLAG = 'portalKeycloakSession';

/** legacy login (personal token) */
const LEGACY_KEYS = ['authToken', 'userEmail', 'isAdmin', 'userPermissions'];

let keycloak = null;
let keycloakUser = null;

function readStorage(key) {
	try {
		return localStorage.getItem(key);
	} catch (e) {
		return null;
	}
}

function writeStorage(key, value) {
	try {
		if (value === null) {
			localStorage.removeItem(key);
		} else {
			localStorage.setItem(key, value);
		}
	} catch (e) {
		// storage not available: the session is simply not remembered
	}
}

function clearLegacySession() {
	LEGACY_KEYS.forEach(key => writeStorage(key, null));
}

/**
 * Sets up the Keycloak login, if enabled for this portal and configured in DAB, and restores the
 * Keycloak session. Never throws: on errors the portal works without the Keycloak login.
 *
 * @param {object} config portal configuration
 */
export async function initPortalAuth(config) {

	if (!config || !config.login || !config.keycloakLogin) {
		return;
	}

	let info = null;
	try {
		const response = await fetch(KEYCLOAK_INFO_URL, { cache: 'no-cache' });
		info = response.ok ? await response.json() : null;
	} catch (e) {
		info = null;
	}

	if (!info || !info.enabled || !info.loginClientId) {
		console.warn('Keycloak login enabled in the portal configuration, but not configured in DAB');
		return;
	}

	keycloak = new Keycloak({ url: info.keycloakUrl, realm: info.realm, clientId: info.loginClientId });

	const remembered = readStorage(SESSION_FLAG) === '1';

	try {
		// without onLoad, init only completes a login redirect (if the page is the redirect target)
		await keycloak.init({
			onLoad: remembered ? 'check-sso' : undefined,
			pkceMethod: 'S256',
			checkLoginIframe: false
		});
	} catch (e) {
		console.error('Keycloak initialization failed', e);
	}

	if (!keycloak.authenticated) {
		writeStorage(SESSION_FLAG, null);
		return;
	}

	writeStorage(SESSION_FLAG, '1');

	// a Keycloak session replaces any personal token session
	clearLegacySession();

	keycloak.onTokenExpired = () => keycloak.updateToken(30).catch(() => { });

	try {
		const response = await authFetch(CURRENT_USER_URL, { cache: 'no-store' });
		const me = response.ok ? await response.json() : null;
		keycloakUser = me && me.authenticated ? me : null;
	} catch (e) {
		keycloakUser = null;
	}

	if (!keycloakUser) {
		console.error('Logged in with Keycloak, but the user is not recognized by DAB');
	}
}

//
// login state
//

/** @returns {boolean} true if the Keycloak login is available in this portal */
export function isKeycloakLoginAvailable() {
	return keycloak !== null;
}

/** @returns {boolean} true if the personal token login is available in this portal */
export function isApiKeyLoginAvailable(config) {
	return !config || config.apiKeyLogin !== false || !isKeycloakLoginAvailable();
}

/** @returns {boolean} true if the user is logged in with Keycloak */
export function isKeycloakSession() {
	return keycloak !== null && keycloak.authenticated === true && keycloakUser !== null;
}

/** @returns {string|null} the personal token of a user logged in with the legacy login */
export function getLegacyToken() {
	return isKeycloakSession() ? null : readStorage('authToken');
}

/** @returns {boolean} true if a user is logged in (Keycloak or personal token) */
export function isLoggedIn() {
	return isKeycloakSession() || !!(readStorage('authToken') && readStorage('userEmail'));
}

/** @returns {string} email (or user name) of the logged in user */
export function getUserLabel() {
	if (isKeycloakSession()) {
		return keycloakUser.email || keycloakUser.username || keycloakUser.identifier;
	}
	return readStorage('userEmail') || '';
}

/** @returns {string[]} permissions of the logged in user */
export function getUserPermissions() {
	if (isKeycloakSession()) {
		return keycloakUser.permissions || [];
	}
	return (readStorage('userPermissions') || '').split(',').map(p => p.trim()).filter(Boolean);
}

/** @returns {boolean} true for administrators (only with the personal token login) */
export function isAdminUser() {
	return !isKeycloakSession() && readStorage('isAdmin') === 'true';
}

//
// requests
//

/**
 * Base URL of the O&M API for the given view, authenticated as the current user.
 *
 * @param {string} view
 * @param {string} [fallbackToken] token used when nobody is logged in (e.g. the portal public token)
 * @returns {string|null} null if nobody is logged in and there is no fallback token
 */
export function omApiBase(view, fallbackToken) {
	if (isKeycloakSession()) {
		return `../services/essi/view/${view}/om-api`;
	}
	const token = readStorage('authToken') || fallbackToken;
	if (!token) {
		return null;
	}
	return `../services/essi/token/${token}/view/${view}/om-api`;
}

/**
 * fetch() authenticated as the current user: with a Keycloak session, the access token is refreshed
 * if needed and sent in the Authorization header.
 */
export async function authFetch(url, options = {}) {
	if (keycloak !== null && keycloak.authenticated) {
		try {
			await keycloak.updateToken(30);
		} catch (e) {
			// the session is over: the request is sent without token, the user will have to log in again
			console.warn('Keycloak session expired');
		}
		if (keycloak.authenticated && keycloak.token) {
			const headers = new Headers(options.headers || {});
			headers.set('Authorization', 'Bearer ' + keycloak.token);
			options = Object.assign({}, options, { headers: headers });
		}
	}
	return fetch(url, options);
}

//
// login / logout
//

/** Keycloak login, back to the current page */
export function keycloakLogin() {
	if (keycloak) {
		keycloak.login({ redirectUri: window.location.href });
	}
}

/** Keycloak registration, back to the current page */
export function keycloakRegister() {
	if (keycloak) {
		keycloak.register({ redirectUri: window.location.href });
	}
}

/** Logs out the current user (also from Keycloak) and reloads the page */
export function portalLogout() {
	clearLegacySession();
	writeStorage('lang', null);
	if (keycloak !== null && keycloak.authenticated) {
		writeStorage(SESSION_FLAG, null);
		keycloak.logout({ redirectUri: window.location.href });
		return;
	}
	window.location.reload();
}
