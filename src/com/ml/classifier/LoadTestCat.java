package com.ml.classifier;

import java.nio.file.Files;
import java.nio.file.Paths;

import com.ml.main.NaiveBayes;

/**
* This class has all the methods required to load the actual outcome of test data
*/		
public class LoadTestCat implements Runnable {

	/**
	* This method reads the actual outcome of test from input file and stores data in a map.
	*/		
	@Override
	public void run() {
		int test_doc_count = 0;
		try {
			for (String line : Files.readAllLines(Paths.get(NaiveBayes.TEST_LABEL))) {
				if(line != null) {
					Test.act_doc_cat_map.put(++test_doc_count, Integer.parseInt(line.trim()));
				}
			}
		}catch(Exception e) {
				System.err.println("Something went wrong while loading test categories"); 
		}
	}

}
