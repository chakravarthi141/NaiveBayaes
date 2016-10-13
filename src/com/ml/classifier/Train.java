package com.ml.classifier;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.ml.beans.TrainData;
import com.ml.main.NaiveBayes;
import com.ml.utils.Constants;
import com.ml.utils.Utils;

/**
 * This class implements all the methods required to load training data and creates training modal
 */
public class Train {

	private TrainData data = new TrainData();

	//This map stores category of each document
	private Map<Integer, Integer> doc_cat_map = new HashMap<>();
	private int doc_count = 0;
	private boolean clean_data_req = false;

	public Train(){
		super();
	}

	/**
	 * Constructor that sets the bit to remove stop words
	 */
	public Train(boolean isCleaningReq) {
		super();
		clean_data_req = isCleaningReq;
	}

	/**
	 * This is the parent method for training. This method calls
	 * all the necessary methods required to load and create model in the order.
	 * output: Train Model
	 */
	public TrainData train() {
		int [] cat_doc_count = load_cat_data();
		data.cat_size = cat_doc_count.length;
		data.initTotal_word_count(data.cat_size); 
		calc_mle(cat_doc_count);
		load_doc_data();

		return data;
	}

	/**
	 * This method calculate MLE.
	 * Input: Category counts
	 * output: Sets the category probability to train model
	 */
	private void calc_mle(int [] cat_doc_count) {
		data.initCat_prob(cat_doc_count.length); 
		for(int i=1;i<cat_doc_count.length;i++) {
			data.setCat_prob(i, Utils.probability(cat_doc_count[i], doc_count)); 
		}
	}

	/**
	 * This method loads category data.
	 * output: document counts of each category
	 */
	private int[] load_cat_data() {
		int cat_count = 0;
		int category = -1;
		int [] cat_doc_count = null;
		try {
			//Loading category list
			for (String line : Files.readAllLines(Paths.get(NaiveBayes.CATEGORY_LIST))) {
				if(line != null) {
					Constants.category_map.put(++cat_count, line);
				}
			}
			cat_doc_count = new int[cat_count+1];

			//Loading category of each document
			for (String line : Files.readAllLines(Paths.get(NaiveBayes.TRAIN_LABEL))) {
				if(line != null) {
					category = Integer.parseInt(line.trim());
					doc_cat_map.put(++doc_count, category);
					cat_doc_count[category]++;
				}
			}
		}catch(Exception e) {
			System.err.println("Something went wrong while loading categories"); 
		}
		return cat_doc_count;
	}

	/**
	 * This method loads and process document data.
	 * output: Sets word counts of each category in training model
	 */
	private void load_doc_data() {
		int vocab_count = 0;
		String [] array = null;
		int doc_id = -1;
		int word_id = -1;
		int count = -1;
		try {
			if(Constants.stop_word_set.isEmpty()) 
				Utils.init_stopwords();
			for (String line : Files.readAllLines(Paths.get(NaiveBayes.VOCAB_LIST))) {
				if(line != null) {
					if(clean_data_req) {
						//uncomment below line to perform stemming on words
						//line = Utils.tokenizeStopStem(line);

						//Removing stop words and not adding word to list if its a stop word
						if(Constants.stop_word_set.contains(line)) {
							vocab_count++;
							continue;
						}
					} 
					Constants.vocab_map.put(++vocab_count, line);
				}
			}
			if(clean_data_req) {
				System.out.println("Total: "+vocab_count); 
				System.out.println("Filtered: "+(vocab_count-Constants.vocab_map.size())); 				
			}

			//initializing all required arrays for the model
			data.initWord_cat_count(vocab_count+1, data.cat_size);
			data.org_vocab_size = vocab_count;
			data.vocab_size = Constants.vocab_map.keySet().size();

			//Iterating through all word counts and adding it to respective category word count
			for (String line : Files.readAllLines(Paths.get(NaiveBayes.TRAIN_DATA))) {
				if(line != null) {
					array = line.split(" ");
					doc_id = Integer.parseInt(array[0]);
					word_id = Integer.parseInt(array[1]);
					count = Integer.parseInt(array[2]);
					if(Constants.vocab_map.containsKey(word_id)) {
						data.incWord_cat_count(word_id, doc_cat_map.get(doc_id), count);  
						data.incTotal_word_count(doc_cat_map.get(doc_id), count); 
					}
				}
			}
		}catch(Exception e) {
			System.err.println("Something went wrong while loading vocabulary"); 
		}
	}

}
