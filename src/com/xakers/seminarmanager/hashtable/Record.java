package com.xakers.seminarmanager.hashtable;

/**
 * Represents a key-value pair stored in the hash table.
 *
 * @param <T> The type of the value associated with the key.
 * 
 * @author Xavier Akers
 * @version 1.0, 2024-12-27
 * @since 2024-12-26
 */
public class Record<T> {
	private int key; // Record identifier
	private T value; // Value with the key

	/**
	 * Constructs a record with the specified key and value.
	 * 
	 * @param key   Identifier for the record.
	 * @param value Value with the key.
	 */
	public Record(int key, T value) {
		this.key = key; // Unique identifier for the record
		this.value = value; // Value for the key
	}

	/**
	 * @return the key
	 */
	public int getKey() {
		return key;
	}

	/**
	 * @param key the key to set
	 */
	public void setKey(int key) {
		this.key = key;
	}

	/**
	 * @return the value
	 */
	public T getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(T value) {
		this.value = value;
	}
}
