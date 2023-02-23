/*
 * Authors: Kevin Callaghan
 * Assignment: Program 2
 * Instructor: Lester McCann
 * TAs:  Priya Kaushik and Aayush Pinto
 * Course: CSC 460
 * Written: 21 September 2022
 * 
 * HashIndexEntry.java -- Stores the EIA Id as an int
 * 	and its number in the .bin file
 * 
 * Java Version: jdk-16.0.2
 * 
 */

import java.io.IOException;
import java.io.RandomAccessFile;

public class HashIndexEntry {
	private int eiaId;
	private int number;
	
	/*gettors*/
	public int getEiaID() {return eiaId;}
	public int getNumber() {return number;}
	
	/*settors*/
	public void setEiaID(int id) {eiaId = id;}
	public void setNumber(int num) {number = num;}
	
	/*Purpose:  Writes the content of the object's fields
    |            to the file represented by the given RandomAccessFile
    |            object reference.*/
	public void dumpObject(RandomAccessFile stream)
	{
	    try {
	        stream.writeInt(eiaId);
	        stream.writeInt(number);
	    } catch (IOException e) {
	       System.out.println("I/O ERROR: Couldn't write to the file;\n\t"
	                        + "perhaps the file system is full?");
	       System.exit(-1);
	    }
	}

/* Purpose:  Read the content of the object's fields from the file
    |            represented by the given RandomAccessFile object 
    |            reference ('stream'), starting at the current file
    |            position.*/
	public void fetchObject(RandomAccessFile stream)
	{   
	    try {
	        eiaId = stream.readInt();
	        number = stream.readInt();
	    } catch (IOException e) {
	       System.out.println("I/O ERROR: Couldn't read from the file;\n\t"
	                        + "perhaps it doesn't have the expected content?");
	       System.exit(-1); 
	    }
	}
}
