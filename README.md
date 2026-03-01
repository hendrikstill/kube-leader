# kube-leader

A Kotlin library for Kubernetes leader election using the [Kubernetes Lease API](https://kubernetes.io/docs/concepts/architecture/leases/).

## Modules

| Module | Description |
|--------|-------------|
| `kube-leader-core` | Core leader election logic (no Spring dependency) |
| `kube-leader-spring-boot-starter` | Spring Boot auto-configuration and `@IfIsKubeLeader` annotation |
| `kube-leader-spring-boot-example` | Example Spring Boot application |

## How It Works

Each application instance runs a background leader election loop against a Kubernetes `Lease` resource. Only one instance holds the lease at a time — that instance is the leader. When the leader stops renewing the lease (e.g., it crashes or is restarted), another instance takes over automatically.

## Spring Boot Starter

### Dependency

```xml
<dependency>
    <groupId>de.gammas</groupId>
    <artifactId>kube-leader-spring-boot-starter</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### Configuration

Add the following properties to your `application.yaml` / `application.properties`:

```yaml
spring:
  application:
    name: my-app   # used as the Lease lock name by default

kubeleader:
  enabled: true          # set to false to disable leader election (e.g. local development)
  namespace: default     # Kubernetes namespace to create the Lease in
  identity: my-pod-name  # unique identity for this instance (defaults to hostname)
  lockName: my-app       # name of the Lease resource (defaults to spring.application.name)
  leaseDuration: 10s     # how long a lease is held before it can be acquired by another instance
  renewDeadline: 8s      # how long the leader tries to renew before giving up
  retryPeriod: 2s        # how often a non-leader polls for the lease
```

Set `kubeleader.enabled=false` to disable leader election entirely, which is useful when running locally without access to a Kubernetes cluster.

### Annotation-based usage

Annotate any Spring-managed method with `@IfIsKubeLeader` to make it execute only when the current instance is the leader:

```java
@Component
public class ScheduledTask {
    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.SECONDS)
    @IfIsKubeLeader
    public void doWork() {
        // Only runs on the leader instance
    }
}
```

### Programmatic usage

Inject `KubeLeader` directly if you need finer control:

```kotlin
@Component
class MyService(private val kubeLeader: KubeLeader) {
    fun doConditionalWork() {
        if (kubeLeader.isLeader()) {
            // leader-only logic
        }
    }
}
```

## Core Library (without Spring)

```kotlin
val config = KubeLeaderConfig(lockName = "my-app", identity = "pod-1")
val leader = KubeLeader(kubeLeaderConfig = config)

// Run in a background thread — blocks until interrupted
Thread(leader::run).start()

// Check status at any time
if (leader.isLeader()) { /* ... */ }
```

## RBAC

The application's service account needs permission to get, create, and update `Lease` resources:

```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: kube-leader
rules:
  - apiGroups: ["coordination.k8s.io"]
    resources: ["leases"]
    verbs: ["get", "create", "update"]
---
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: kube-leader
subjects:
  - kind: ServiceAccount
    name: default
roleRef:
  kind: Role
  name: kube-leader
  apiGroup: rbac.authorization.k8s.io
```

## Building

Requires JDK 11 and a running Kubernetes cluster (or [Kind](https://kind.sigs.k8s.io/)) for the integration tests.

```bash
mvn -B package
```
