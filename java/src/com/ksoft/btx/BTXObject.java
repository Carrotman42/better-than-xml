package com.ksoft.btx;

import java.util.Iterator;

public interface BTXObject {
	String getName();
	int getAttributeCount();
	int getChildrenCount();
	
	Iterator<BTXObject> getChildren();
	Iterator<BTXAttribute> getAttributes();
}
