import { PORTAL_ASSET_VERSION } from './portal-version.js';

const scriptVersion = new URL(import.meta.url).searchParams.get('v') || PORTAL_ASSET_VERSION;
window.__PORTAL_ASSET_VERSION__ = scriptVersion;

function withVersion(modulePath) {
	const resolved = new URL(modulePath, import.meta.url);
	resolved.searchParams.set('v', scriptVersion);
	return resolved.href;
}

function appendStylesheet() {
	if (document.querySelector('link[data-portal-stylesheet="true"]')) {
		return;
	}

	const link = document.createElement('link');
	link.rel = 'stylesheet';
	link.type = 'text/css';
	link.href = withVersion('./gi-portal.css');
	link.setAttribute('data-portal-stylesheet', 'true');
	document.head.appendChild(link);
}

appendStylesheet();

await import(withVersion('./libraries.js'));

const { initializePortal } = await import(withVersion('./gi-portal.js'));

const configUrl = new URL('./config.json', window.location.href);
configUrl.searchParams.set('v', scriptVersion);

fetch(configUrl.toString(), { cache: 'no-store' })
	.then(function(response) {
		return response.json();
	})
	.then(function(config) {
		return initializePortal(config);
	})
	.catch(function(err) {
		console.error('Configuration load error:', err);
	});
