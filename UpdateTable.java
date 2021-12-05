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
	public static void parseUpdateString(String updateTheString) {
		System.out.println("METHOD UPDATE");
		System.out.println("string Parse:\"" + updateTheString + "\"");
		
		String[] tokns=updateTheString.split(" ");
		String tables = tokns[1];
		String[] temporary1 = updateTheString.split("set");
		String[] temporary2 = temporary1[1].split("where");
		String cmpTemporary = temporary2[1];
		String setTemporary = temporary2[0];
		String[] cmps = DavisBase.parserEquation(cmpTemporary);
		String[] sets = DavisBase.parserEquation(setTemporary);
		if(!DavisBase.checkTableExists(tables)){
			System.out.println("Table "+tables+" do not exists.");
		}
		else
		{
			update(tables, cmps, sets);
		}
		
	}
	public static void update(String tables, String[] cmps, String[] sets){
		try{
			
			int key = new Integer(cmps[2]);
			
			RandomAccessFile file = new RandomAccessFile("data/"+tables+".tbl", "rw");
			int numPages = Table.pages(file);
			int page = 0;
			for(int p = 1; p <= numPages; p++)
				if(Page.hasKey(file, p, key)&Page.getPageType(file, p)==0x0D){
					page = p;
				}
			
			if(page==0)
			{
				System.out.println("The value of key do not exist");
				return;
			}
			
			int[] keys = Page.getKeyArray(file, page);
			int x = 0;
			for(int i = 0; i < keys.length; i++)
				if(keys[i] == key)
					x = i;
			int offset = Page.getCellOffset(file, page, x);
			long loc = Page.getCellLoc(file, page, x);
			
			String[] cols = Table.getColName(tables);
			String[] values = Table.retrieveValues(file, loc);

			String[] type = Table.getDataType(tables);
			for(int i=0; i < type.length; i++)
				if(type[i].equals("DATE") || type[i].equals("DATETIME"))
					values[i] = "'"+values[i]+"'";

			for(int i = 0; i < cols.length; i++)
				if(cols[i].equals(sets[0]))
					x = i;
			values[x] = sets[2];

			String[] nullble = Table.getNullable(tables);
			for(int i = 0; i < nullble.length; i++){
				if(values[i].equals("null") && nullble[i].equals("NO")){
					System.out.println("NULL-value constraint violation");
					return;
				}
			}

			byte[] stc = new byte[cols.length-1];
			int plusize = Table.calPayloadSize(tables, values, stc);
			Page.updateLeafCell(file, page, offset, plusize, key, stc, values);

			file.close();

		}catch(Exception e){
			System.out.println(e);
		}
	}

}
