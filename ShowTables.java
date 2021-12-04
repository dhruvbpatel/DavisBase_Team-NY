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
		System.out.println("SHOW METHOD");
		System.out.println("Parsing the string:\"show tables\"");
		
		String table = "davisbase_tables";  // default table_name that contains list of all the tables;
		String[] cols = {"table_name"};  // default col name
		String[] cmptr = new String[0];
		select(table, cols, cmptr);   //Table.java 
	}

public static void select(String table, String[] cols, String[] cmp){     //select(davisbase_tables,table_name,=)
	try{
		
		RandomAccessFile file = new RandomAccessFile("data/"+table+".tbl", "rw");
		String[] columnName = Table.getColName(table);  // arrayList to stores col names
		String[] type = Table.getDataType(table);      // 
		
		Buffer buffer = new Buffer();
		
		Table.filter(file, cmp, columnName, type, buffer);
		buffer.display(cols);
		file.close();
	}catch(Exception e){
		System.out.println(e);
	}
}

}
 