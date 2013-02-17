package com.ksoft.btx;

import java.util.Iterator;

public interface BTXObject {
	int getAttributeCount();
	int getChildrenCount();
	
	Iterator<BTXObject> getChildren();
	Iterator<BTXAttribute> getAttributes();
}
