package edu.casestudy.merkle.service;

import edu.casestudy.merkle.MerkleApplication;
import edu.casestudy.merkle.data.Repositories.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes=MerkleApplication.class)
@DirtiesContext(classMode=DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CheckpointLifecycleTest {
  @Autowired IntegrityService integrity;
  @Autowired CheckpointService checkpoints;
  @Autowired Checkpoints checkpointRepository;
  @Autowired Records records;

  @Test void checkpointIsPersistedAcrossVerificationMutationAndRestore(){
    var dataset=integrity.create("checkpoint lifecycle");
    var first=integrity.add(dataset.id,"2026|Ada|Science|A");
    integrity.add(dataset.id,"2026|Grace|Science|A");
    var checkpoint=checkpoints.create(dataset.id);
    assertEquals(checkpoint.rootHash,integrity.tree(dataset.id).root());
    assertEquals(checkpoint.id,checkpointRepository.findByDatasetIdOrderByCreatedAtDesc(dataset.id).getFirst().id);
    assertEquals("VERIFIED",integrity.verify(dataset.id).get("status"));
    integrity.update(dataset.id,first.id,"2026|Ada|Science|B");
    var failed=integrity.verify(dataset.id);
    assertEquals("FAILED",failed.get("status"));
    assertEquals(checkpoint.rootHash,failed.get("trustedRoot"));
    assertNotEquals(checkpoint.rootHash,failed.get("currentRoot"));
    checkpoints.restore(dataset.id);
    assertEquals(checkpoint.rootHash,integrity.tree(dataset.id).root());
    assertEquals("VERIFIED",integrity.verify(dataset.id).get("status"));
    assertEquals(2,records.findByDatasetIdOrderByPosition(dataset.id).size());
  }
}
