package com.ksoft.btx;

import java.util.ArrayList;
import java.util.Iterator;

class MemoryBTXObject implements BTXObject {
	protected final String name;
	protected final ArrayList<BTXAttribute> attrs = new ArrayList<>();
	protected final ArrayList<BTXObject> children = new ArrayList<>();
	
	MemoryBTXObject(String name) {
		this.name = name;
	}
	
	@Override
	public int getAttributeCount() {
		return attrs.size();
	}
	
	@Override
	public int getChildrenCount() {
		return children.size();
	}
	
	@Override
	public Iterator<BTXObject> getChildren() {
		return children.iterator();
	}
	
	@Override
	public Iterator<BTXAttribute> getAttributes() {
		return attrs.iterator();
	}
	
}
