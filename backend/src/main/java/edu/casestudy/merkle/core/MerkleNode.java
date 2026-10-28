package edu.casestudy.merkle.core;
import java.util.*;
public record MerkleNode(String hash, String recordId, MerkleNode left, MerkleNode right){ public boolean leaf(){return left==null&&right==null;} }
