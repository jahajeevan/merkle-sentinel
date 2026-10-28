package edu.casestudy.merkle.web;

import edu.casestudy.merkle.data.Entities.Checkpoint;
import edu.casestudy.merkle.data.Entities.Verification;
import edu.casestudy.merkle.data.Repositories.Datasets;
import edu.casestudy.merkle.data.Repositories.Records;
import edu.casestudy.merkle.data.Repositories.Verifications;
import edu.casestudy.merkle.service.CheckpointService;
import edu.casestudy.merkle.service.IntegrityService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Read-only dashboard projection; trust is never inferred from the current root. */
@RestController
@RequestMapping("/api/v1")
public class DatasetSummaryController {
  private final IntegrityService integrity;
  private final CheckpointService checkpoints;
  private final Datasets datasets;
  private final Records records;
  private final Verifications verifications;

  public DatasetSummaryController(
      IntegrityService integrity,
      CheckpointService checkpoints,
      Datasets datasets,
      Records records,
      Verifications verifications) {
    this.integrity = integrity;
    this.checkpoints = checkpoints;
    this.datasets = datasets;
    this.records = records;
    this.verifications = verifications;
  }

  public record Summary(
      UUID datasetId,
      String datasetName,
      int recordCount,
      String currentRoot,
      String trustedRoot,
      String verificationStatus,
      Instant lastVerifiedAt) {}

  @GetMapping("/datasets/{id}/summary")
  public Summary one(@PathVariable UUID id) {
    var dataset = integrity.dataset(id);
    var checkpoint = checkpoints.list(id).stream().findFirst();
    var verification = verifications.findTop20ByDatasetIdOrderByCreatedAtDesc(id).stream().findFirst();
    return summary(dataset.id, dataset.name, checkpoint, verification);
  }

  @GetMapping("/dataset-summaries")
  public List<Summary> all() {
    return datasets.findAll().stream()
        .map(dataset -> summary(
            dataset.id,
            dataset.name,
            checkpoints.list(dataset.id).stream().findFirst(),
            verifications.findTop20ByDatasetIdOrderByCreatedAtDesc(dataset.id).stream().findFirst()))
        .toList();
  }

  private Summary summary(
      UUID datasetId,
      String datasetName,
      java.util.Optional<Checkpoint> checkpoint,
      java.util.Optional<Verification> verification) {
    return new Summary(
        datasetId,
        datasetName,
        records.findByDatasetIdOrderByPosition(datasetId).size(),
        integrity.tree(datasetId).root(),
        checkpoint.map(value -> value.rootHash).orElse(null),
        verification.map(value -> value.status).orElse("NOT_RUN"),
        verification.map(value -> value.createdAt).orElse(null));
  }
}
