package edu.casestudy.merkle.core;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class MerkleTreeTest {
  private List<MerkleTree.Leaf> leaves(int count){var out=new ArrayList<MerkleTree.Leaf>();for(int i=0;i<count;i++)out.add(new MerkleTree.Leaf(UUID.nameUUIDFromBytes(("leaf-"+i).getBytes()),i,"payload-"+i));return out;}
  @Test void rootsAndProofsAreDeterministic(){var tree=new MerkleTree(leaves(2));assertEquals(tree.root(),new MerkleTree(leaves(2)).root());var proof=tree.proof(leaves(2).get(0).id());assertTrue(ProofVerifier.verify(proof.leafHash(),proof,tree.root()));assertFalse(ProofVerifier.verify(HashUtils.hex(HashUtils.leaf(HashUtils.utf8("tampered"))),proof,tree.root()));}
  @Test void supportsRequestedLeafCountsAndValidProofs(){for(int count:List.of(1,2,3,4,5,8,16,129)){var source=leaves(count);var tree=new MerkleTree(source);assertTrue(tree.root().matches("[0-9a-f]{64}"));for(var leaf:source){var proof=tree.proof(leaf.id());assertTrue(ProofVerifier.verify(proof.leafHash(),proof,tree.root()),"valid proof for "+count+" leaves");}assertEquals(tree.root(),tree.structure().hash());}}
  @Test void handlesEmptyRootAndSingleLeaf(){assertEquals(HashUtils.hex(HashUtils.empty()),new MerkleTree(List.of()).root());var only=leaves(1).get(0);var tree=new MerkleTree(List.of(only));assertEquals(only.id().toString(),tree.structure().recordId());assertTrue(ProofVerifier.verify(tree.proof(only.id()).leafHash(),tree.proof(only.id()),tree.root()));}
  @Test void structureMapsEachPersistedLeafIdentityOnce(){for(int count:List.of(0,1,2,3,5,8)){var source=leaves(count);var tree=new MerkleTree(source);var ids=new HashSet<String>();collectDistinctLeafIds(tree.structure(),ids);assertEquals(source.stream().map(x->x.id().toString()).collect(java.util.stream.Collectors.toSet()),ids,"distinct record leaves for "+count+" records");}}
  private void collectDistinctLeafIds(MerkleNode node,Set<String> ids){if(node==null)return;if(node.leaf()){assertNotNull(node.recordId());ids.add(node.recordId());return;}collectDistinctLeafIds(node.left(),ids);collectDistinctLeafIds(node.right(),ids);}
  @Test void rejectsWrongSiblingRootAndOrdering(){var tree=new MerkleTree(leaves(5));var proof=tree.proof(leaves(4).get(3).id());assertFalse(ProofVerifier.verify(proof.leafHash(),proof,"0".repeat(64)));var changed=new ArrayList<>(proof.steps());changed.set(0,new MerkleProof.Step("0".repeat(64),changed.get(0).siblingOnLeft()));assertFalse(ProofVerifier.verify(proof.leafHash(),new MerkleProof(proof.leafHash(),changed),tree.root()));var reversed=new ArrayList<>(proof.steps());Collections.reverse(reversed);assertFalse(ProofVerifier.verify(proof.leafHash(),new MerkleProof(proof.leafHash(),reversed),tree.root()));assertFalse(ProofVerifier.verify("bad",proof,tree.root()));}
  @Test void canonicalizationOrderingAndBoundaries(){var id=UUID.randomUUID();assertArrayEquals(RecordCanonicalizer.canonical(id,1,"é"),RecordCanonicalizer.canonical(id,1,"e\u0301"));assertNotEquals(new MerkleTree(List.of(new MerkleTree.Leaf(id,0,"same"))).root(),new MerkleTree(List.of(new MerkleTree.Leaf(id,1,"same"))).root());}
}
