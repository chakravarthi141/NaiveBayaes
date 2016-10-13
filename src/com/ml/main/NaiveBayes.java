package com.ml.main;

import java.text.DecimalFormat;
import java.util.List;

import com.ml.beans.TrainData;
import com.ml.classifier.Test;
import com.ml.classifier.Train;
import com.ml.utils.Utils;

public class NaiveBayes {

	public static String TEST_DATA ="test.data";
	public static String TEST_LABEL ="test.label";
	public static String TRAIN_DATA ="train.data";
	public static String TRAIN_LABEL ="train.label";
	public static String VOCAB_LIST ="vocabulary.txt";
	public static String CATEGORY_LIST ="newsgrouplabels.txt";
	private static int QNO = 2;
	private static boolean clean = false;
	public static void main(String[] args) {
		Train train = null;
		TrainData data = null;
		Test test = null;
		for(int i=0;i<args.length;i++) {
			if(args[i].equalsIgnoreCase("-F") || args[i].equalsIgnoreCase("-train_data")) {
				if(i+1 < args.length && args[i+1] != null)
					TRAIN_DATA = args[i+1];
				else {
					System.err.println("Invalid option -F!!");
					System.err.println("-F or -train_data should be followed by valid path!!");
					System.exit(0);
				}
				i += 1;
			}if(args[i].equalsIgnoreCase("-E") || args[i].equalsIgnoreCase("-train_label")) {
				if(i+1 < args.length && args[i+1] != null)
					TRAIN_LABEL = args[i+1];
				else {
					System.err.println("Invalid option -E!!");
					System.err.println("-E or -train_label should be followed by valid path!!");
					System.exit(0);
				}
				i += 1;
			} if(args[i].equalsIgnoreCase("-T") || args[i].equalsIgnoreCase("-test_data")) {
				if(i+1 < args.length && args[i+1] != null)
					TEST_DATA = args[i+1];
				else {
					System.err.println("Invalid option -T!!");
					System.err.println("-T or -test_data should be followed by valid path!!");
					System.exit(0);
				}
				i += 1;
			} if(args[i].equalsIgnoreCase("-A") || args[i].equalsIgnoreCase("-test_label")) {
				if(i+1 < args.length && args[i+1] != null)
					TEST_LABEL = args[i+1];
				else {
					System.err.println("Invalid option -A!!");
					System.err.println("-A or -test_label should be followed by valid path!!");
					System.exit(0);
				}
				i += 1;
			} if(args[i].equalsIgnoreCase("-C") || args[i].equalsIgnoreCase("-category_data")) {
				if(i+1 < args.length && args[i+1] != null)
					CATEGORY_LIST = args[i+1];
				else {
					System.err.println("Invalid option -C!!");
					System.err.println("-C or -category_data should be followed by valid path!!");
					System.exit(0);
				}
				i += 1;
			} if(args[i].equalsIgnoreCase("-V") || args[i].equalsIgnoreCase("-vocabulary_data")) {
				if(i+1 < args.length && args[i+1] != null)
					VOCAB_LIST = args[i+1];
				else {
					System.err.println("Invalid option -V!!");
					System.err.println("-V or -vocabulary_data should be followed by valid path!!");
					System.exit(0);
				}
				i += 1;
			} if(args[i].equalsIgnoreCase("-Q") || args[i].equalsIgnoreCase("-question")) {
				if(i+1 < args.length && args[i+1] != null) {
					QNO = Integer.parseInt(args[i+1]);
					if(QNO < 0 || QNO > 3) 
						System.err.println("-Q or -question should be followed by valid question number (1-Accuracy, 2-Graph for Beta values from 0.000001 to 1, 3 - Print Top 100 words)!!");
				} else {
					System.err.println("Invalid option -Q!!");
					System.err.println("-Q or -question should be followed by valid question number (1-Accuracy, 2-Graph for Beta values from 0.000001 to 1, 3 - Print Top 100 words)!!");
					System.exit(0);
				}
				i += 1;
			} if(args[i].equalsIgnoreCase("-S") || args[i].equalsIgnoreCase("-stop_words")) {
				clean = true;
			} 
		}

		if(QNO == 1) {
			if(clean)
				train = new Train(true);
			else
				train = new Train();
			data = train.train();
			test = new Test(data);
			float acc = test.test();
			System.out.println("\nAccuracy: "+acc+"%."); 
		} else if(QNO == 2) {
			int c = 0;
			float min = 100;
			float max = 0;
			double [][] BetaVSAccuracy = new double[2][12];
			if(clean)
				train = new Train(true);
			else
				train = new Train();
			data = train.train();
			DecimalFormat df = new DecimalFormat("#.######");
			df.setMaximumFractionDigits(6); 
			for(double f=0.00001;f<=1;f*=10){
				test = new Test(data, f/2);
				float acc = test.test();
				BetaVSAccuracy[0][c] = Math.log10(f/2);
				BetaVSAccuracy[1][c++] = acc;
				System.out.println("Beta: "+df.format(f/2)+"\t Accuracy: "+acc);
				if(max < acc)
					max = acc;
				else if(min > acc)
					min = acc;
				test = new Test(data, f);
				acc = test.test();
				BetaVSAccuracy[0][c] = Math.log10(f);
				BetaVSAccuracy[1][c++] = acc;
				System.out.println("Beta: "+df.format(f)+" Accuracy"+acc);
				if(max < acc)
					max = acc;
				else if(min > acc)
					min = acc;
			}
			Utils.draw_graph(BetaVSAccuracy, min-1, max+1); 
		} else if(QNO == 3) {
			int count = 0;
			train = new Train(true);
			data = train.train();
			List<String> top_words = Utils.top_words(100, data, data.vocab_size);
			for(String s : top_words) {
				System.out.print(s+", ");
				if(++count % 10 == 0) 
					System.out.println(); 
			}
		}
	}
}
