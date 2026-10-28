package edu.casestudy.merkle.web;

import static org.junit.jupiter.api.Assertions.*;

import edu.casestudy.merkle.MerkleApplication;
import edu.casestudy.merkle.service.CheckpointService;
import edu.casestudy.merkle.service.IntegrityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(classes = MerkleApplication.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class DatasetSummaryControllerTest {
  @Autowired DatasetSummaryController summaries;
  @Autowired IntegrityService integrity;
  @Autowired CheckpointService checkpoints;

  @Test void reportsCurrentAndTrustedIntegrityState() {
    var dataset = integrity.create("summary dataset");
    integrity.add(dataset.id, "2026|Ada|Science|A");
    var initial = summaries.one(dataset.id);

    assertEquals(1, initial.recordCount());
    assertEquals("NOT_RUN", initial.verificationStatus());
    assertNull(initial.trustedRoot());

    var checkpoint = checkpoints.create(dataset.id);
    integrity.verify(dataset.id);
    var verified = summaries.one(dataset.id);

    assertEquals(checkpoint.rootHash, verified.currentRoot());
    assertEquals(checkpoint.rootHash, verified.trustedRoot());
    assertEquals("VERIFIED", verified.verificationStatus());
    assertNotNull(verified.lastVerifiedAt());
  }
}
