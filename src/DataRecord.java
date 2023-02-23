import java.io.IOException;
import java.io.RandomAccessFile;

/*+----------------------------------------------------------------------
||
||  Class DataRecord 
||
||        Author:  L. McCann and Kevin Callaghan
||
||        Purpose:  An object of this class holds the field values of one
||                  record of data.  There are nine fields, listed below.
			 * EIA ID: int
			 * Project Name: String
			 * Solar COD: String
			 * State: String
			 * Latitude: double
			 * Longitude: double
			 * Avg GHI: double
			 * Solar Capacity MW-DC: double
			 * Solar Capacity MW-AC: double
||
||  Inherits From:  None.
||
||     Interfaces:  None.
||
|+-----------------------------------------------------------------------
||
||   Constructors:  Just the default constructor; no arguments.
||
||  Class Methods:  None.
||
||  Inst. Methods:     int getEiaId()
||                  String getName()
||                  String getSolarCOD()
||                  String getState()
||                  double getLatitude()
||                  double getLongitude()
||                  double getAvgGHI()
||                  double getSolarCapacityDC()
||                  double getSolarCapacityAC()
||  			    void setEiaId()
||                  void setName()
||                  void setSolarCOD()
||                  void setState()
||                  void setLatitude()
||                  void setLongitude()
||                  void setAvgGHI()
||                  void setSolarCapacityDC()
||                  void setSolarCapacityAC()
||
||                    void dumpObject(RandomAccessFile stream)
||                    void fetchObject(RandomAccessFile stream)
||
++-----------------------------------------------------------------------*/

public class DataRecord {
	public class java {

	}

	private int PROJECT_NAME_LENGTH;
	private int SOLAR_COD_LENGTH;
	private int STATE_LENGTH;
	private int RECORD_LENGTH;
	
	//data fields that comprise a record of our file
	private int eiaId;
	private String name;
	private String solarCOD;
	private String state;
	private double latitude;
	private double longitude;
	private double avgGHI;
	private double solarCapacityDC;
	private double solarCapacityAC;
	
	/*constructor*/
	public DataRecord(int PNL, int SCL, int SL) {
		PROJECT_NAME_LENGTH = PNL;
		SOLAR_COD_LENGTH = SCL;
		STATE_LENGTH = SL;
		RECORD_LENGTH = PROJECT_NAME_LENGTH + SOLAR_COD_LENGTH + STATE_LENGTH + 1*(4) + 5*(8); //int is (4) bytes, double is (8) bytes
	}
	
	//getters for the data field values
	public int getProjectNameLength() {return PROJECT_NAME_LENGTH;}
	public int getSolarCodLength() {return SOLAR_COD_LENGTH;}
	public int getStateLength() {return STATE_LENGTH;}
	public int getRecordLength() {return RECORD_LENGTH;}
	
	public int getEiaID() 				{return eiaId;}
	public String getName() 			{return name;}
	public String getSolarCOD() 		{return solarCOD;}
	public String getState()			{return state;}
	public double getLatitude()			{return latitude;}
	public double getLongitude()		{return longitude;}
	public double getAvgGHI()			{return avgGHI;}
	public double getSolarCapacityDC()	{return solarCapacityDC;}
	public double getSolarCapacityAC()	{return solarCapacityAC;}
	

	//setters for the data field values
	public void setEiaId(int eiaId) 			{this.eiaId = eiaId;}
	public void setName(String name) 			{this.name = name;}
	public void setSolarCOD(String solarCOD) 	{this.solarCOD = solarCOD;}
	public void setState(String state)			{this.state = state;}
	public void setLatitude(double latitude)	{this.latitude = latitude;}
	public void setLongitude(double longitude)	{this.longitude = longitude;}
	public void setAvgGHI(double avgGHI)		{this.avgGHI = avgGHI;}
	public void setSolarCapacityDC(double dc)	{this.solarCapacityDC = dc;}
	public void setSolarCapacityAC(double ac)	{this.solarCapacityAC = ac;}

	/*Purpose:  Writes the content of the object's fields
        |            to the file represented by the given RandomAccessFile
        |            object reference.*/
    public void dumpObject(RandomAccessFile stream)
    {
        StringBuffer n = new StringBuffer(name);  // paddable name str
        StringBuffer c = new StringBuffer(solarCOD);
        StringBuffer s = new StringBuffer(state);

        try {
            stream.writeInt(eiaId);
            n.setLength(PROJECT_NAME_LENGTH);  // pads to right with nulls
            c.setLength(SOLAR_COD_LENGTH);
            s.setLength(STATE_LENGTH);
            stream.writeBytes(n.toString());  // only ASCII, not UNICODE
            stream.writeBytes(c.toString());
            stream.writeBytes(s.toString());
            stream.writeDouble(latitude);
            stream.writeDouble(longitude);
            stream.writeDouble(avgGHI);
            stream.writeDouble(solarCapacityDC);
            stream.writeDouble(solarCapacityAC);
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
        byte[] n = new byte[PROJECT_NAME_LENGTH];  // ASCII, not UNICODE
        byte[] c = new byte[SOLAR_COD_LENGTH];
        byte[] s = new byte[STATE_LENGTH];
        
        try {
            eiaId = stream.readInt();
            stream.readFully(n);        // reads all the bytes we need...
            name = new String(n); // ...& makes a String of them
            stream.readFully(c);
            solarCOD = new String(c);
            stream.readFully(s);
            state = new String(s);
            latitude = stream.readDouble();
            longitude = stream.readDouble();
            avgGHI = stream.readDouble();
            solarCapacityDC = stream.readDouble();
            solarCapacityAC = stream.readDouble();
        } catch (IOException e) {
           System.out.println("I/O ERROR: Couldn't read from the file;\n\t"
                            + "perhaps it doesn't have the expected content?");
           System.exit(-1); 
        }
    }
}
