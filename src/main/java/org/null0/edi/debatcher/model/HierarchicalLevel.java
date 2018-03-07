package org.null0.edi.debatcher.model;

public class HierarchicalLevel {

	private int id;
	private int parentId;
	private int levelCode;
	private int childcode;
	private StringBuffer hlDataBuffer = new StringBuffer();

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getParentId() {
		return parentId;
	}

	public void setParentId(int parentId) {
		this.parentId = parentId;
	}

	public int getLevelCode() {
		return levelCode;
	}

	public void setLevelCode(int levelCode) {
		this.levelCode = levelCode;
	}

	public int getChildcode() {
		return childcode;
	}

	public void setChildcode(int childcode) {
		this.childcode = childcode;
	}

	public StringBuffer getHlDataBuffer() {
		return hlDataBuffer;
	}

	public void setHlDataBuffer(StringBuffer hlDataBuffer) {
		this.hlDataBuffer = hlDataBuffer;
	}

}
