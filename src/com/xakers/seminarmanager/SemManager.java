package com.xakers.seminarmanager;

/**
 * A Seminar Manager utilizing closed hash tables and memory pools
 * This class serves as the entry point to the seminar management system.
 */

/**
 * The class containing the main method to initialize and load the seminar
 * database.
 *
 * @author Xavier Akers
 * @version 1.0, 2024-12-27
 * @since 2024-12-26
 */
public class SemManager {
	/**
	 * The main method which is the entry point of the program.
	 *
	 * @param args Command line parameters: 1. initial-memory-size (size of the
	 *             memory pool) 2. initial-hash-size (size of the hash table) 3.
	 *             command-file (file containing commands to load)
	 */
	public static void main(String[] args) {
		if (args.length < 3) {
			System.out.println("command usage : {initial-memory-size} {initial-hash-size} {command-file}");
			System.exit(0);
		}

		int poolSize = Integer.parseInt(args[0]);
		int hashSize = Integer.parseInt(args[1]);
		String filename = args[2];

		// Initialize SeminarDB
		SeminarDB db = new SeminarDB(poolSize, hashSize);

		// Load the commands from file
		db.load(filename);
	}

}
