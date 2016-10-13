package com.ml.utils;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * This is a comparator used to sort tree map based on the value
 */	
class ValueComparator implements Comparator<Integer>{
	 
	HashMap<Integer, Double> map = new HashMap<Integer, Double>();
 
	public ValueComparator(Map<Integer, Double> map2){ 
		this.map.putAll(map2);
	}
 
	@Override
	public int compare(Integer i1, Integer i2) {
		if(Double.compare(map.get(i1), map.get(i2)) > 0){
			return -1;
		}else{
			return 1;
		}	
	}
}
