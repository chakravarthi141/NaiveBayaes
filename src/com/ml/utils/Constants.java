package com.ml.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public interface Constants {

	Map<Integer, String> category_map = new HashMap<>();
	Map<Integer, String> vocab_map = new HashMap<>();
	
	int THREAD_COUNT = 8;
	Set<String> stop_word_set = new HashSet<>();
}
