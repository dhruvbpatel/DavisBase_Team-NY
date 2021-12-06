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
		System.out.println("STACKTRACE: UPDATE METHOD CALLED");
		
		
		String[] tokens=updateString.split(" ");

		String tbl = tokens[1];

		String[] tmp1 = updateString.split("set");
		String[] tmp2 = tmp1[1].split("where");

		String cmp_temp = tmp2[1];
		String set_temp = tmp2[0];

		String[] cmp = DavisBase.parserEquation(cmp_temp);
		String[] set = DavisBase.parserEquation(set_temp);

		// check if table exists
		if(!DavisBase.checkTableExists(tbl)){
			System.out.println("Table "+tbl+" does not exist.");
		}
		else
		{
			update(tbl, cmp, set);
		}
		
	}
	public static void update(String tbl, String[] cmp, String[] set){
		try{
			
			int key = new Integer(cmp[2]);
			
			RandomAccessFile file = new RandomAccessFile("data/"+tbl+".tbl", "rw");

			int num_pages = Table.pages(file);

			int page = 0;

			for(int iter = 1; iter <= num_pages; iter++)
				if(Page.has_key(file, iter, key) & Page.getPageType(file, iter)==0x0D){
					page = iter;
				}
			

			if(page==0)
			{
				System.out.println("KeyError: Keyval does not exist");
				return;
			}
			
			int[] key_list = Page.getKeyArray(file, page);

			int id = 0;

			for(int i = 0; i < key_list.length; i++)

				if(key_list[i] == key)
					id = i;


			int offset = Page.getCellOffset(file, page, id);

			long cell_loc = Page.getCellLoc(file, page, id);
			
			String[] cols = Table.getColName(tbl);

			String[] vals = Table.retrieveValues(file, cell_loc);

			String[] dType = Table.getDataType(tbl);


			for(int i=0; i < dType.length; i++)

				if(dType[i].equals("DATE") || dType[i].equals("DATETIME"))
				
					vals[i] = "'"+vals[i]+"'";

			for(int i = 0; i < cols.length; i++)
				if(cols[i].equals(set[0]))
					id = i;

			vals[id] = set[2];

			String[] nullable = Table.getNullable(tbl);

			for(int i = 0; i < nullable.length; i++){

				if(vals[i].equals("null") && nullable[i].equals("NO")){

					System.out.println("ERROR: NULL VALUE VIOLATION");
					return;
				}
			}

			byte[] stc = new byte[cols.length-1];

			int payload_size = Table.calPayloadSize(tbl, vals, stc);

			Page.updateLeafCell(file, page, offset, payload_size, key, stc, vals);

			file.close();

		}catch(Exception e){
			System.out.println(e);
		}
	}

}
