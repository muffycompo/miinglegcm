package com.maomuffy.miingleutilities;

import android.content.Context;

public class CategoryData {
	
	private String categoryName;
	private Integer categoryID;
	
	public CategoryData(Context ct){
		// Just in case I need to use the context
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public Integer getCategoryID() {
		return categoryID;
	}

	public void setCategoryID(Integer categoryID) {
		this.categoryID = categoryID;
	}

	
	
	
	
	
}
