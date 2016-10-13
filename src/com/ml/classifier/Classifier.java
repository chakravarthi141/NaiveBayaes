package com.ml.classifier;

import java.util.Set;

import com.ml.beans.TrainData;

/**
 * This class has thread implementation required to predict the 
 * outcome of a given test data based on the training model.
 */	
public class Classifier implements Runnable {

	Set<int[]> vocab_matrix; 
	int doc_id;
	double max_prob;
	int pred_cat=-1;
	TrainData data;
	double beta;
	public Classifier(Set<int[]> matrix, int docID, TrainData data, double b) {
		vocab_matrix = matrix;
		doc_id = docID;
		this.data = data;
		beta = b;
		max_prob = Double.NEGATIVE_INFINITY;
	}
	
	/**
	* This method predicts the outcome for given test data
	* Input: test data and training model
	* Output: stored predicted value in a map
	*/			
	@Override
	public void run() {
		double prob = 0;
		try {
		for(int i=1;i<data.cat_size;i++) {
			prob = logbase2(data.getCat_prob()[i]);
			for(int [] val : vocab_matrix) {
				prob += val[1]*logbase2((data.getWord_cat_count(val[0], i)+beta)*1.0d/(data.getTotal_word_count()[i]+beta*data.vocab_size));
			}
			if( Double.compare(max_prob, prob) < 0) {
				max_prob = prob;
				pred_cat = i;
			}
		}
		}catch(Exception e){
			e.printStackTrace();
		}
		Test.pred_doc_cat_map.put(doc_id, pred_cat);
	}

	/**
	* This method computes the probability for the given values
	* Input: attribute count and total count
	* Output: probability of an attribute
	*/		
	private double logbase2(double x) {
		return (Math.log(x) / Math.log(2));
	}
}
