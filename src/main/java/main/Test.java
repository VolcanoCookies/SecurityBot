package main;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Test {
	
	public static void main(String[] args) {
		Map<Integer, String> map = new ConcurrentHashMap<>();
		
		map.put(123, "test");
		map.put(432, "test2");
		map.put(123, "ddd");
		
		System.out.println(map.size());
		
	}
}
