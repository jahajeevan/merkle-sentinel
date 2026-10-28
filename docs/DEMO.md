# Demonstration workflow

1. Create `Student Records` and add `2026|Ada Lovelace|A` plus other records.
2. Click **Create trusted checkpoint**, then **Run verification**; it reports `VERIFIED`.
3. Use the row’s **Tamper** control (available only in development/demo mode), then verify; the result is `FAILED` and lists affected UUIDs.
4. Select an unchanged record to display its proof, then click **Verify proof** and **Test invalid proof**.
5. In the Tampering laboratory, choose **Restore trusted snapshot**, then scan again to see `VERIFIED`.
6. Run the Performance laboratory to execute actual Java build, scan and proof timings.
