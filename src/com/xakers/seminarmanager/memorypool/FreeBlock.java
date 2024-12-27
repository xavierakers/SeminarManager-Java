package com.xakers.seminarmanager.memorypool;

public class FreeBlock {
	private int position;
	private int size;
	private FreeBlock next;
	private FreeBlock prev;

	public FreeBlock(int position, int size) {
		this.position = position;
		this.size = size;
		this.next = null;
		this.prev = null;
	}

	/**
	 * @return the next
	 */
	public FreeBlock getNext() {
		return next;
	}

	/**
	 * @param next the next to set
	 */
	public void setNext(FreeBlock next) {
		this.next = next;
	}

	/**
	 * @return the prev
	 */
	public FreeBlock getPrev() {
		return prev;
	}

	/**
	 * @param prev the prev to set
	 */
	public void setPrev(FreeBlock prev) {
		this.prev = prev;
	}

	/**
	 * @return the position
	 */
	public int getPosition() {
		return position;
	}

	/**
	 * @return the size
	 */
	public int getSize() {
		return size;
	}

}
