import java.io.RandomAccessFile;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.SortedMap;
public class DropTable {
	public static void dropTable(String dropTableString) {
		System.out.println("DROP METHOD");
		System.out.println("Parsing the string:\"" + dropTableString + "\"");
		
		String[] tokens=dropTableString.split(" ");
		String tableName = tokens[2];
		if(!DavisBase.tableExists(tableName)){
			System.out.println("Table "+tableName+" does not exist.");
		}
		else
		{
			drop(tableName);
		}		

	}
	public static void drop(String table){
		try{
			
			RandomAccessFile file = new RandomAccessFile("data/davisbase_tables.tbl", "rw");
			int numOfPages = Table.pages(file);
			for(int page = 1; page <= numOfPages; page ++){
				file.seek((page-1)*Table.pageSize);
				byte fileType = file.readByte();
				if(fileType == 0x0D)
				{
					short[] cellsAddr = Page.getCellArray(file, page);
					int k = 0;
					for(int i = 0; i < cellsAddr.length; i++)
					{
						long loc = Page.getCellLoc(file, page, i);
						String[] vals = Table.retrieveValues(file, loc);
						String tb = vals[1];
						if(!tb.equals(table))
						{
							Page.setCellOffset(file, page, k, cellsAddr[i]);
							k++;
						}
					}
					Page.setCellNumber(file, page, (byte)k);
				}
				else
					continue;
			}

			file = new RandomAccessFile("data/davisbase_columns.tbl", "rw");
			numOfPages = Table.pages(file);
			for(int page = 1; page <= numOfPages; page ++){
				file.seek((page-1)*Table.pageSize);
				byte fileType = file.readByte();
				if(fileType == 0x0D)
				{
					short[] cellsAddr = Page.getCellArray(file, page);
					int k = 0;
					for(int i = 0; i < cellsAddr.length; i++)
					{
						long loc = Page.getCellLoc(file, page, i);
						String[] vals = Table.retrieveValues(file, loc);
						String tb = vals[1];
						if(!tb.equals(table))
						{
							Page.setCellOffset(file, page, k, cellsAddr[i]);
							k++;
						}
					}
					Page.setCellNumber(file, page, (byte)k);
				}
				else
					continue;
			}

			File anOldFile = new File("data", table+".tbl"); 
			anOldFile.delete();
		}catch(Exception e){
			System.out.println(e);
		}

	}

}
