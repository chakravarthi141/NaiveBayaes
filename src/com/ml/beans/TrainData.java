package com.ml.beans;

/**
* This is a class that stores the trained model
* This class contains all the necessary variable to store model and 
* all the necessary methods to access the data.
*/
public class TrainData {

	public int cat_size;
	public int vocab_size;
	public int org_vocab_size;
	private float [] cat_prob;
	private long [][] word_cat_count;
	private long [] total_word_count;
	
	public float[] getCat_prob() {
		return cat_prob;
	}
	public void initCat_prob(int size) {
		this.cat_prob = new float[size];
	}
	public void setCat_prob(int index, float value) {
		this.cat_prob[index] = value;
	}
	public void setCat_prob(float[] cat_prob) {
		this.cat_prob = cat_prob;
	}
	public long[][] getWord_cat_count() {
		return word_cat_count;
	}
	public long getWord_cat_count(int row, int col) {
		return word_cat_count[row][col];
	}
	public void initWord_cat_count(int a, int b) {
		word_cat_count = new long[a][b];
	}
	public void setWord_cat_count(long[][] word_cat_count) {
		this.word_cat_count = word_cat_count;
	}
	public void setWord_cat_count(int row, int col, long value) {
		this.word_cat_count[row][col] = value;
	}
	public void incWord_cat_count(int row, int col) {
		this.word_cat_count[row][col] += 1;
	}
	public void incWord_cat_count(int row, int col, int count) {
		if(row > 0 && col > 0 && row < org_vocab_size+1 && col < cat_size+1)
			this.word_cat_count[row][col] += count;
	}
	public long[] getTotal_word_count() {
		return total_word_count;
	}
	public void initTotal_word_count(int size) {
		this.total_word_count = new long[size];
	}
	public void incTotal_word_count(int index, int count) {
		if(index > 0 && index < total_word_count.length+1)
			this.total_word_count[index] += count;
	}
	public void setTotal_word_count(long[] total_word_count) {
		this.total_word_count = total_word_count;
	}
}
