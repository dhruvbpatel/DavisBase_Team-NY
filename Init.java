import java.io.RandomAccessFile;
import java.io.File;
import java.util.SortedMap;
import java.util.Arrays;
import java.io.*;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.*;



public class Init{
	
	
public static int size_of_page = 512;

public static void init(){
		try {

			File data_directory = new File("data");
			if(data_directory.mkdir()){

				System.out.println("STACKTRACE: DataBase Not Found, Initializing DataBase...");
				initialize();
			}
			else {
				
				String[] data_dir_files = data_directory.list();

				boolean isTableExist = false;
				boolean isColTblExist = false;

				for (int i=0; i<data_dir_files.length; i++) {

					if(data_dir_files[i].equals("davisbase_tables.tbl"))
						isTableExist = true;

					if(data_dir_files[i].equals("davisbase_columns.tbl"))
						isColTblExist = true;
				}
				
				if(!isTableExist){
					System.out.println("STACKTRACE: davisbase_tables NOT FOUND, initializing DataBase...");
					System.out.println();
					initialize();
				}
				
				if(!isColTblExist){
					System.out.println("STACKTRACE: davisbase_columns NOT FOUND, nitializing DataBase...");
					System.out.println();
					initialize();
				}
				
			}
		}
		catch (SecurityException e) {
			System.out.println(e);
		}

	}
	
public static void initialize() {

		
		try {

			File data_directory = new File("data");
			data_directory.mkdir();
			
			String[] data_dir_files;

			data_dir_files = data_directory.list();

			for (int i=0; i<data_dir_files.length; i++) {
				File prev_file = new File(data_directory, data_dir_files[i]); 
				prev_file.delete();
			}
		}
		catch (SecurityException e) {
			System.out.println(e);
		}

		try {
			RandomAccessFile tables_list = new RandomAccessFile("data/davisbase_tables.tbl", "rw");

			tables_list.setLength(size_of_page);
			tables_list.seek(0);
			tables_list.write(0x0D); 
			tables_list.writeByte(0x02); 
			
			int size1=24;
			int size2=25;
			
			int offsetT=size_of_page-size1;  
			int offsetC=offsetT-size2;
			
			tables_list.writeShort(offsetC); 
			tables_list.writeInt(0);
			tables_list.writeInt(0);
			tables_list.writeShort(offsetT);
			tables_list.writeShort(offsetC);
			
			tables_list.seek(offsetT);
			tables_list.writeShort(20);
			tables_list.writeInt(1); 
			tables_list.writeByte(1);
			tables_list.writeByte(28);
			tables_list.writeBytes("davisbase_tables");
			
			tables_list.seek(offsetC);
			tables_list.writeShort(21);
			tables_list.writeInt(2); 
			tables_list.writeByte(1);
			tables_list.writeByte(29);
			tables_list.writeBytes("davisbase_columns");
			
			tables_list.close();
		}
		catch (Exception e) {
			System.out.println(e);
		}
		
		try {
			RandomAccessFile cols_list = new RandomAccessFile("data/davisbase_columns.tbl", "rw");

			cols_list.setLength(size_of_page);
			cols_list.seek(0);       
			cols_list.writeByte(0x0D); //Btree leaf
			cols_list.writeByte(0x08); 
			
			int[] offset=new int[10]; 
			offset[0]=size_of_page-43;
			offset[1]=offset[0]-47;
			offset[2]=offset[1]-44;
			offset[3]=offset[2]-48;
			offset[4]=offset[3]-49;
			offset[5]=offset[4]-47;
			offset[6]=offset[5]-57;
			offset[7]=offset[6]-49;
			
			cols_list.writeShort(offset[7]); 
			cols_list.writeInt(0); 
			cols_list.writeInt(0); 
			
			for(int i=0;i<8;i++)
				cols_list.writeShort(offset[i]);

			
			cols_list.seek(offset[0]);
			cols_list.writeShort(33);
			cols_list.writeInt(1); 
			cols_list.writeByte(5);
			cols_list.writeByte(28);
			cols_list.writeByte(17);
			cols_list.writeByte(15);
			cols_list.writeByte(4);
			cols_list.writeByte(14);
			cols_list.writeBytes("davisbase_tables"); 
			cols_list.writeBytes("rowid"); 
			cols_list.writeBytes("INT"); 
			cols_list.writeByte(1); 
			cols_list.writeBytes("NO"); 	
			
			cols_list.seek(offset[1]);
			cols_list.writeShort(39); 
			cols_list.writeInt(2); 
			cols_list.writeByte(5);
			cols_list.writeByte(28);
			cols_list.writeByte(22);
			cols_list.writeByte(16);
			cols_list.writeByte(4);
			cols_list.writeByte(14);
			cols_list.writeBytes("davisbase_tables"); 
			cols_list.writeBytes("table_name"); 
			cols_list.writeBytes("TEXT"); 
			cols_list.writeByte(2);
			cols_list.writeBytes("NO"); 
			
			cols_list.seek(offset[2]);
			cols_list.writeShort(34); 
			cols_list.writeInt(3); 
			cols_list.writeByte(5);
			cols_list.writeByte(29);
			cols_list.writeByte(17);
			cols_list.writeByte(15);
			cols_list.writeByte(4);
			cols_list.writeByte(14);
			cols_list.writeBytes("davisbase_columns");
			cols_list.writeBytes("rowid");
			cols_list.writeBytes("INT");
			cols_list.writeByte(1);
			cols_list.writeBytes("NO");
			
			cols_list.seek(offset[3]);
			cols_list.writeShort(40);
			cols_list.writeInt(4); 
			cols_list.writeByte(5);
			cols_list.writeByte(29);
			cols_list.writeByte(22);
			cols_list.writeByte(16);
			cols_list.writeByte(4);
			cols_list.writeByte(14);
			cols_list.writeBytes("davisbase_columns");
			cols_list.writeBytes("table_name");
			cols_list.writeBytes("TEXT");
			cols_list.writeByte(2);
			cols_list.writeBytes("NO");
			
			cols_list.seek(offset[4]);
			cols_list.writeShort(41);
			cols_list.writeInt(5); 
			cols_list.writeByte(5);
			cols_list.writeByte(29);
			cols_list.writeByte(23);
			cols_list.writeByte(16);
			cols_list.writeByte(4);
			cols_list.writeByte(14);
			cols_list.writeBytes("davisbase_columns");
			cols_list.writeBytes("column_name");
			cols_list.writeBytes("TEXT");
			cols_list.writeByte(3);
			cols_list.writeBytes("NO");
			
			cols_list.seek(offset[5]);
			cols_list.writeShort(39);
			cols_list.writeInt(6); 
			cols_list.writeByte(5);
			cols_list.writeByte(29);
			cols_list.writeByte(21);
			cols_list.writeByte(16);
			cols_list.writeByte(4);
			cols_list.writeByte(14);
			cols_list.writeBytes("davisbase_columns");
			cols_list.writeBytes("data_type");
			cols_list.writeBytes("TEXT");
			cols_list.writeByte(4);
			cols_list.writeBytes("NO");
			
			cols_list.seek(offset[6]);
			cols_list.writeShort(49); 
			cols_list.writeInt(7); 
			cols_list.writeByte(5);
			cols_list.writeByte(29);
			cols_list.writeByte(28);
			cols_list.writeByte(19);
			cols_list.writeByte(4);
			cols_list.writeByte(14);
			cols_list.writeBytes("davisbase_columns");
			cols_list.writeBytes("ordinal_position");
			cols_list.writeBytes("TINYINT");
			cols_list.writeByte(5);
			cols_list.writeBytes("NO");
			
			cols_list.seek(offset[7]);
			cols_list.writeShort(41); 
			cols_list.writeInt(8); 
			cols_list.writeByte(5);
			cols_list.writeByte(29);
			cols_list.writeByte(23);
			cols_list.writeByte(16);
			cols_list.writeByte(4);
			cols_list.writeByte(14);
			cols_list.writeBytes("davisbase_columns");
			cols_list.writeBytes("is_nullable");
			cols_list.writeBytes("TEXT");
			cols_list.writeByte(6);
			cols_list.writeBytes("NO");
			
			cols_list.close();
		}
		catch (Exception e) {
			System.out.println(e);
		}
}

}
