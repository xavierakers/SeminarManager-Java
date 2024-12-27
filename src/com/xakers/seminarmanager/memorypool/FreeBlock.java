package com.xakers.seminarmanager.memorypool;

/**
 * Represents a free memory node in a memory pool. Used for managing free
 * memory regions as a doubly linked list.
 */
public class FreeBlock {
	private int position; // Starting position of the memory block
	private int size; // Size of the memory block
	private FreeBlock next; // Reference to the next free block
	private FreeBlock prev; // Reference to the previous free block

	/**
	 * Constructs a free memory block with the specified position and size.
	 * 
	 * @param position The starting position of the memory block.
	 * @param size     The size of the memory block.
	 */
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
