package org.null0.edi.debatcher;

public class Segment {

	private String id;
	private String line;
	private int count;

	public Segment(boolean create) {
		this("", "", 0);
	}

	public Segment(String id, String line, int count) {
		this.id = id;
		this.line = line;
		this.count = count;
	}

	// Id of the segment e.g. ISA
	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	// Contains the details of the segment after the segment id from * to ~(end delimiter)
	public String getLine() {
		return this.line;
	}

	public void setLine(String name) {
		this.line = name;
	}

	// Count for the elements
	public int getCount() {
		return this.count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public void set(int i, String string) {
		// TODO Auto-generated method stub

	}

}
