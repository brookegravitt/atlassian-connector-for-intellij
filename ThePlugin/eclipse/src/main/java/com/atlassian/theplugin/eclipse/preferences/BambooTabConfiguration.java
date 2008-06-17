package com.atlassian.theplugin.eclipse.preferences;

import java.util.ArrayList;
import java.util.List;

public class BambooTabConfiguration {
	
	int[] columnsOrder;
	List<Integer> columnsWidth = new ArrayList<Integer>();

	
	public int[] getColumnsOrder() {
		return columnsOrder;
	}
	
	public void setColumnsOrder(int[] order) {
		columnsOrder = order;
	}


	public String getColumnsOrderString() {
		StringBuilder ret = new StringBuilder();
		
		for (Integer column : columnsOrder) {
			ret.append(column + " ");
		}
		
		return ret.toString();
	}
	
	public void setColumnsOrderString(String columnsOrder) {

		String[] order = columnsOrder.split(" ");
		
		List<Integer> columnsOrderList = new ArrayList<Integer>(order.length);
		
		for (String column : order) {
			if (column != null && column.length() > 0) {
				try {
					columnsOrderList.add(Integer.valueOf(column));
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
		}
		
		// copy ArrayList to the simple array (size can be smaller than order.lenght)
		this.columnsOrder = new int[columnsOrderList.size()];
		
		for (int i = 0 ; i < columnsOrderList.size() ; ++i) {
			this.columnsOrder[i] = columnsOrderList.get(i);
		}
		
	}

	public List<Integer> getColumnsWidth() {
		return columnsWidth;
	}

	public void setColumnsWidth(List<Integer> columnsWidth) {
		this.columnsWidth = columnsWidth;
	}

	public String getColumnsWidthString() {
		StringBuilder ret = new StringBuilder();
		
		for (Integer column : columnsWidth) {
			ret.append(column + " ");
		}
		
		return ret.toString();
	}

	public void setColumnsWidthString(String columnsWidth) {
		this.columnsWidth.clear();
		
		String[] order = columnsWidth.split(" ");
		for (String column : order) {
			if (column != null && column.length() > 0) {
				
				try {
					this.columnsWidth.add(Integer.valueOf(column));
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
}
