package com.xakers.seminarmanager.hashtable;

/**
 * A dynamic closed hash table implementation that utilizes double hashing for
 * collision resolution.
 * 
 * @param <T> The type of elements stored in the hash table.
 * 
 * @author Xavier Akers
 * @version 1.0, 2024-12-27
 * @since 2024-12-26
 */
public class HashTable<T> {
	/**
	 * Marker to represent deleted records in the hash table.
	 */
	final Record<T> TOMBSTONE = new Record<T>(-1, null);
	private Record<T>[] table;
	private int currSize; // Current number of elements in the hash table
	private int maxSize; // Maximum capacity of the hash table
	private int threshold; // Threshold to trigger resizing (half the table size)

	/**
	 * Constructs a hash table with a specified initial size.
	 * 
	 * @param hashSize The initial size of the hash table, must be a power of two.
	 */
	@SuppressWarnings("unchecked")
	public HashTable(int hashSize) {
		this.maxSize = hashSize;
		this.table = (Record<T>[]) new Record[hashSize];
		this.currSize = 0;
		this.threshold = (int) (this.maxSize / 2);

	}

	/**
	 * Inserts a new record into the hash table using double hashing for collision
	 * resolution.
	 * 
	 * @param id     The key associated with the record.
	 * @param handle The value associated with the record.
	 * @return true if the record was successfully inserted; false if the key
	 *         already exists.
	 */
	public boolean insert(int id, T handle) {
		// Check if resizing is needed
		if (this.currSize == threshold) {
			resize();
		}

		Record<T> record = new Record<T>(id, handle);
		int index = hash(record.getKey());
		int stepSize = doubleHash(record.getKey());

		// Check if the key already exists
		if (search(record.getKey()) != null) {
			return false;
		}

		// Find the next available position using double hashing
		while (table[index] != null && table[index] != TOMBSTONE) {
			index = (index + stepSize) % this.maxSize;
		}

		// Insert the new record
		table[index] = record;
		this.currSize++;

		return true;

	}

	/**
	 * Searches the hash table for the specified key.
	 * 
	 * @param id The key to search for, representing a handle ID.
	 * @return The value associated with the key, or null if not found.
	 */
	public T search(int id) {
		int index = hash(id);
		int stepSize = doubleHash(id);

		// Traverse the table using double hashing until an empty slot is found
		while (table[index] != null) {
			if (table[index].getKey() == id) {
				return table[index].getValue(); // Key found, return associated value
			}
			index = (index + stepSize) % this.maxSize; // Probe the next position
		}
		return null;
	}

	/**
	 * Deletes the record associated with the specified key from the hash table.
	 * 
	 * @param key The key of the record to delete.
	 * @return The value of the deleted record, or null if the key is not found.
	 */
	public T delete(int key) {
		int index = hash(key);
		int stepSize = doubleHash(key);

		// Traverse the table using double hashing until an empty slot is found
		while (table[index] != null) {
			if (table[index].getKey() == key) {
				Record<T> record = table[index];
				table[index] = TOMBSTONE; // Mark slot as deleted
				this.currSize--; // Decrement the size of the table
				return record.getValue(); // Return the value of the deleted record
			}
			index = (index + stepSize) % this.maxSize; // Probe the next position
		}
		return null; // Key not found
	}

	/**
	 * Prints all contents of the hash table by iterating through each index.
	 */
	public void printHashTable() {
		System.out.printf("Hashtable:\n");
		for (int i = 0; i < this.table.length; i++) {
			Record<T> record = table[i];
			if (record == null) {
				continue; // Skip empty slots
			}

			// Print 'TOMBSTONE' for deleted records
			if (record == TOMBSTONE) {
				System.out.printf("%d: TOMBSTONE\n", i);
			} else {
				System.out.printf("%d: %d\n", i, record.getKey()); // Print value records
			}

		}
		System.out.printf("total records: %d\n", this.currSize); // Print total active records
	}

	/**
	 * Doubles the size of the hash table and rehashes all active records.
	 */
	private void resize() {
		// Double capacity and update threshold
		this.maxSize *= 2;
		this.threshold = (int) (this.maxSize / 2);

		// Create new table with updated size
		@SuppressWarnings("unchecked")
		Record<T>[] newTable = (Record<T>[]) new Record[this.maxSize];
		for (Record<T> value : this.table) {
			if (value != null && value != TOMBSTONE) { // Rehash only active records
				int index = hash(value.getKey());
				int stepSize = doubleHash(value.getKey());
				while (newTable[index] != null) {
					index = (index + stepSize) % this.maxSize; // Find the next available slot
				}
				newTable[index] = value;
			}
		}
		// Replace the old table
		table = newTable;
		System.out.printf("Hash table expanded to %d records\n", this.maxSize);
	}

	/**
	 * Hash function to calculate the initial index for a given key.
	 * 
	 * @param key The key to hash.
	 * @return The hashed index.
	 */
	private int hash(int key) {
		// Modulo operation to map the key within table size
		return key % this.maxSize;
	}

	/**
	 * Double hashing function to calculate the step size for collision resolution.
	 * 
	 * @param key The key to hash.
	 * @return The step size for probing.
	 */
	private int doubleHash(int key) {
		// Ensures step size is always odd and non-zero
		return (((int) (key / this.maxSize) % (this.maxSize / 2)) * 2) + 1;
	}

}
