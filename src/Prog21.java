/*
 * Authors: Kevin Callaghan
 * Assignment: Program 2
 * Instructor: Lester McCann
 * TAs:  Priya Kaushik and Aayush Pinto
 * Course: CSC 460
 * Written: 21 September 2022
 * 
 * Prog21.java -- Creates in a binary file named lhl.idx a Linear Hashing Lite
 * index for a .bin file. Stores lhl.idx in current directory.
 * 
 * Java Version: jdk-16.0.2
 * Compiation: store complete path to .bin file in args[0]
 */

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class Prog21 {
	
	public static int BUCKET_CAP = 20; //buckets have a capacity of 20 buckets
	
	/*---------------------------------------------------------------------
    |  Method setDataLengths(filename:String)
    |
    |  Purpose: Opens and reads the first three longs of .bin file.
    			The first three longs specify the lengths of the
    				3 String fields in class DataRecord.
    			Returns the three longs in int[].
    				
    |  Pre-condition:  The file filename.bin exists includes path to
    |                  directory, is properly structured, and is readable.
    |
    |  Post-condition: The returned collection of records contains the
    |                  same data as the file does, and in the same order.
    |
    |  Parameters:
    |      fileName -- Includes path and file extension .bin.
    |
    |  Returns: int[3]
    *-------------------------------------------------------------------*/
	public static int[] setDataLengths(String filename) {
		RandomAccessFile binFile = null; // RAF specializes in binary file I/O
		int[] dataLengths = new int[3];
		
		//open the binary file of data for reading and get String lengths
		try {
			binFile = new RandomAccessFile(filename, "r");
			long PNL = binFile.readLong(); dataLengths[0] = (int) PNL;
			long SCL = binFile.readLong(); dataLengths[1] = (int) SCL;
			long SL = binFile.readLong(); dataLengths[2] = (int) SL;
		} catch(IOException e) {
			System.out.println("I/O ERROR: Something went wrong with the "
                    + "opening of the RandomAccessFile object.");
			System.exit(-1);
		}
		return dataLengths;
	}
	
	/*---------------------------------------------------------------------
    |  Method readBinaryFile(fileName)
    |
    |  Purpose:  Opens and reads the content of fileName.bin, and returns
    |            that content to the caller in the form of an ArrayList
    |            of DataRecord objects.
    |
    |  Pre-condition:  The file fileName.bin exists in the current
    |                  directory, is properly structured, and is readable.
    |
    |  Post-condition: The returned collection of records contains the
    |                  same data as the file does, and in the same order.
    |
    |  Parameters:
    |      fileName -- Just the file name of the CSV file, not the 
    |                  file extension.
    |
    |  Returns: An ArrayList of DataRecord objects.
    *-------------------------------------------------------------------*/
	private static ArrayList<DataRecord> readBinaryFile(String filename, int pnl, int scl, int sl, int record_length){
		RandomAccessFile binFile = null; // RAF specializes in binary file I/O
		
		try {
			binFile = new RandomAccessFile(filename, "r");
			//iterate past String lengths at beginning of file
			long PNL = binFile.readLong(); //PROJECT_NAME_LENGTH = (int) PNL;
			long SCL = binFile.readLong(); //SOLAR_COD_LENGTH = (int) SCL;
			long SL = binFile.readLong(); //STATE_LENGTH = (int) SL;
			//RECORD_LENGTH = PROJECT_NAME_LENGTH + SOLAR_COD_LENGTH + STATE_LENGTH + 1*(4) + 5*(8); //int is (4) bytes, double is (8) bytes
			
		} catch(IOException e) {
			System.out.println("I/O ERROR: Something went wrong with the "
                    + "opening of the RandomAccessFile object.");
			System.exit(-1);
		}
		
		// Determine how many records are in the binary file,
        // so that we know how many to read.
		long numberOfRecords = 0; // Quantity of records in the binary file
		try {
			numberOfRecords = binFile.length() / record_length;
		} catch (IOException e) {
			System.out.println("I/O ERROR: Couldn't get the file's length.");
			System.exit(-1);
		}
		
		// Move the file pointer (which marks the byte with which
        // the next access will begin)
		try {
			binFile.seek(binFile.getFilePointer());
		} catch (IOException e) {
			System.out.println("I/O ERROR: Seems we can't reset the file "
                    + "pointer to the start of the file.");
			System.exit(-1);
		}
		
		// Read the records from the binary file into an
        // in-memory data structure, for return to the caller.
		ArrayList<DataRecord> binContent = new ArrayList<DataRecord>(); //holds binary records
		for (int i = 0; i < numberOfRecords; i++) {
			DataRecord record = new DataRecord(pnl, scl, sl); //create object to hold record
			record.fetchObject(binFile);
			binContent.add(record);
		}
		
		//Reading is complete; close the binary file.
		try {
            binFile.close();
        } catch (IOException e) {
            System.out.println("VERY STRANGE I/O ERROR: Couldn't close "
                             + "the binary file after reading!");
            System.exit(-1);
        }
		
		return binContent;
	} // readBinaryFile()
	
	
	private static void writeBinaryFile (String filename,
            ArrayList<HashIndexEntry> indexEntries)
	{
		File fileRef = null;              // provides exists(), delete()
		RandomAccessFile binFile = null;  // RAF specializes in binary file I/O

		// If an old version of this binary file exists, delete it.
		// We can overwrite an old file, but it's safer to delete
		// and start fresh.
		try {
		fileRef = new File(filename + ".idx");
			if (fileRef.exists()) {
				fileRef.delete();
			}
		} catch (Exception e) {
			System.out.println("I/O ERROR: Something went wrong with the "
			+ "deletion of the previous index file.");
			System.exit(-1);
		}
		
		// (Re)Create the binary file.  The mode cannot be just
		// "w"; that's not an acceptable option to Java.
		try {
			binFile = new RandomAccessFile(fileRef,"rw");
		} catch (IOException e) {
			System.out.println("I/O ERROR: Something went wrong with the "
			+ "creation of the RandomAccessFile object.");
			System.exit(-1);
		}
		
		// Ask the DataRecord objects to write themselves to the
		// binary file, in the same order in which they were read.
		for (int i = 0; i < indexEntries.size(); i++) {
			HashIndexEntry r = indexEntries.get(i);
			r.dumpObject(binFile);
		}	
		// Wrtiting is complete; close the binary file.
		
		/*try {
			System.out.println(binFile.length()/(2*(4))); //2 ints at 4 bytes each
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} //should print the total number of entries*/
		
		try {
			binFile.close();
		} catch (IOException e) {
			System.out.println("VERY STRANGE I/O ERROR: Couldn't close "
			+ "the binary file!");
			System.exit(-1);
		}
	
	}  // writeBinaryFile()
	
	private static void printHashIndex(ArrayList<HashIndexEntry> table) {
		System.out.print("[");
		for(int j = 1; j <= table.size(); j++) {
			if(j == table.size()) {
				System.out.print(table.get(j-1).getEiaID() + ":" + table.get(j-1).getNumber());
			} else if (j % BUCKET_CAP == 0) {
				System.out.print(table.get(j-1).getEiaID() + ":" + table.get(j-1).getNumber() + "|");
			} else {
				System.out.print(table.get(j-1).getEiaID() + ":" + table.get(j-1).getNumber() + "/");
			}
		}
		System.out.print("] ");
		int H = calculate_H(table.size());
		System.out.print("H = " + H);
		int numBuckets = calculate_numBuckets(H);
		System.out.print(", numBuckets = " + numBuckets);
		System.out.print(", BUCKET_CAP = " + BUCKET_CAP);
		System.out.println(", size = " + table.size());
	}
	
	private static int calculate_H(int size) {
		int H = (int) (Math.log(size / BUCKET_CAP)/Math.log(2)) - 1;
		return H;
	}
	
	private static int calculate_numBuckets(int H) {
		return (int) Math.pow(2, H + 1);
	}
	
	/*initialize every index in indexEntries to distinguish empty
	 * indeces from non empty indeces (-1 is not a record number)*/
	private static void initializeHashIndex(int H, ArrayList<HashIndexEntry> table) {
		int numBuckets = calculate_numBuckets(H);
		int numEntries = numBuckets * BUCKET_CAP; //number of total indexes in ArrayList
		
		for(int i = 0; i < numEntries; i++) {
			HashIndexEntry newEntry = new HashIndexEntry();
			newEntry.setEiaID(-1);
			newEntry.setNumber(-1);
			table.add(newEntry);
		}
	}
	
	private static ArrayList<HashIndexEntry> expandHashIndex(ArrayList<HashIndexEntry> table) {
		ArrayList<HashIndexEntry> newTable = new ArrayList<>(); //new ArrayList to store entries
		int newH = calculate_H(table.size()) + 1;
		int newNumBuckets = calculate_numBuckets(newH);
		
		/*System.out.println("Old: ");
		printHashIndex(table);*/
		
		//create new table and rehash old values
		initializeHashIndex(newH, newTable);
		
		for(int i = 0; i < table.size(); i++) { //go through every entry of table
			if(table.get(i).getNumber() != -1) { //if entry is non-empty
				//copy entry
				HashIndexEntry newEntry = new HashIndexEntry();
				newEntry.setEiaID(table.get(i).getEiaID());
				newEntry.setNumber(table.get(i).getNumber());
				int newBucket = table.get(i).getEiaID() % newNumBuckets; //calculate its new bucket
				//now find an empty bucket to place it in
				for(int j = BUCKET_CAP * newBucket; j < BUCKET_CAP * newBucket + BUCKET_CAP; j++) { //from bucket[0] to bucket[BUCKET_CAP - 1]
					if(newTable.get(j).getNumber() == -1) {
						//copy values to new table
						newTable.get(j).setEiaID(newEntry.getEiaID());
						newTable.get(j).setNumber(newEntry.getNumber());
						break;
					}
				}
			}
		}
		
		/*System.out.println("New: ");
		printHashIndex(newTable);*/
		
		return newTable;
	}
	
	private static ArrayList<HashIndexEntry> hashFunction(HashIndexEntry k, ArrayList<HashIndexEntry> table) {
		int H = calculate_H(table.size()); //calculate H
		int numBuckets = calculate_numBuckets(H);
		
		int bucket = k.getEiaID() % numBuckets; //calculate its bucket
		///System.out.print("Bucket=" + bucket + " ");
		int startEntryOfBucket = BUCKET_CAP * bucket;
		int lastEntryOfBucket = BUCKET_CAP * bucket + BUCKET_CAP - 1;
		///System.out.println("Last: " + lastEntryOfBucket);
		
		if(table.get(lastEntryOfBucket).getNumber() != -1) { //last entry in bucket is not empty, so no more space
			table = expandHashIndex(table); //expand the table
		
			//recalculate H and numBuckets
			H = calculate_H(table.size());
			numBuckets = calculate_numBuckets(H);
			
			bucket = k.getEiaID() % numBuckets; //recalculate its bucket
			
			startEntryOfBucket = BUCKET_CAP * bucket; //recalculate startEntry
			lastEntryOfBucket = BUCKET_CAP * bucket + BUCKET_CAP - 1; //recalculate lastEntry
			
			///System.out.print("Bucket=" + bucket + " ");
			
			for(int j = startEntryOfBucket; j <= lastEntryOfBucket; j++) { //from bucket[0] to bucket[BUCKET_CAP - 1]
				if(table.get(j).getNumber() == -1) {
					//copy values to new table
					table.get(j).setEiaID(k.getEiaID());
					table.get(j).setNumber(k.getNumber());
					//////System.out.println(j);
					break;
				}
			}
		} else {
			///System.out.print("Bucket=" + bucket + " ");
			for(int j = startEntryOfBucket; j <= lastEntryOfBucket; j++) { //from bucket[0] to bucket[BUCKET_CAP - 1]
				if(table.get(j).getNumber() == -1) {
					//copy values to new table
					table.get(j).setEiaID(k.getEiaID());
					table.get(j).setNumber(k.getNumber());
					///////System.out.println(j);
					break;
				}
			}
		}
		
		return table;
	}
	
	
	public static void main(String[] args) {			
		String filename = args[0];
		
		int[] dataLengths = setDataLengths(filename); //stores the length for each String field
		//used to calculate record length
		DataRecord calculateRecordLength = new DataRecord(dataLengths[0], dataLengths[1], dataLengths[2]);
		
		ArrayList<DataRecord> binContent = null;
		binContent = readBinaryFile(filename, dataLengths[0], dataLengths[1], dataLengths[2], calculateRecordLength.getRecordLength());
		
		ArrayList<HashIndexEntry> indexEntries = new ArrayList<>();
		
		initializeHashIndex(0, indexEntries);
		
		for(int i = 0; i < binContent.size(); i++) {
			/*System.out.print(binContent.get(i).getEiaID() + ":");
			System.out.print(i + "-->");*/
			HashIndexEntry newEntry = new HashIndexEntry();
			newEntry.setEiaID(binContent.get(i).getEiaID());
			newEntry.setNumber(i);
			indexEntries = hashFunction(newEntry, indexEntries);
			///printHashIndex(indexEntries);///
		}
		///printHashIndex(indexEntries);///
		
		writeBinaryFile("lhl", indexEntries);
		
		 //(a) number of buckets in the index
		int finalH = calculate_H(indexEntries.size());
		int finalNumBuckets = calculate_numBuckets(finalH);
		System.out.println("Number of buckets in the index: " + finalNumBuckets);
		
		
		//(b) number of records in lowest-occupancy bucket
		int numRecords_lowest = 0;
		for(int i = 0; i <= BUCKET_CAP - 1; i++) {
			if(indexEntries.get(i).getNumber() != -1) {
				numRecords_lowest++;
			}
		}
		System.out.println("Number of records in lowest-occupancy bucket: " + numRecords_lowest);
		
		//(c) number of records in highest-occupancy bucket
		int lastBucket = finalNumBuckets - 1;
		int startEntry_lastBucket = lastBucket * BUCKET_CAP;
		int lastEntry_lastBucket = startEntry_lastBucket + BUCKET_CAP - 1;
		int numRecords_highest = 0;
		for(int i = startEntry_lastBucket; i <= lastEntry_lastBucket; i++) {
			if(indexEntries.get(i).getNumber() != -1) {
				numRecords_highest++;
			}
		}
		System.out.println("Number of records in highest-occupancy bucket: " + numRecords_highest);
		
		//(d) mean of the occupancies across all buckets
		int[] numOccupancies = new int[finalNumBuckets];
		int currentBucket = 0;
		for(int i = 0; i < indexEntries.size(); i++) {
			if(i % BUCKET_CAP == 0 && i > 0) {
				currentBucket++;
			}
			///System.out.println(i + ":" + currentBucket);
			if(indexEntries.get(i).getNumber() != -1) {
				numOccupancies[currentBucket] = numOccupancies[currentBucket] + 1;
			}
		}
		
		//now calculate mean
		double mean = 0;
		for(int i = 0; i < numOccupancies.length; i++) {
			mean += numOccupancies[i];
		}
		mean = mean / finalNumBuckets;
		
		System.out.println("Mean of the occupancies across all buckets: " + mean);
	}	
}
