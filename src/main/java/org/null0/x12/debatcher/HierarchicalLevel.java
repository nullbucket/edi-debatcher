package org.null0.x12.debatcher;

class HierarchicalLevel {
	private int childcode;
	private StringBuffer hlDataBuffer = new StringBuffer();
	private int id;
	private int levelCode;
	private int parentId;

	public int getChildcode() {
		return childcode;
	}

	public StringBuffer getHlDataBuffer() {
		return hlDataBuffer;
	}

	public int getId() {
		return id;
	}

	public int getLevelCode() {
		return levelCode;
	}

	public int getParentId() {
		return parentId;
	}

	public void setChildcode(int childcode) {
		this.childcode = childcode;
	}

	public void setHlDataBuffer(StringBuffer hlDataBuffer) {
		this.hlDataBuffer = hlDataBuffer;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setLevelCode(int levelCode) {
		this.levelCode = levelCode;
	}

	public void setParentId(int parentId) {
		this.parentId = parentId;
	}
}
