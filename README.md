# Merkle Sentinel

Merkle Sentinel is a local-first case-study application for verifying dataset integrity with a Java SHA-256 Merkle tree. It separates the **current calculated root** from an explicitly created **trusted checkpoint**; a normal record edit never changes trust. The UI includes overview, datasets, records/export, tree/proof explorer, verification, tampering/recovery, audit history, benchmarks, and protocol documentation.

## Quick start (macOS/Linux)

```sh
./scripts/setup.sh
./scripts/dev.sh
```

Open `http://localhost:5173`. The API runs at `http://localhost:8080`; H2 is used only for the local `dev` profile. The local profile intentionally permits the demonstration UI. For PostgreSQL, copy `.env.example`, start `docker compose up -d db`, then run with `SPRING_PROFILES_ACTIVE=postgres APP_SECURITY_ENABLED=true APP_ADMIN_PASSWORD='use-a-long-random-password' ./backend/mvnw spring-boot:run`.

## Commands

```sh
./backend/mvnw test                 # Java tests
cd frontend && npm test -- --run    # UI tests
cd frontend && npm run build        # production UI bundle
docker compose up --build           # PostgreSQL + backend
```

`scripts/setup.sh` downloads Maven into the project cache when Maven is absent; it never changes the system installation. Node/npm and a JDK are checked before installation.

## Integrity model

Records are ordered by immutable `position`, then UUID. Canonical record bytes are UTF-8 JSON-like length-prefixed fields: `recordId`, `position`, and normalized payload (Unicode NFC; null is distinct from empty). A leaf is `SHA-256(0x00 || canonicalBytes)` and a parent is `SHA-256(0x01 || leftHash || rightHash)`. Odd levels duplicate their final node. Empty root is `SHA-256(0x02)`.

Tree build and scans are O(n); proof generation and verification are O(log n); fixed-position update is O(log n) in the in-memory tree, although this service rebuilds after persistence changes to keep ordering and deletion semantics simple. Inserts, deletes, and reordering can require O(n) rebuilding. The tree stores O(n) nodes.

## CSV export

The Records page exports the selected dataset's persisted records as UTF-8 CSV with `position`, `record_id`, and `payload` columns. Values are quoted according to CSV rules. Payloads that begin with `=`, `+`, `-`, or `@` are prefixed with an apostrophe only in exports to prevent spreadsheet formula execution; stored payloads and Merkle hashes are unchanged. CSV import is intentionally not a product feature.

## Security and limitations

Checkpoints contain a signed manifest of record hashes. The local signing key (`APP_CHECKPOINT_KEY`) is **development-only** and must not be treated as protection from a database attacker who can read configuration. Production deployments should keep signing keys in KMS/HSM, retain signed checkpoints separately, apply least-privilege database roles, TLS, backups, rate limiting at the gateway, and security review. Audit rows in the same database are useful evidence but are not tamper-proof. Event-triggered checks cover application writes; use the configurable periodic/on-demand scan to discover out-of-band modifications.

The tampering endpoint only works when `APP_DEMO_MODE=true` (the local dev default) and is disabled otherwise. Production security enables HTTP Basic authentication and requires `APP_ADMIN_PASSWORD`. Checkpoints retain a recovery snapshot in this demonstration project; treat that snapshot as sensitive data and encrypt/restrict it in a real deployment.

## Documents

- `docs/ARCHITECTURE.md` — components, API, data flow and operations.
- `docs/THREAT_MODEL.md` — threat model and deployment limits.
- `docs/DEMO.md` — tested walkthrough.

