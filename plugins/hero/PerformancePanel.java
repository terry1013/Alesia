package plugins.hero;

import java.awt.*;
import java.util.*;

import javax.swing.*;

public class PerformancePanel extends JPanel{
	private TreeMap<String, Integer> perfomanceTable;
	
	public PerformancePanel() {
super(new GridLayout(0, 2));
perfomanceTable = new TreeMap<>();
	}
	

	public void setVariable(String name, int value) {
		if(!perfomanceTable.containsKey(name))
		perfomanceTable.put(name, value);
	}
	
}
