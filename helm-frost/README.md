# FROST Server Helm Chart

This Helm chart deploys FROST Server with PostgreSQL/PostGIS database.

## Components

- **FROST Server**: OGC SensorThings API server (ports 8080 HTTP, 8883 MQTT)
- **PostgreSQL/PostGIS**: Database backend for FROST Server

## Prerequisites

- Kubernetes cluster
- Helm 3.x
- Storage class for persistent volumes (if persistence is enabled)

## Installation

### Basic Installation

```bash
helm install frost-server ./helm-frost -n frost --create-namespace
```

### Custom Configuration

1. Edit `values.yaml` to customize:
   - Database password (IMPORTANT: Change default password!)
   - Service root URL
   - Resource limits
   - Ingress configuration

2. Install with custom values:
```bash
helm install frost-server ./helm-frost -n frost --create-namespace -f custom-values.yaml
```

## Configuration

### Important Settings

- **Database Password**: Change `postgres.password` in `values.yaml` before production use!
- **Service Root URL**: Update `frost.serviceRootUrl` with your actual ingress hostname if using ingress
- **Namespace**: Default is `frost`, change in `values.yaml` if needed

### Ingress

To enable ingress, set in `values.yaml`:
```yaml
ingress:
  enabled: true
  host: "frost.yourdomain.com"
  path: /FROST-Server
  ingressClassName: nginx
  clusterIssuer: "letsencrypt-prod"  # For automatic TLS
```

### Persistent Storage

PostgreSQL data is stored in a PersistentVolumeClaim. Configure in `values.yaml`:
```yaml
postgres:
  persistence:
    enabled: true
    size: 10Gi
    storageClassName: ""  # Uses default storage class if empty
```

## Accessing FROST Server

### Via Port Forward (for testing)
```bash
kubectl port-forward -n frost svc/frost-server 8080:8080
```
Then access: `http://localhost:8080/FROST-Server`

### Via Ingress
If ingress is configured: `https://frost.yourdomain.com/FROST-Server`

## Database Connection

FROST Server connects to PostgreSQL using:
- **Host**: `postgres` (Kubernetes service name)
- **Port**: `5432`
- **Database**: `sensorthings`
- **Username**: `sensorthings`
- **Password**: As configured in `values.yaml`

## Upgrading

```bash
helm upgrade frost-server ./helm-frost -n frost
```

## Uninstallation

```bash
helm uninstall frost-server -n frost
```

**Note**: This will delete the PersistentVolumeClaim and all data unless you set `postgres.persistence.enabled: false` first.

## Values Reference

See `values.yaml` for all configurable options.

