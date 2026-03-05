# Session Coordinator

Generic session coordination for systems that allow only one active session at a time (e.g. ARPA Lombardia, ARPA Marche). Uses a **namespace** to isolate Redis keys when multiple systems share the same Redis instance.

## Package: `eu.essi_lab.accessor.session`

### Interfaces

| Class | Description |
|-------|-------------|
| **TokenStore** | Stores and retrieves session tokens. Implementations: file (single-node) or Redis (multi-node). |
| **SessionCoordinator** | Coordinates exclusive access. `runWithExclusiveSession(SessionReclaimer, SessionWork)` guarantees only one run at a time cluster-wide. |
| **SessionCoordinator.SessionWork&lt;T&gt;** | Functional interface for the work to run (typically login → API call → logout). |
| **SessionCoordinator.SessionReclaimer** | Callback to reclaim a dead node's session (e.g. remote logout). |

### Implementations

| Class | Description |
|-------|-------------|
| **RedisTokenStore** | Token in Redis at `{namespace}:token`. |
| **FileTokenStore** | Token in `{namespace}.token` under `java.io.tmpdir`. |
| **JedisSessionCoordinator** | Queue-based coordination via Redis: nodes enter queue, send heartbeats every 5s; first in queue runs after current holder finishes or is dead (no heartbeat 30s). Keys: `{namespace}:queue`, `{namespace}:active`, `{namespace}:alive:{nodeId}`. |
| **FileSessionCoordinator** | No coordination; runs work directly (single-node). |

### Lombardia integration (`eu.essi_lab.accessor.hiscentral.lombardia`)

| Class | Description |
|-------|-------------|
| **LombardiaTokenStore** | Extends `TokenStore`. |
| **LombardiaSessionCoordinator** | Lombardia-specific interface with `runWithExclusiveSession(HISCentralLombardiaClient, SessionWork)`. |
| **JedisLombardiaSessionCoordinator** | Delegates to `JedisSessionCoordinator` with namespace `arpa-lombardia` (configurable). |
| **RedisLombardiaTokenStore** | Delegates to `RedisTokenStore`. |
| **FileLombardiaTokenStore** | Delegates to `FileTokenStore`. |
| **FileLombardiaSessionCoordinator** | Delegates to `FileSessionCoordinator`. |

### Usage example (generic)

```java
String namespace = "arpa-marche";
TokenStore tokenStore = new RedisTokenStore(namespace, jedisPool);
SessionCoordinator coordinator = new JedisSessionCoordinator(namespace, jedisPool, tokenStore);

coordinator.runWithExclusiveSession(
    token -> client.logoutWithToken(token),
    () -> { login(); callApi(); logout(); return result; }
);
```

### Configuration

**SessionCoordinatorSetting** (System tab): enables distributed coordination, Redis endpoint, and **namespace** (default: `arpa-lombardia`).
