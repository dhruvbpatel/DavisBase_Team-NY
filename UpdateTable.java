import java.io.RandomAccessFile;
import java.io.FileReader;
import java.io.File;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Date;
import java.text.SimpleDateFormat;

public class UpdateTable {
	public static void parseUpdateString(String updateString) {
		System.out.println("UPDATE METHOD");
		System.out.println("Parsing the string:\"" + updateString + "\"");
		
		String[] tokens=updateString.split(" ");
		String table = tokens[1];
		String[] temp1 = updateString.split("set");
		String[] temp2 = temp1[1].split("where");
		String cmpTemp = temp2[1];
		String setTemp = temp2[0];
		String[] cmp = DavisBase.parserEquation(cmpTemp);
		String[] set = DavisBase.parserEquation(setTemp);
		if(!DavisBase.checkTableExists(table)){
			System.out.println("Table "+table+" does not exist.");
		}
		else
		{
			update(table, cmp, set);
		}
		
	}
	public static void update(String table, String[] cmp, String[] set){
		try{
			
			int key = new Integer(cmp[2]);
			
			RandomAccessFile file = new RandomAccessFile("data/"+table+".tbl", "rw");
			int numPages = Table.pages(file);
			int page = 0;
			for(int p = 1; p <= numPages; p++)
				if(Page.has_key(file, p, key)&Page.getPageType(file, p)==0x0D){
					page = p;
				}
			
			if(page==0)
			{
				System.out.println("The given key value does not exist");
				return;
			}
			
			int[] keys = Page.getKeyArray(file, page);
			int x = 0;
			for(int i = 0; i < keys.length; i++)
				if(keys[i] == key)
					x = i;
			int offset = Page.getCellOffset(file, page, x);
			long loc = Page.getCellLoc(file, page, x);
			
			String[] coloumns = Table.getColName(table);
			String[] values = Table.retrieveValues(file, loc);

			String[] type = Table.getDataType(table);
			for(int i=0; i < type.length; i++)
				if(type[i].equals("DATE") || type[i].equals("DATETIME"))
					values[i] = "'"+values[i]+"'";

			for(int i = 0; i < coloumns.length; i++)
				if(coloumns[i].equals(set[0]))
					x = i;
			values[x] = set[2];

			String[] nullable = Table.getNullable(table);
			for(int i = 0; i < nullable.length; i++){
				if(values[i].equals("null") && nullable[i].equals("NO")){
					System.out.println("NULL-value constraint violation");
					return;
				}
			}

			byte[] stc = new byte[coloumns.length-1];
			int plsize = Table.calPayloadSize(table, values, stc);
			Page.updateLeafCell(file, page, offset, plsize, key, stc, values);

			file.close();

		}catch(Exception e){
			System.out.println(e);
		}
	}

}
