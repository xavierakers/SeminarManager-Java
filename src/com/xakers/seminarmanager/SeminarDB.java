package com.xakers.seminarmanager;

import java.io.File;
import java.util.Scanner;

import com.xakers.seminarmanager.hashtable.HashTable;
import com.xakers.seminarmanager.memorypool.MemManager;
import com.xakers.seminarmanager.memorypool.MemManager.MemHandle;
import com.xakers.seminarmanager.seminar.Seminar;

/**
 * SeminarDB is responsible for managing a database of seminar records, handling
 * insertion, deletion, search, and printing operations. It interfaces with a
 * memory manager (MemManager) and a hash table (HashTable) to store seminar
 * data in memory and provide efficient access and manipulation.
 * 
 * @author Xavier Akers
 * @version 1.0
 * @since 2023-08-24
 */
public class SeminarDB {
	// Memory pool for storing seminar data
	private MemManager memoryPool;
	// Hash table to store seminar data
	private HashTable<MemHandle> table;

	/**
	 * Initializes the memory pool and hash table for storing and managing seminar
	 * records.
	 * 
	 * @param memSize  The size of the memory pool to allocate for storing seminar
	 *                 records.
	 * @param hashSize The size of the hash table used to index the seminar records.
	 */
	public SeminarDB(int memSize, int hashSize) {
		this.memoryPool = new MemManager(memSize);
		this.table = new HashTable<MemHandle>(hashSize);
	}

	/**
	 * Loads seminar data from a specified file.
	 * 
	 * @param filename The name of the file containing seminar records and commands.
	 */
	public void load(String filename) {
		try {
			// Open the file for reading
			Scanner scanner = new Scanner(new File(filename));

			// Process each line in the file
			while (scanner.hasNextLine()) {
				String[] command = scanner.nextLine().trim().split("\\s+");

				// Execute different commands
				switch (command[0]) {
				case "insert": {
					// Insert new seminar
					int id = Integer.parseInt(command[1]);
					String title = scanner.nextLine().trim();
					String[] logistics = scanner.nextLine().trim().split("\\s+");
					String[] keywords = scanner.nextLine().trim().split("\\s+");
					String desc = scanner.nextLine().trim();

					// Parse logistics data
					String date = logistics[0];
					int length = Integer.parseInt(logistics[1]);
					short x = Short.parseShort(logistics[2]);
					short y = Short.parseShort(logistics[3]);
					int cost = Integer.parseInt(logistics[4]);

					// Create a new seminar object
					Seminar seminar = new Seminar(id, title, date, length, x, y, cost, keywords, desc);

					// Try inserting the seminar
					int result = insert(id, seminar);
					if (result == 0) {
						System.out.printf("Insert FAILED - There is already a record with ID %d\n", id);
					} else {
						System.out.printf("Successfully inserted record with ID %d\n", id);
						System.out.printf("%s\n", seminar.toString());
						System.out.printf("Size: %d\n", result);
					}

					break;
				}
				case "delete": {
					// Delete a seminar record by ID
					int id = Integer.parseInt(command[1]);
					MemHandle handle = delete(id);
					if (handle == null) {
						System.out.printf("Delete Failed -- There is no record with ID %d\n", id);
					} else {
						System.out.printf("Record with ID %d successfully deleted from the database\n", id);
					}
					break;
				}
				case "search": {
					// Search for seminar by ID
					int id = Integer.parseInt(command[1]);
					Seminar seminar = search(id);
					if (seminar == null) {
						System.out.printf("Search FAILED -- There is no record with ID %d\n", id);
					} else {
						System.out.printf("%s\n", seminar.toString());
					}
					break;
				}
				case "print": {
					// Print the seminar based on print type
					String printType = command[1];
					print(printType);
					break;
				}
				default: {
					System.out.printf("Invalid command: %s.\n", command[0]);
					break;
				}
				}
			}
			// Close the scanner after processing the file
			scanner.close();
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}

	/**
	 * Inserts a seminar into the hash table and memory pool.
	 * 
	 * @param key Unique identifier for the seminar
	 * @param sem Seminar to insert
	 * @return the size of the seminar memory block if insertion is successful, 0 if
	 *         the seminar with the given key already exists
	 * @throws Exception if there is an error during serialization or memory
	 *                   allocation
	 */
	private int insert(int key, Seminar sem) throws Exception {
		// Check if the seminar with the given key already exists in the hash table
		if (table.search(key) != null) {
			return 0; // Return 0 if the seminar already exists
		}

		// Serialize the seminar and insert
		MemHandle handle = memoryPool.insert(sem.serialize(), sem.serialize().length);

		// Store the memory handle in the hash tale
		table.insert(key, handle);

		// Return the size of the seminar
		return handle.getSize();

	}

	/**
	 * Deletes a seminar from the hash table and memory pool.
	 * 
	 * @param key Unique identifier for the seminar
	 * @return the MemHandle of the deleted seminar, or null if the seminar was not
	 *         found
	 */
	private MemHandle delete(int key) {
		MemHandle handle = table.delete(key);

		// If the seminar is not found, return null
		if (handle == null) {
			return null;
		}

		// Delete the seminar from the memory pool
		memoryPool.delete(handle);

		// Returns seminar metadata
		return handle;
	}

	/**
	 * Searches for a seminar by its unique identifier.
	 * 
	 * @param key Unique identifier for the seminar
	 * @return the Seminar object, or null if the seminar was not found
	 * @throws Exception if there is an error during deserialization
	 */
	private Seminar search(int key) throws Exception {
		// Search for existing MemHandle
		MemHandle handle = table.search(key);

		// If the seminar is not found
		if (handle == null) {
			return null;
		}
		// Retrieve serialize seminar data
		byte[] seminarData = memoryPool.get(handle);

		// Deserialize seminar data
		return Seminar.deserialize(seminarData);
	}

	/**
	 * Prints the data of the hash table or memory blocks based on the print type.
	 * 
	 * @param printType the type of data to print ("hashtable" or "blocks")
	 */
	private void print(String printType) {
		switch (printType) {
		case "hashtable": {
			// Print the hash table
			table.printHashTable();
			break;
		}
		case "blocks": {
			// Dump the memory pool blocks (memory usage)
			memoryPool.dump();
			break;
		}
		default: {
			System.out.printf("Unknown print type: %s\n", printType);
			break;
		}
		}

	}
}
