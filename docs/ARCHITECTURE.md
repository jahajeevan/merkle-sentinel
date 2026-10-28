# Architecture

The React/Vite client is a thin REST consumer; all hashes, trees, proofs and checkpoint decisions run in Java under `/api/v1`. Spring Boot persists datasets, records, checkpoints, verification events and audit events through JPA. Flyway owns schema changes. PostgreSQL is the deployment database and H2 is limited to the default ephemeral developer profile.

API: datasets and records use CRUD routes; `POST /datasets/{id}/checkpoints` is the sole trust-creation operation; `POST /datasets/{id}/verify` compares a recomputed root and current manifest to the last checkpoint; `GET /datasets/{id}/tree` returns tree structure; proof routes generate/validate inclusion proofs. `POST /datasets/{id}/restore` restores the latest retained snapshot, while `POST /benchmarks` performs actual Java timing runs. Health is restricted to `/actuator/health`.

All writes run in Spring transactions. Dataset version increments protect the audit trail from ambiguity; a production next step is an `If-Match` version requirement for concurrent browser edits.
