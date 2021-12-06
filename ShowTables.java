import java.io.RandomAccessFile;
import java.io.FileReader;
import java.io.File;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Date;
import java.text.SimpleDateFormat;


public class ShowTables{

public static void showTables() {
		System.out.println("STACKTRACE: SHOW METHOD CALLED");
		// System.out.println("Parsing the string:\"show tables\"");
		
		String table = "davisbase_tables";  // default table_name that contains list of all the tables;
		String[] coloumns = {"table_name"};  // default col name
		String[] cmptr = new String[0];
		select(table, coloumns, cmptr);   //Table.java 
	}

public static void select(String table, String[] coloumns, String[] cmp){     //select(davisbase_tables,table_name,=)
	try{
		
		RandomAccessFile file = new RandomAccessFile("data/"+table+".tbl", "rw");
		String[] columnName = Table.getColName(table);  // arrayList to stores col names
		String[] type = Table.getDataType(table);      // 
		
		Buffer buffer = new Buffer();
		
		Table.filter(file, cmp, columnName, type, buffer);
		buffer.b_disp(coloumns);
		file.close();
	}catch(Exception e){
		System.out.println(e);
	}
}

}
 