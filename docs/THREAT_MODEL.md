# Threat model

The application detects edits, inserts, and deletions made after an existing checkpoint because its signed manifest maps stable record UUIDs to leaf hashes. It does not automatically trust a newly calculated root.

An attacker that can modify both the database and the signing secret can forge a checkpoint. Therefore production checkpoint keys belong outside the application/database (KMS/HSM), signed roots should be replicated to an independent service or append-only store, and key IDs plus rotation history should be recorded. Application monitoring detects app writes immediately; direct database writes are found only during a later scan. Ordinary audit tables are not independently tamper-proof.
