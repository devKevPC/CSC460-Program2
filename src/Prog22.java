/*
 * Authors: Kevin Callaghan
 * Assignment: Program 2
 * Instructor: Lester McCann
 * TAs:  Priya Kaushik and Aayush Pinto
 * Course: CSC 460
 * Written: 21 September 2022
 * 
 * Prog22.java -- Process a simple variety of query with the help of a
 * LHL index stored in a .idx file. Prompts user to enter a target look-up
 * value and uses the LHL index to locate the proper record in the .bin file
 * quicker. 
 * 
 * Java Version: jdk-16.0.2
 * Compiation: 	store complete path to .idx file in args[0]
 * 				store complete path to .bin file in args[1]
 * 	Possilble bugs: didn't check that all target values can be found properly
 */

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Scanner;

public class Prog22 {
	public static int BUCKET_CAP = 20;

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
    |  Method readBinaryFile(filename:String)
    |
    |  Purpose:  Opens and reads the content of filename.bin, and returns
    |            that content to the caller in the form of an ArrayList
    |            of DataRecord objects.
    |
    |  Pre-condition:  The file filename.bin exists, filename includes path
    |                  directory, is properly structured, and is readable.
    |
    |  Post-condition: The returned collection of records contains the
    |                  same data as the file does, and in the same order.
    |
    |  Parameters:
    |      filename -- Includes path and .bin extension.
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
	
	/*print the DataRecord object with specified format*/
	private static void printDataRecord(DataRecord dr) {
		System.out.print("[" + dr.getEiaID() + "]");
		System.out.print("[" + dr.getName() + "]");
		//System.out.print("[" + dr.getSolarCOD() + "]");
		//System.out.print("[" + dr.getState() + "]");
		//System.out.print("[" + dr.getLatitude() + "]");
		//System.out.print("[" + dr.getLongitude() + "]");
		//System.out.print("[" + dr.getAvgGHI() + "]");
		//System.out.print("[" + dr.getSolarCapacityDC() + "]");
		System.out.print("[" + dr.getSolarCapacityAC() + "]");
		System.out.println();
	}
	
	/*Calculates the current H of Hashfunction from ArrayList<HashIndexEntry> size
	 * returns H as H:int*/
	private static int calculate_H(int size) {
		int H = (int) (Math.log(size / BUCKET_CAP)/Math.log(2)) - 1;
		return H;
	}
	
	/*Calculates the current number of buckets from current H
	 * returns number of buckets as numBuckets:int*/
	private static int calculate_numBuckets(int H) {
		return (int) Math.pow(2, H + 1);
	}
	
	
	/*---------------------------------------------------------------------
    |  Method readIndexFile(idxFilepath:String)
    |
    |  Purpose:  Opens and reads the content of idxFilepath.idx, and returns
    |            that content to the caller in the form of an ArrayList
    |            of HashIndexEntry objects.
    |
    |  Pre-condition:  The file idxFilepath.idx exists, includes path
    |                  directory, is properly structured, and is readable.
    |
    |  Post-condition: The returned collection of records contains the
    |                  same data as the file does, and in the same order.
    |
    |  Parameters:
    |      idxFilepath -- Includes path and .idx extension.
    |
    |  Returns: An ArrayList of HashIndexEntry objects.
    *-------------------------------------------------------------------*/
	private static ArrayList<HashIndexEntry> readIndexFile(String idxFilepath){
		RandomAccessFile binFile = null; // RAF specializes in binary file I/O
		
		try {
			binFile = new RandomAccessFile(idxFilepath, "r");
		} catch(IOException e) {
			System.out.println("I/O ERROR: Something went wrong with the "
                    + "opening of the RandomAccessFile object for lhl.idx.");
			System.exit(-1);
		}
		
		// Determine how many records are in the binary file,
        // so that we know how many to read.
		long numberOfEntries = 0; // Quantity of records in the binary file
		try {
			numberOfEntries = (int) (binFile.length() / (2*(4))); //two ints
		} catch (IOException e) {
			System.out.println("I/O ERROR: Couldn't get the file's length.");
			System.exit(-1);
		}
		
		// Move the file pointer (which marks the byte with which
        // the next access will begin)
		try {
			binFile.seek(0);
		} catch (IOException e) {
			System.out.println("I/O ERROR: Seems we can't reset the file "
                    + "pointer to the start of the file.");
			System.exit(-1);
		}
		
		// Read the records from the binary file into an
        // in-memory data structure, for return to the caller.
		ArrayList<HashIndexEntry> idxContent = new ArrayList<>(); //holds binary records
		for (int i = 0; i < numberOfEntries; i++) {
			HashIndexEntry indexEntry = new HashIndexEntry(); //create object to hold record
			indexEntry.fetchObject(binFile);
			idxContent.add(indexEntry);
		}
		
		//Reading is complete; close the binary file.
		try {
            binFile.close();
        } catch (IOException e) {
            System.out.println("VERY STRANGE I/O ERROR: Couldn't close "
                             + "the binary file after reading!");
            System.exit(-1);
        }
		
		return idxContent;
	} //readIdxFile
	
	public static void main(String[] args) {			
		String idxFilepath = args[0]; //idx file path
		String binFilepath = args[1]; //bin file path
		
		int[] dataLengths = setDataLengths(binFilepath); //stores the length for each String field
		//used to calculate record length
		DataRecord calculateRecordLength = new DataRecord(dataLengths[0], dataLengths[1], dataLengths[2]);
		
		ArrayList<DataRecord> binContent = null;
		//get content from binary file
		binContent = readBinaryFile(binFilepath, dataLengths[0], dataLengths[1], dataLengths[2], calculateRecordLength.getRecordLength());
		
		ArrayList<HashIndexEntry> indexEntries = null;
		//get content of HashIndex and store it in an ArrayList
		indexEntries = readIndexFile(idxFilepath);
		
		int H = calculate_H(indexEntries.size()); //H
		int numBuckets = calculate_numBuckets(H); //number of buckets
		int numEntries = numBuckets * BUCKET_CAP; //number of entries = indexEntries.size();
		
		String in =  "";
		Scanner input;
		int targetValue;
		while(true) { //loop for user input
			System.out.println("Enter target value (type <quit> to exit): ");
			input = new Scanner(System.in);
			in = input.next();
			
			if(in.matches("[1-9][0-9]*|0")) { //make sure entered value is an integer
				targetValue = Integer.parseInt(in);
				
				int bucket = targetValue % numBuckets;
				int startEntry = bucket * BUCKET_CAP;
				int lastEntry = startEntry + BUCKET_CAP - 1;
				for(int i = startEntry; i <= lastEntry; i++) { //from first entry of bucket to last entry of bucket
					if(indexEntries.get(i).getNumber() == -1) { //if an empty entry is an encountered, then target value is not there
						System.out.println("The target value '" + targetValue + "' was not found.");
						break;
					} else if(indexEntries.get(i).getEiaID() == targetValue) { //target value found in bucket
						int number = indexEntries.get(i).getNumber();
						DataRecord dr = binContent.get(number);
						printDataRecord(dr);
						break;
					}
				}
				
			}else if(in.equals("quit")) { //close console
				input.close();
				System.exit(0);
			}else {
				System.out.println("You did not input a valid EIA ID!!!"); //invalid target value entered
			}
		}//end while
	}	
}