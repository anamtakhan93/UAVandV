package CSVModule;

import java.io.FileWriter;
import java.io.IOException;

import com.opencsv.CSVWriter;

public class CSVDataWriter {
	
	private String fileName;
	private CSVWriter writer;
	
	
	public CSVDataWriter(String fileName) throws IOException {
		super();
		this.fileName = fileName;
		writer = new CSVWriter(new FileWriter(fileName, true));
	}


	public void addNewLine( String [] record) throws IOException {
	      writer.writeNext(record);
	}
	
	public void closeWriter() throws IOException {
		writer.close();
	}
	
	
}
