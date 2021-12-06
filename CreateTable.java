
import java.io.RandomAccessFile;
import java.io.FileReader;
import java.io.File;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Date;
import java.text.SimpleDateFormat;
public class CreateTable {
public static void parseCreateString(String createString) {
		
		System.out.println("STACKTRACE: CREATE METHOD");
		// System.out.println("Parsing the string:\"" + createString + "\"");

		String[] ct_tokens=createString.split(" ");
		
		if (ct_tokens[1].compareTo("index")==0)
		{
		String ct_col = ct_tokens[4];

		String ct_name_col = ct_col.substring(1,ct_col.length()-1);

		Index.createIndex(ct_tokens[3],ct_name_col, "String");
		}
		else
		{
		
		if (ct_tokens[1].compareTo("table")>0){
			System.out.println("ERROR: Wrong syntax");
			
		}
		else{
				 
		String name_of_the_table = ct_tokens[2];

		String[] ct_temp = createString.split(name_of_the_table);

		String ct_colmns = ct_temp[1].trim();
		String[] create_cols = ct_colmns.substring(1, ct_colmns.length()-1).split(",");
		
		for(int i = 0; i < create_cols.length; i++)
			create_cols[i] = create_cols[i].trim();
		
		if(DavisBase.checkTableExists(name_of_the_table)){

			System.out.println("Table "+name_of_the_table+" already exists.");
		}
		else
			{
			createTable(name_of_the_table, create_cols);		
			}
			}
		}
	}



public static void createTable(String table, String[] col){                                     //Create from DavisBase.java
	try{	
		
		RandomAccessFile file = new RandomAccessFile("data/"+table+".tbl", "rw");				//creates .tbl file (table)
		file.setLength(Table.size_of_page);	//512bytes										
		file.seek(0);				//seek first ct_position
		file.writeByte(0x0D);		//Write
		file.close();
		
		file = new RandomAccessFile("data/davisbase_tables.tbl", "rw");
		
		int pages_num = Table.pages(file);
		int page=1;
		for(int p = 1; p <= pages_num; p++){
			int rm = Page.get_right_most(file, p);
			if(rm == 0)
				page = p;
		}
		
		int[] keys = Page.getKeyArray(file, page);
		int l = keys[0];
		for(int i = 0; i < keys.length; i++)
			if(keys[i]>l)
				l = keys[i];
		file.close();
		
		String[] values = {Integer.toString(l+1), table};
		insertInto("davisbase_tables", values);

		file = new RandomAccessFile("data/davisbase_columns.tbl", "rw");
		
		pages_num = Table.pages(file);
		page=1;
		for(int p = 1; p <= pages_num; p++){
			int rm = Page.get_right_most(file, p);
			if(rm == 0)
				page = p;
		}
		
		keys = Page.getKeyArray(file, page);
		l = keys[0];
		for(int i = 0; i < keys.length; i++)
			if(keys[i]>l)
				l = keys[i];
		file.close();

		for(int i = 0; i < col.length; i++){
			l = l + 1;
			String[] token = col[i].split(" ");
			String col_name = token[0];
			String datee = token[1].toUpperCase();
			String ct_position = Integer.toString(i+1);
			String ct_null;
			if(token.length > 2)
				ct_null = "NO";
			else
				 ct_null = "YES";
			String[] value = {Integer.toString(l), table, col_name, datee, ct_position, ct_null};
			insertInto("davisbase_columns", value);
		}

	}catch(Exception e){
		System.out.println(e);
	}
}



public static void insertInto(String table, String[] values){
	try{
		RandomAccessFile file = new RandomAccessFile("data/"+table+".tbl", "rw");

		insertInto(file, table, values);
		file.close();

	}catch(Exception e){
		System.out.println(e);
	}
}


public static void insertInto(RandomAccessFile file, String table, String[] values){
	String[] ct_dtype = Table.getDataType(table);
	String[] nullable = Table.getNullable(table);

	for(int i = 0; i < nullable.length; i++)

		if(values[i].equals("null") && nullable[i].equals("NO")){

			System.out.println("ERROR: NULL VALUE VIOLATION");

			System.out.println();
			return;
		}

	int key = new Integer(values[0]);

	int page = Table.searchKeyPage(file, key);

	if(page != 0)
		if(Page.has_key(file, page, key)){

			System.out.println("ERROR: Uniqueness violation");
			return;
		}
	if(page == 0)
		page = 1;


	byte[] ct_stc = new byte[ct_dtype.length-1];
	short size_of_pl = (short) Table.calPayloadSize(table, values, ct_stc);
	int size_of_the_cell = size_of_pl + 6;
	int offset = Page.checkLeafSpace(file, page, size_of_the_cell);


	if(offset != -1){
		Page.insertLeafCell(file, page, offset, size_of_pl, key, ct_stc, values);
	}else{
		Page.splitLeaf(file, page);
		insertInto(file, table, values);
	}
}

}
