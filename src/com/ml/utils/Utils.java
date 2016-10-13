package com.ml.utils;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;

import com.ml.beans.TrainData;

/**
* This class contains all the methods which are commonly used through out the program.
*/
public class Utils {

	/**
	 * This method computes the probability for the given values
	 * Input: attribute count and total count
	 * Output: probability of an attribute
	 */		
	public static float probability(int a, int t) {
		return a*1f/t;
	}

	/**
	 * This method computes the entropy for the given pos and neg values
	 * Input: +, - values
	 * Output: entropy value
	 */
	public static float entropy(int pos, int neg) {
		float entropy = 0f;
		int total = pos+neg;
		if(pos > 0) {
			float p_pos = probability(pos, total);
			entropy -= (p_pos*Math.log(p_pos))/Math.log(2);
		} if(neg > 0) {
			float p_neg = probability(neg, total);
			entropy -= (p_neg*Math.log(p_neg))/Math.log(2);
		}
		return entropy;
	}

	/**
	 * This method computes the information gain using entropy for the given values
	 * Input: +, - counts of all sub nodes of a property-type
	 * Output: information gain of a property-type
	 */	
	public static float info_gain(float s_entropy, Map<Character, int[]> elmMap, int total) {
		float gain = s_entropy;
		for(char elm : elmMap.keySet()) {
			int pos = elmMap.get(elm)[0];
			int neg = elmMap.get(elm)[1];
			gain -= (pos+neg)*entropy(pos, neg)/total;
		}
		return gain;
	}

	/**
	 * This method assign ranks to vocabulary and return top 100 words list based on rank
	 * Input: top count, train data, vocabulary length
	 * Output: List of top 100 words
	 */	
	public static List<String> top_words(int top_count, TrainData data, int vocab_length) {
		List<String> l_top_words = new ArrayList<>(top_count);
		//checking if the input lest has less words then top count.
		if(top_count >= vocab_length) {
			l_top_words.addAll(Constants.vocab_map.values());
		} else {
			Map<Integer, Double> map = new HashMap<>(vocab_length);
			float word_prob;
			//Calculating info gain for each word and storing in a map
			for(int i=1;i<=data.vocab_size;i++) {
				int sum = 0;
				for(int j=0;j<data.cat_size;j++) {
					sum += data.getWord_cat_count(i, j);
				}
				word_prob = probability(sum, vocab_length);
				double w = 0;
				for(int k=0;k<data.cat_size;k++) {
					w += word_prob*(Math.log(sum) - (data.getWord_cat_count(i, k)*data.getCat_prob()[k])*sum/Math.log(data.getWord_cat_count(i, k)));
				}
				map.put(i, w);
			}
			//Sorting list based on the information gain
			Map<Integer, Double> result = sortMapByValue(map);
			int count = 0;
			//Reading first 100 values from the sorted list
			for(int i : result.keySet()) {
				if(Constants.vocab_map.get(i) != null) {
					l_top_words.add(Constants.vocab_map.get(i)); 
					if(++count > 99)
						break;
				}
			}
		}
		return l_top_words;
	}

	/**
	 * This method sorts a hashmap based on values
	 * Input: HashMap object
	 * Output: Sorted TreeMap object - sorted based on value
	 */	
	public static TreeMap<Integer, Double> sortMapByValue(Map<Integer, Double> map){
		Comparator<Integer> comparator = new ValueComparator(map); 
		//TreeMap is a map sorted by its keys. 
		//The comparator is used to sort the TreeMap by keys. 
		TreeMap<Integer, Double> result = new TreeMap<>(comparator);
		result.putAll(map);
		return result;
	}

	/**
	 * This method generates the graph for given beta Vs Accuracy values
	 * Input: beta vs accuracy table, min accuracy and max accuracy obtained.
	 * Output: opens a windows with beta vs accuracy graph
	 */	
	public static void draw_graph(double [][] data, float min, float max) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame frame = new JFrame("Charts");

				frame.setSize(600, 400);
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setVisible(true);

				XYDataset ds = createDataset(data);
				JFreeChart chart = ChartFactory.createXYLineChart("Beta Vs Accuracy",
						"log10(beta)", "Accuracy", ds, PlotOrientation.VERTICAL, true, true,
						false);
				XYPlot xyPlot = (XYPlot) chart.getPlot();
				NumberAxis domain = (NumberAxis) xyPlot.getRangeAxis();
				domain.setRange(min, max);
				ChartPanel cp = new ChartPanel(chart);
				frame.getContentPane().add(cp);
			}
		});
	}

	//creating data set for JFreeChart
	private static XYDataset createDataset(double [][] data) {
		DefaultXYDataset ds = new DefaultXYDataset();
		ds.addSeries("Accuracy", data);
		return ds;
	}

	/**
	 * This method performs stemming and removes stop words on given string
	 * Input: String
	 * Output: filtered string
	 */	
	public static String tokenizeStopStem(String input) {
		if(Constants.stop_word_set.isEmpty())
			init_stopwords();
		TokenStream tokenStream = new StandardTokenizer(
				Version.LUCENE_36, new StringReader(input));
		tokenStream = new StopFilter(Version.LUCENE_36, tokenStream, Constants.stop_word_set);
		tokenStream = new PorterStemFilter(tokenStream);
		StringBuilder sb = new StringBuilder();
		CharTermAttribute charTermAttr = tokenStream.getAttribute(CharTermAttribute.class);
		try{
			while (tokenStream.incrementToken()) {
				if (sb.length() > 0) {
					sb.append(" ");
				}
				sb.append(charTermAttr.toString());
			}
		}
		catch (IOException e){
			System.out.println(e.getMessage());
		} 
		return sb.toString();
	}

	//Adding list of stop words to a set
	public static void init_stopwords() {
		Constants.stop_word_set.add("a");Constants.stop_word_set.add("about");Constants.stop_word_set.add("above");
		Constants.stop_word_set.add("after");Constants.stop_word_set.add("again");Constants.stop_word_set.add("against");
		Constants.stop_word_set.add("all");Constants.stop_word_set.add("am");Constants.stop_word_set.add("an");
		Constants.stop_word_set.add("and");Constants.stop_word_set.add("any");Constants.stop_word_set.add("are");
		Constants.stop_word_set.add("as");Constants.stop_word_set.add("at");Constants.stop_word_set.add("be");
		Constants.stop_word_set.add("because");Constants.stop_word_set.add("been");Constants.stop_word_set.add("before");
		Constants.stop_word_set.add("being");Constants.stop_word_set.add("below");Constants.stop_word_set.add("between");
		Constants.stop_word_set.add("both");Constants.stop_word_set.add("but");Constants.stop_word_set.add("by");
		Constants.stop_word_set.add("could");Constants.stop_word_set.add("did");Constants.stop_word_set.add("do");
		Constants.stop_word_set.add("does");Constants.stop_word_set.add("doing");Constants.stop_word_set.add("down");
		Constants.stop_word_set.add("during");Constants.stop_word_set.add("each");Constants.stop_word_set.add("few");
		Constants.stop_word_set.add("for");Constants.stop_word_set.add("from");Constants.stop_word_set.add("further");
		Constants.stop_word_set.add("had");Constants.stop_word_set.add("has");Constants.stop_word_set.add("have");
		Constants.stop_word_set.add("having");Constants.stop_word_set.add("he");Constants.stop_word_set.add("he'd");
		Constants.stop_word_set.add("he'll");Constants.stop_word_set.add("he's");Constants.stop_word_set.add("her");
		Constants.stop_word_set.add("here");Constants.stop_word_set.add("here's");Constants.stop_word_set.add("hers");
		Constants.stop_word_set.add("herself");Constants.stop_word_set.add("him");Constants.stop_word_set.add("himself");
		Constants.stop_word_set.add("his");Constants.stop_word_set.add("how");Constants.stop_word_set.add("how's");
		Constants.stop_word_set.add("i");Constants.stop_word_set.add("i'd");Constants.stop_word_set.add("i'll");
		Constants.stop_word_set.add("i'm");Constants.stop_word_set.add("i've");Constants.stop_word_set.add("if");
		Constants.stop_word_set.add("in");Constants.stop_word_set.add("into");Constants.stop_word_set.add("is");
		Constants.stop_word_set.add("it");Constants.stop_word_set.add("it's");Constants.stop_word_set.add("its");
		Constants.stop_word_set.add("itself");Constants.stop_word_set.add("let's");Constants.stop_word_set.add("me");
		Constants.stop_word_set.add("more");Constants.stop_word_set.add("most");Constants.stop_word_set.add("my");
		Constants.stop_word_set.add("myself");Constants.stop_word_set.add("no");Constants.stop_word_set.add("nor");
		Constants.stop_word_set.add("of");Constants.stop_word_set.add("off");Constants.stop_word_set.add("on");
		Constants.stop_word_set.add("once");Constants.stop_word_set.add("only");Constants.stop_word_set.add("or");
		Constants.stop_word_set.add("other");Constants.stop_word_set.add("ought");Constants.stop_word_set.add("our");
		Constants.stop_word_set.add("ours");Constants.stop_word_set.add("ourselves");Constants.stop_word_set.add("out");
		Constants.stop_word_set.add("over");Constants.stop_word_set.add("own");Constants.stop_word_set.add("same");
		Constants.stop_word_set.add("she");Constants.stop_word_set.add("she'd");Constants.stop_word_set.add("she'll");
		Constants.stop_word_set.add("she's");Constants.stop_word_set.add("should");Constants.stop_word_set.add("so");
		Constants.stop_word_set.add("some");Constants.stop_word_set.add("such");Constants.stop_word_set.add("than");
		Constants.stop_word_set.add("that");Constants.stop_word_set.add("that's");Constants.stop_word_set.add("the");
		Constants.stop_word_set.add("their");Constants.stop_word_set.add("theirs");Constants.stop_word_set.add("them");
		Constants.stop_word_set.add("themselves");Constants.stop_word_set.add("then");Constants.stop_word_set.add("there");
		Constants.stop_word_set.add("there's");Constants.stop_word_set.add("these");Constants.stop_word_set.add("they");
		Constants.stop_word_set.add("they'd");Constants.stop_word_set.add("they'll");Constants.stop_word_set.add("they're");
		Constants.stop_word_set.add("they've");Constants.stop_word_set.add("this");Constants.stop_word_set.add("those");
		Constants.stop_word_set.add("through");Constants.stop_word_set.add("to");Constants.stop_word_set.add("too");
		Constants.stop_word_set.add("under");Constants.stop_word_set.add("until");Constants.stop_word_set.add("up");
		Constants.stop_word_set.add("very");Constants.stop_word_set.add("was");Constants.stop_word_set.add("we");
		Constants.stop_word_set.add("we'd");Constants.stop_word_set.add("we'll");Constants.stop_word_set.add("we're");
		Constants.stop_word_set.add("we've");Constants.stop_word_set.add("were");Constants.stop_word_set.add("what");
		Constants.stop_word_set.add("what's");Constants.stop_word_set.add("when");Constants.stop_word_set.add("when's");
		Constants.stop_word_set.add("where");Constants.stop_word_set.add("where's");Constants.stop_word_set.add("which");
		Constants.stop_word_set.add("while");Constants.stop_word_set.add("who");Constants.stop_word_set.add("who's");
		Constants.stop_word_set.add("whom");Constants.stop_word_set.add("why");Constants.stop_word_set.add("why's");
		Constants.stop_word_set.add("with");Constants.stop_word_set.add("won't");Constants.stop_word_set.add("would");
		Constants.stop_word_set.add("you");Constants.stop_word_set.add("you'd");Constants.stop_word_set.add("you'll");
		Constants.stop_word_set.add("you're");Constants.stop_word_set.add("you've");Constants.stop_word_set.add("your");
		Constants.stop_word_set.add("yours");Constants.stop_word_set.add("yourself");Constants.stop_word_set.add("yourselves");	
	}

}
