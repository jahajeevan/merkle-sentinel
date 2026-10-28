package edu.casestudy.merkle.core;
import java.util.*;
public record MerkleProof(String leafHash, List<Step> steps){ public record Step(String siblingHash, boolean siblingOnLeft){} }
