package com.xakers.seminarmanager.hashtable;

import com.xakers.seminarmanager.memorypool.MemManager.MemHandle;
import com.xakers.seminarmanager.seminar.Seminar;

public class HashTable<T> {

	final Record<T> TOMBSTONE = new Record<T>(-1, null);
	private Record<T>[] table;
	private int currSize;
	private int maxSize;
	private int threshold;

	/**
	 * Hash Table constructor
	 * 
	 * @param size Number of indices in hash table, a power of two
	 */
	@SuppressWarnings("unchecked")
	public HashTable(int hashSize) {
		// TODO Auto-generated constructor stub
		this.maxSize = hashSize;
		this.table = (Record<T>[]) new Record[hashSize];
		this.currSize = 0;
		this.threshold = (int) (this.maxSize / 2);

	}

	/**
	 * Used to insert a memory handle into the hash table Uses single and double
	 * hashing functionality
	 * 
	 * @param record An object containing reference to the memHandle
	 * @return Returns true if successfully inserted into the hash table
	 */
	public boolean insert(int id, T handle) {
		// Check threshold
		if (this.currSize == threshold) {
			resize();
		}
		Record<T> record = new Record<T>(id, handle);
		int index = hash(record.getKey());
		int stepSize = doubleHash(record.getKey());

		if (search(record.getKey()) != null) {
			return false;
		}

		while (table[index] != null && table[index] != TOMBSTONE) {
			index = (index + stepSize) % this.maxSize;
		}
		table[index] = record;
		this.currSize++;

		return true;

	}

	/**
	 * Searches the hash table for the specified key
	 * 
	 * @param key An integer used for the handle ID
	 * @return MemHandle The handle containing the seminar reference
	 */

	public T search(int id) {
		int index = hash(id);
		int stepSize = doubleHash(id);

		while (table[index] != null) {
			if (table[index].getKey() == id) {
				return table[index].getValue();
			}
			index = (index + stepSize) % this.maxSize;
		}
		return null;
	}

	/**
	 * Deletes the specific seminar reference from the hash table
	 * 
	 * @param key An integer used for the handle ID
	 * @return boolean Returns true if the handle was successfully deleted
	 */
	public T delete(int key) {
		int index = hash(key);
		int stepSize = doubleHash(key);

		while (table[index] != null) {
			if (table[index].getKey() == key) {
				Record<T> record = table[index];
				table[index] = TOMBSTONE;
				this.currSize--;
				return record.getValue();
			}
			index = (index + stepSize) % this.maxSize;
		}
		return null;
	}

	/**
	 * Prints all contents in the hash table Iterates linearly
	 */
	public void printHashTable() {
		System.out.printf("Hashtable:\n");
		for (int i = 0; i < this.table.length; i++) {
			Record<T> record = table[i];
			if (record == null) {
				continue;
			}

			// Only print tombstone records as 'TOMBSTONE', not as valid records
			if (record == TOMBSTONE) {
				System.out.printf("%d: TOMBSTONE\n", i);
			} else {
				System.out.printf("%d: %d\n", i, record.getKey());
			}

		}
		System.out.printf("total records: %d\n", this.currSize);
	}

	/**
	 * Doubles the size of the hash table
	 */
	private void resize() {
		// Double capacity
		this.maxSize *= 2;
		this.threshold = (int) (this.maxSize / 2);

		// Create new table
		@SuppressWarnings("unchecked")
		Record<T>[] newTable = (Record<T>[]) new Record[this.maxSize];
		for (Record<T> value : this.table) {
			if (value != null && value != TOMBSTONE) {
				int index = hash(value.getKey());
				int stepSize = doubleHash(value.getKey());
				while (newTable[index] != null) {
					index = (index + stepSize) % this.maxSize;
				}
				newTable[index] = value;
			}
		}
		table = newTable;
		System.out.printf("Hash table expanded to %d records\n", this.maxSize);
	}

	private int hash(int key) {
		return key % this.maxSize;
	}

	private int doubleHash(int key) {
		return (((int) (key / this.maxSize) % (this.maxSize / 2)) * 2) + 1;
	}

}
