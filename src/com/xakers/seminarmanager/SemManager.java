package com.xakers.seminarmanager;

import com.xakers.seminarmanager.seminar.Seminar;

/**
 * A Seminar Manager utilizing closed hash tables and a memory pools
 */

/**
 * The class containing the main method.
 *
 * @author Xavier Akers
 * @version Last Updated
 */
public class SemManager {
	/**
	 * @param args Command line parameters
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		Seminar dum = new Seminar();

		if (args.length < 3) {
			System.out.println("command usage : {initial-memory-size} {initial-hash-size} {command-file}");
			System.exit(0);
		}	
		int poolSize = Integer.parseInt(args[0]);
		int hashSize = Integer.parseInt(args[1]);
		String filename = args[2];
		
		SeminarDB db = new SeminarDB(poolSize, hashSize);
		db.load(filename);
	}

}
