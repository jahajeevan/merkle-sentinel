package edu.casestudy.merkle.service;

import edu.casestudy.merkle.core.*;
import org.springframework.stereotype.Service;
import java.util.*;

@Service public class BenchmarkService {
  public List<Map<String,Object>> run(int requested) {
    int[] sizes = requested > 0 ? new int[]{Math.min(requested, 20000)} : new int[]{100,1000,5000};
    var result = new ArrayList<Map<String,Object>>();
    for (int size : sizes) {
      var leaves=new ArrayList<MerkleTree.Leaf>();
      for(int i=0;i<size;i++) leaves.add(new MerkleTree.Leaf(UUID.nameUUIDFromBytes(("bench-"+i).getBytes()),i,"student|"+i+"|grade|"+(i%100)));
      long started=System.nanoTime(); var tree=new MerkleTree(leaves); long build=System.nanoTime()-started;
      var proof=tree.proof(leaves.get(size/2).id()); started=System.nanoTime(); tree.proof(leaves.get(size/2).id()); long proofGeneration=System.nanoTime()-started;
      started=System.nanoTime(); boolean valid=ProofVerifier.verify(proof.leafHash(),proof,tree.root()); long proofVerification=System.nanoTime()-started;
      started=System.nanoTime(); new MerkleTree(leaves).root(); long fullVerification=System.nanoTime()-started;
      result.add(Map.of("size",size,"treeBuildMs",ms(build),"fullVerificationMs",ms(fullVerification),"proofGenerationMs",ms(proofGeneration),"proofVerificationMs",ms(proofVerification),"proofValid",valid));
    } return result;
  }
  private double ms(long nanos){return Math.round(nanos/10000d)/100d;}
}
