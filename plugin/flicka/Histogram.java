package plugin.flicka;

import java.util.*;

/**
 * lazy histogram implementation
 * 
 * @author terry
 *
 */
public class Histogram extends TreeMap<Double, Double> {

	/**
	 * Equal to original implementation but return 0.0 if the mapped value don't exist.
	 * 
	 * @return the value to which the specified key is mapped, or 0.0 if this map contains no mapping for the key
	 * 
	 */
	@Override
	public synchronized Double get(Object key) {
		Double obj = super.get(key);
		return obj == null ? 0.0 : obj;
	}

	/**
	 * increment the value by 1 to which specify key map. this metod reduce the presicion of double value to 4 decimal
	 * digits
	 * 
	 * @param key the key whose associated value will be incremented
	 */
	public void increment(double key) {
		double kd = round(key);
		double ov = get(kd);
		ov = round(ov);
		ov++;
		put(kd, ov);
	}
	
	private double round(double ov) {
		int tmp = (int) (ov * 10000);
		return ((double) tmp) / 10000;		
	}
}
