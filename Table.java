import java.io.RandomAccessFile;
import java.io.FileReader;
import java.io.File;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Date;
import java.text.SimpleDateFormat;

public class Table{
	
	public static int sizeOfPage = 512;
	public static String patternOfDate = "yyyy-MM-dd_HH:mm:ss";

	public static void main(String[] args){}


	
	public static int pages(RandomAccessFile file){

		int pages_num = 0;
		try{
			pages_num = (int)(file.length()/(new Long(sizeOfPage)));
		}catch(Exception e){
			System.out.println(e);
		}

		return pages_num;
	}

	public static String[] getColName(String table){  
		String[] colmns = new String[0];
		try{
			RandomAccessFile file = new RandomAccessFile("data/davisbase_columns.tbl", "rw"); // read file
			Buffer buffer = new Buffer();
			String[] columnName = {"rowid", "table_name", "column_name", "data_type", "ordinal_position", "is_nullable"}; // rows defined in column table;
			String[] cmp = {"table_name","=",table};
			filter(file, cmp, columnName, buffer);
			HashMap<Integer, String[]> content = buffer.content;
			ArrayList<String> array = new ArrayList<String>();
			for(String[] i : content.values()){
				array.add(i[2]);
			}
			int size=array.size();
			colmns = array.toArray(new String[size]);
			file.close();
			return colmns;
		}catch(Exception e){
			System.out.println(e);
		}
		return colmns;
	}
	
	public static String[] getDataType(String table){
		String[] dataType = new String[0];
		try{
			RandomAccessFile file = new RandomAccessFile("data/davisbase_columns.tbl", "rw");
			Buffer buffer = new Buffer();
			String[] columnName = {"rowid", "table_name", "column_name", "data_type", "ordinal_position", "is_nullable"};
			String[] cmp = {"table_name","=",table};
			filter(file, cmp, columnName, buffer);
			HashMap<Integer, String[]> content = buffer.content;
			ArrayList<String> array = new ArrayList<String>();
			for(String[] x : content.values()){
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
			
			int numberOfPages = pages(file); 
			for(int page = 1; page <= numberOfPages; page++){
				
				file.seek((page-1)*sizeOfPage);  
				byte pageType = file.readByte(); 
				if(pageType == 0x0D)  
				{
					byte numOfCells = Page.getCellNumber(file, page); 

					for(int i=0; i < numOfCells; i++){   
						
						long loc = Page.getCellLoc(file, page, i);	 
						String[] vals = retrieveValues(file, loc);
						int rowid=Integer.parseInt(vals[0]);

						boolean checks = cmpCheck(vals, rowid, cmp, columnName);
						
						if(checks)
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
	
		String[] valued = null;
		try{
			
			SimpleDateFormat dateFormat = new SimpleDateFormat (patternOfDate);

			file.seek(loc+2);
			int key = file.readInt();
			int num_cols = file.readByte();
			
			byte[] stc = new byte[num_cols];
			file.read(stc);
			
			valued = new String[num_cols+1];
			
			valued[0] = Integer.toString(key);
			
			for(int i=1; i <= num_cols; i++){
				switch(stc[i-1]){
					case 0x00:  file.readByte();
					            valued[i] = "null";
								break;

					case 0x01:  file.readShort();
					            valued[i] = "null";
								break;

					case 0x02:  file.readInt();
					            valued[i] = "null";
								break;

					case 0x03:  file.readLong();
					            valued[i] = "null";
								break;

					case 0x04:  valued[i] = Integer.toString(file.readByte());
								break;

					case 0x05:  valued[i] = Integer.toString(file.readShort());
								break;

					case 0x06:  valued[i] = Integer.toString(file.readInt());
								break;

					case 0x07:  valued[i] = Long.toString(file.readLong());
								break;

					case 0x08:  valued[i] = String.valueOf(file.readFloat());
								break;

					case 0x09:  valued[i] = String.valueOf(file.readDouble());
								break;

					case 0x0A:  Long temp = file.readLong();
								Date dateTime = new Date(temp);
								valued[i] = dateFormat.format(dateTime);
								break;

					case 0x0B:  temp = file.readLong();
								Date date = new Date(temp);
								valued[i] = dateFormat.format(date).substring(0,10);
								break;

					default:    int len = new Integer(stc[i-1]-0x0C);
								byte[] bytes = new byte[len];
								file.read(bytes);
								valued[i] = new String(bytes);
								break;
				}
			}

		}catch(Exception e){
			System.out.println(e);
		}

		return valued;
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
	
	public static byte getStc(String values, String dataType){
		if(values.equals("null")){
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
				case "TEXT":        return (byte)(values.length()+0x0C);
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
				file.seek((page - 1)*sizeOfPage);
				byte pageType = file.readByte();
				if(pageType == 0x0D){
					int[] keys = Page.getKeyArray(file, page);
					if(keys.length == 0)
						return 0;
					int rm = Page.getRightMost(file, page);
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
			HashMap<Integer, String[]> content = buffer.content;
			ArrayList<String> array = new ArrayList<String>();
			for(String[] i : content.values()){
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
			
			int numberOfPages = pages(file);
			
			for(int page = 1; page <= numberOfPages; page++){
				
				file.seek((page-1)*sizeOfPage);
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
						
						boolean checks = cmpCheck(vals, rowid , cmp, columnName);

						
						for(int j=0; j < type.length; j++)
							if(type[j].equals("DATE") || type[j].equals("DATETIME"))
								vals[j] = vals[j].substring(1, vals[j].length()-1);
						
						if(checks)
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

		boolean checks = false;
		
		if(cmp.length == 0){
			checks = true;
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
								checks = true;
							  else
							  	checks = false;
							  break;
					case ">": if(rowid > val) 
								checks = true;
							  else
							  	checks = false;
							  break;
					case ">=": if(rowid >= val) 
						        checks = true;
					          else
					  	        checks = false;	
					          break;
					case "<": if(rowid < val) 
								checks = true;
							  else
							  	checks = false;
							  break;
					case "<=": if(rowid <= val) 
								checks = true;
							  else
							  	checks = false;	
							  break;
					case "!=": if(rowid != val)  
								checks = true;
							  else
							  	checks = false;	
							  break;						  							  							  							
				}
			}else{
				if(cmp[2].equals(values[colPos-1]))
					checks = true;
				else
					checks = false;
			}
		}
		return checks;
	}
	
}
