import java.io.RandomAccessFile;
import java.io.FileReader;
import java.io.File;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Date;import java.io.RandomAccessFile;
import java.io.FileReader;
import java.io.File;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.*;

public class Table{
	
	public static int size_of_page = 512;
	public static String date_pattern = "yyyy-MM-dd_HH:mm:ss";

	public static void main(String[] args){}


	
	public static int pages(RandomAccessFile file){
		// get the file page size;
		int number_pages = 0;
		try{
			number_pages = (int)(file.length()/(new Long(size_of_page)));
		}catch(Exception e){
			System.out.println(e);
		}

		return number_pages;
	}

	public static String[] getColName(String table){ //tables=davisbase_tables
		String[] coloumns = new String[0];
		try{
			RandomAccessFile file = new RandomAccessFile("data/davisbase_columns.tbl", "rw"); // read file
			Buffer buffer = new Buffer();
			String[] columnName = {"rowid", "table_name", "column_name", "data_type", "ordinal_position", "is_nullable"}; // rows defined in column table;
			String[] cmp = {"table_name","=",table};
			filter(file, cmp, columnName, buffer);
			HashMap<Integer, String[]> contents = buffer.contents;
			ArrayList<String> array = new ArrayList<String>();
			for(String[] i : contents.values()){
				array.add(i[2]);
			}
			int size=array.size();
			coloumns = array.toArray(new String[size]);
			file.close();
			return coloumns;
		}catch(Exception e){
			System.out.println(e);
		}
		return coloumns;
	}
	
	public static String[] getDataType(String table){
		String[] dataType = new String[0];
		try{
			RandomAccessFile file = new RandomAccessFile("data/davisbase_columns.tbl", "rw");
			Buffer buffer = new Buffer();
			String[] columnName = {"rowid", "table_name", "column_name", "data_type", "ordinal_position", "is_nullable"};
			String[] cmp = {"table_name","=",table};
			filter(file, cmp, columnName, buffer);
			HashMap<Integer, String[]> contents = buffer.contents;
			ArrayList<String> array = new ArrayList<String>();
			for(String[] x : contents.values()){
				array.add(x[3]);
			}
			int size=array.size();
			dataType = array.toArray(new String[size]);
			file.close();
			return dataType;
		}catch(Exception e){
			System.out.println(e);
		}
		return dataType;
	}

	
	public static void filter(RandomAccessFile file, String[] cmp, String[] columnName, Buffer buffer){
		try{
			
			int numOfPages = pages(file); // get num of pages;
			for(int page = 1; page <= numOfPages; page++){
				
				file.seek((page-1)*size_of_page);  // get to the current page start header 
				byte pageType = file.readByte(); // reads the byte of curr loc
				if(pageType == 0x0D)  // if it is a leaf page of b-tree , here 13 , unitl its leaf it will skip this
				{
					byte numOfCells = Page.getCellNumber(file, page); //accesses file header to get number of cells.

					for(int i=0; i < numOfCells; i++){   // iter over all the records;
						
						long loc = Page.getCellLoc(file, page, i);	 // return cell loc;
						String[] vals = retrieveValues(file, loc);
						int rowid=Integer.parseInt(vals[0]);

						boolean check = cmpCheck(vals, rowid, cmp, columnName);
						
						if(check)
							buffer.add_vals(rowid, vals);
					}
				}
				else
					continue;
			}

			buffer.columnName = columnName;
			buffer.format = new int[columnName.length];

		}catch(Exception e){
			System.out.println("Error at filter");
			e.printStackTrace();
		}

	}

		public static String[] retrieveValues(RandomAccessFile file, long loc){
		// retrieve values w.r.t it's appropriate datatype
		String[] values = null;
		try{
			
			SimpleDateFormat dateFormat = new SimpleDateFormat (date_pattern);

			file.seek(loc+2);
			int key = file.readInt();
			int num_cols = file.readByte();
			
			byte[] stc = new byte[num_cols];
			file.read(stc);
			
			values = new String[num_cols+1];
			
			values[0] = Integer.toString(key);
			
			for(int i=1; i <= num_cols; i++){
				switch(stc[i-1]){
					case 0x00:  file.readByte();
					            values[i] = "null";
								break;

					case 0x01:  file.readShort();
					            values[i] = "null";
								break;

					case 0x02:  file.readInt();
					            values[i] = "null";
								break;

					case 0x03:  file.readLong();
					            values[i] = "null";
								break;

					case 0x04:  values[i] = Integer.toString(file.readByte());
								break;

					case 0x05:  values[i] = Integer.toString(file.readShort());
								break;

					case 0x06:  values[i] = Integer.toString(file.readInt());
								break;

					case 0x07:  values[i] = Long.toString(file.readLong());
								break;

					case 0x08:  values[i] = String.valueOf(file.readFloat());
								break;

					case 0x09:  values[i] = String.valueOf(file.readDouble());
								break;

					case 0x0A:  Long temp = file.readLong();
								Date dateTime = new Date(temp);
								values[i] = dateFormat.format(dateTime);
								break;

					case 0x0B:  temp = file.readLong();
								Date date = new Date(temp);
								values[i] = dateFormat.format(date).substring(0,10);
								break;

					default:    int len = new Integer(stc[i-1]-0x0C);
								byte[] bytes = new byte[len];
								file.read(bytes);
								values[i] = new String(bytes);
								break;
				}
			}

		}catch(Exception e){
			System.out.println(e);
		}

		return values;
	}
		
	public static int calPayloadSize(String table, String[] vals, byte[] stc){
		String[] dataType = getDataType(table);
		int size =dataType.length;
		for(int i = 1; i < dataType.length; i++){
			stc[i - 1]= getStc(vals[i], dataType[i]);
			size = size + feildLength(stc[i - 1]);
		}
		return size;
	}
	
	public static byte getStc(String value, String dataType){
		if(value.equals("null")){
			switch(dataType){
				case "TINYINT":     return 0x00;
				case "SMALLINT":    return 0x01;
				case "INT":			return 0x02;
				case "BIGINT":      return 0x03;
				case "REAL":        return 0x02;
				case "DOUBLE":      return 0x03;
				case "DATETIME":    return 0x03;
				case "DATE":        return 0x03;
				case "TEXT":        return 0x03;
				default:			return 0x00;
			}							
		}else{
			switch(dataType){
				case "TINYINT":     return 0x04;
				case "SMALLINT":    return 0x05;
				case "INT":			return 0x06;
				case "BIGINT":      return 0x07;
				case "REAL":        return 0x08;
				case "DOUBLE":      return 0x09;
				case "DATETIME":    return 0x0A;
				case "DATE":        return 0x0B;
				case "TEXT":        return (byte)(value.length()+0x0C);
				default:			return 0x00;
			}
		}
	}
	
    public static short feildLength(byte stc){
		switch(stc){
			case 0x00: return 1;
			case 0x01: return 2;
			case 0x02: return 4;
			case 0x03: return 8;
			case 0x04: return 1;
			case 0x05: return 2;
			case 0x06: return 4;
			case 0x07: return 8;
			case 0x08: return 4;
			case 0x09: return 8;
			case 0x0A: return 8;
			case 0x0B: return 8;
			default:   return (short)(stc - 0x0C);
		}
	}


	
public static int searchKeyPage(RandomAccessFile file, int key){
		int val = 1;
		try{
			int numPages = pages(file);
			for(int page = 1; page <= numPages; page++){
				file.seek((page - 1)*size_of_page);
				byte pageType = file.readByte();
				if(pageType == 0x0D){
					int[] keys = Page.getKeyArray(file, page);
					if(keys.length == 0)
						return 0;
					int rm = Page.get_right_most(file, page);
					if(keys[0] <= key && key <= keys[keys.length - 1]){
						return page;
					}else if(rm == 0 && keys[keys.length - 1] < key){
						return page;
					}
				}
			}
		}catch(Exception e){
			System.out.println(e);
		}

		return val;
	}

	
	public static String[] getNullable(String table){
		String[] nullable = new String[0];
		try{
			RandomAccessFile file = new RandomAccessFile("data/davisbase_columns.tbl", "rw");
			Buffer buffer = new Buffer();
			String[] columnName = {"rowid", "table_name", "column_name", "data_type", "ordinal_position", "is_nullable"};
			String[] cmp = {"table_name","=",table};
			filter(file, cmp, columnName, buffer);
			HashMap<Integer, String[]> contents = buffer.contents;
			ArrayList<String> array = new ArrayList<String>();
			for(String[] i : contents.values()){
				array.add(i[5]);
			}
			int size=array.size();
			nullable = array.toArray(new String[size]);
			file.close();
			return nullable;
		}catch(Exception e){
			System.out.println(e);
		}
		return nullable;
	}


	public static void filter(RandomAccessFile file, String[] cmp, String[] columnName, String[] type, Buffer buffer){
		try{
			
			int numOfPages = pages(file);
			
			for(int page = 1; page <= numOfPages; page++){
				
				file.seek((page-1)*size_of_page);
				byte pageType = file.readByte();
				
					if(pageType == 0x0D){
						
					byte numOfCells = Page.getCellNumber(file, page);

					 for(int i=0; i < numOfCells; i++){
						long loc = Page.getCellLoc(file, page, i);
						String[] vals = retrieveValues(file, loc);
						int rowid=Integer.parseInt(vals[0]);
						
						for(int j=0; j < type.length; j++)
							if(type[j].equals("DATE") || type[j].equals("DATETIME"))
								vals[j] = "'"+vals[j]+"'";
						
						boolean check = cmpCheck(vals, rowid , cmp, columnName);

						
						for(int j=0; j < type.length; j++)
							if(type[j].equals("DATE") || type[j].equals("DATETIME"))
								vals[j] = vals[j].substring(1, vals[j].length()-1);
						
						if(check)
							buffer.add_vals(rowid, vals);
					 }
				   }
				    else
						continue;
			}

			buffer.columnName = columnName;
			buffer.format = new int[columnName.length];

		}catch(Exception e){
			System.out.println("Error at filter");
			e.printStackTrace();
		}

	}

	
	public static boolean cmpCheck(String[] values, int rowid, String[] cmp, String[] columnName){

		boolean check = false;
		
		if(cmp.length == 0){
			check = true;
		}
		else{
			int colPos = 1;
			for(int i = 0; i < columnName.length; i++){
				if(columnName[i].equals(cmp[0])){
					colPos = i + 1;
					break;
				}
			}
			
			if(colPos == 1){
				int val = Integer.parseInt(cmp[2]);
				String operator = cmp[1];
				switch(operator){
					case "=": if(rowid == val) 
								check = true;
							  else
							  	check = false;
							  break;
					case ">": if(rowid > val) 
								check = true;
							  else
							  	check = false;
							  break;
					case ">=": if(rowid >= val) 
						        check = true;
					          else
					  	        check = false;	
					          break;
					case "<": if(rowid < val) 
								check = true;
							  else
							  	check = false;
							  break;
					case "<=": if(rowid <= val) 
								check = true;
							  else
							  	check = false;	
							  break;
					case "!=": if(rowid != val)  
								check = true;
							  else
							  	check = false;	
							  break;						  							  							  							
				}
			}else{
				if(cmp[2].equals(values[colPos-1]))
					check = true;
				else
					check = false;
			}
		}
		return check;
	}
	
}