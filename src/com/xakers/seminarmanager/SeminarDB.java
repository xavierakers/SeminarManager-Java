package com.xakers.seminarmanager;

import java.io.File;
import java.util.Scanner;

import com.xakers.seminarmanager.hashtable.HashTable;
import com.xakers.seminarmanager.memorypool.MemManager;
import com.xakers.seminarmanager.memorypool.MemManager.MemHandle;
import com.xakers.seminarmanager.seminar.Seminar;

/**
 * @author Xavier Akers
 * 
 * @version 1.0
 * 
 * @since 2023-08-24
 * 
 *        Handles communication between the CommandProcessor, HashTable, and
 *        MemManager
 * 
 */
public class SeminarDB {
	private MemManager memoryPool;
	private HashTable<MemHandle> table;

	public SeminarDB(int memSize, int hashSize) {
		this.memoryPool = new MemManager(memSize);
		this.table = new HashTable<MemHandle>(hashSize);
	}

	public void load(String filename) {
		try {
			Scanner scanner = new Scanner(new File(filename));
			while (scanner.hasNextLine()) {
				String[] command = scanner.nextLine().trim().split("\\s+");
				switch (command[0]) {
				case "insert": {
					int id = Integer.parseInt(command[1]);
					String title = scanner.nextLine().trim();
					String[] logistics = scanner.nextLine().trim().split("\\s+");
					String[] keywords = scanner.nextLine().trim().split("\\s+");
					String desc = scanner.nextLine().trim();

					String date = logistics[0];
					int length = Integer.parseInt(logistics[1]);
					short x = Short.parseShort(logistics[2]);
					short y = Short.parseShort(logistics[3]);
					int cost = Integer.parseInt(logistics[4]);

					Seminar seminar = new Seminar(id, title, date, length, x, y, cost, keywords, desc);
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
					String printType = command[1];
					print(printType);
					break;
				}
				}
			}
			scanner.close();
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}

	private int insert(int key, Seminar sem) throws Exception {
		if (table.search(key) != null) {
			return 0;
		}

		MemHandle handle = memoryPool.insert(sem.serialize(), sem.serialize().length);
		table.insert(key, handle);
		return handle.getSize();

	}

	private MemHandle delete(int key) {
		MemHandle handle = table.delete(key);
		if (handle == null) {
			return null;
		}
		memoryPool.delete(handle);
		return handle;
	}

	private Seminar search(int key) throws Exception {
		MemHandle handle = table.search(key);
		if (handle == null) {
			return null;
		}
		byte[] seminarData = memoryPool.get(handle);
		return Seminar.deserialize(seminarData);
	}

	private void print(String printType) {
		switch (printType) {
		case "hashtable": {
			table.printHashTable();
			break;
		}
		case "blocks": {
			memoryPool.dump();
			break;
		}
		}

	}
}
