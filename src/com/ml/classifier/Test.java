package com.ml.classifier;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.ml.beans.TrainData;
import com.ml.main.NaiveBayes;
import com.ml.utils.Constants;

/**
 * This class implements all the methods required to load test data and to calculate accuracy.
 */
public class Test {

	TrainData data = null;
	int [][] confusion_matrix = null;
	double beta;
	boolean logging = true;
	public static Map<Integer, Integer> act_doc_cat_map = new HashMap<>(); 
	
	//Using Thread safe concurrent HashMap to access this table among different threads
	public static Map<Integer, Integer> pred_doc_cat_map = new ConcurrentHashMap<>(); 

	/**
	 * This constructor initializes class with training data.
	 */
	public Test(TrainData data) {
		this.data = data;
		beta = 1.0/data.vocab_size;
		confusion_matrix = new int[data.cat_size][data.cat_size];
	}

	/**
	 * This constructor initializes class with training data and beta value.
	 */
	public Test(TrainData data, double beta) {
		this.data = data;
		this.beta = beta;
		logging = false;
		confusion_matrix = new int[data.cat_size][data.cat_size];
	}

	/**
	 * This is the parent method for testing. This method calls
	 * all the necessary methods required to load test data 
	 * and predict the value for each test record and compute accuracy
	 * output: Accuracy
	 */
	public float test() {
		ExecutorService service = null;
		float accuracy = -1;
		try{
			//Using Java thread pool and divided test tasks among threads to improve performance
			service = Executors.newFixedThreadPool(Constants.THREAD_COUNT);
			service.submit(new LoadTestCat());
			predict(service); 
			service.shutdown();
			//Waiting for all threads to finish
			while(service.isTerminated()){
				Thread.sleep(100);
				System.out.println("Number of active tasks: "+Thread.activeCount()); 
			} 
			accuracy = calc_accuracy();
			if(logging) {
				System.out.println("Accuracy : "+accuracy+"%.");
				System.out.println("\n************Confusion Matrix************"); 
				for(int i=1;i<data.cat_size;i++) {
					for(int j=1;j<data.cat_size;j++) {
						System.out.print(confusion_matrix[i][j]+"\t");
					}
					System.out.println(); 
				} 
			}
		}catch(Exception e) {
			e.printStackTrace(); 
			System.err.println("Error occured while testing"); 
		}
		return accuracy;
	}

	/**
	 * This method predicts the outcome of given test values
	 * input : Thread pool to run the testing environment
	 * output: predicted value
	 */
	private void predict(ExecutorService service) {
		int doc_id = -1;
		int prev_doc_id = -1;
		Set<int[]> doc_set = null;
		int [] word_count = null;
		try{
			//Reading test data and assigning each record to thread pool
			for (String line : Files.readAllLines(Paths.get(NaiveBayes.TEST_DATA))) {
				if(line != null) {
					String [] array = line.split(" ");
					doc_id = Integer.parseInt(array[0]);
					word_count = new int[2];
					word_count[0] = Integer.parseInt(array[1]);
					word_count[1] = Integer.parseInt(array[2]);
					if(prev_doc_id != doc_id) {
						if(doc_set != null) {
							service.submit(new Classifier(doc_set, prev_doc_id, data, beta)); 
						}
						doc_set = new HashSet<>();
					} 
					doc_set.add(word_count);
				}
				prev_doc_id = doc_id;
			}
			//checking if there is any left over test data
			if(doc_set != null) {
				service.submit(new Classifier(doc_set, prev_doc_id, data, beta)); 
			}
		}catch(Exception e) {
			System.err.println("Error occured while predicting"); 
		}
	}

	/**
	 * This method calculate accuracy by comparing predicted values against actual values.
	 * output: Accuracy
	 */
	private float calc_accuracy() {
		int total_test_cases = act_doc_cat_map.keySet().size();
		int r_predict_count = 0;
		int actual = -1;
		int prediction = -1;
		//Reading actual values and comparing against predicted values.
		for(int test_case : act_doc_cat_map.keySet()) {
			actual = act_doc_cat_map.get(test_case);
			prediction = pred_doc_cat_map.get(test_case);
			if(actual == prediction) 
				r_predict_count++;
			confusion_matrix[actual][prediction]++;
		}
		if(logging) {
			System.out.println("Accuracy obtained by: ");
			System.out.println("Number of correctly classified documents in the test set: "+r_predict_count);
			System.out.println("Total Number of test documents: "+total_test_cases); 
		}
		return r_predict_count*100.0f/total_test_cases;
	}
}
