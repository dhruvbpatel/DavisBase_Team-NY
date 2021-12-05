import java.io.RandomAccessFile;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.SortedMap;


public class Init{

	
public static int pageSize = 512;
public static void init(){
		try {
			File dataDir = new File("data");
			if(dataDir.mkdir()){
				System.out.println("The data base doesn't exit, initializing data base...");
				initialize();
			}
			else {
				
				String[] oldTableFiles = dataDir.list();
				boolean checkTab = false;
				boolean checkCol = false;
				for (int i=0; i<oldTableFiles.length; i++) {
					if(oldTableFiles[i].equals("davisbase_tables.tbl"))
						checkTab = true;
					if(oldTableFiles[i].equals("davisbase_columns.tbl"))
						checkCol = true;
				}
				
				if(!checkTab){
					System.out.println("The davisbase_tables does not exit, initializing data base...");
					System.out.println();
					initialize();
				}
				
				if(!checkCol){
					System.out.println("The davisbase_columns table does not exit, initializing data base...");
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
			File dataDir = new File("data");
			dataDir.mkdir();
			String[] oldTableFiles;
			oldTableFiles = dataDir.list();
			for (int i=0; i<oldTableFiles.length; i++) {
				File anOldFile = new File(dataDir, oldTableFiles[i]); 
				anOldFile.delete();
			}
		}
		catch (SecurityException e) {
			System.out.println(e);
		}

		try {
			RandomAccessFile tablesCatalog = new RandomAccessFile("data/davisbase_tables.tbl", "rw");
			tablesCatalog.setLength(pageSize);
			tablesCatalog.seek(0);
			tablesCatalog.write(0x0D); 
			tablesCatalog.writeByte(0x02); 
			
			int size1=24;
			int size2=25;
			
			int offsetT=pageSize-size1;  // 512-24 = 488
			int offsetC=offsetT-size2;	// 488-25 = 463
			
			tablesCatalog.writeShort(offsetC); 
			tablesCatalog.writeInt(0);
			tablesCatalog.writeInt(0);
			tablesCatalog.writeShort(offsetT);
			tablesCatalog.writeShort(offsetC);
			
			tablesCatalog.seek(offsetT);
			tablesCatalog.writeShort(20);
			tablesCatalog.writeInt(1); 
			tablesCatalog.writeByte(1);
			tablesCatalog.writeByte(28);
			tablesCatalog.writeBytes("davisbase_tables");
			
			tablesCatalog.seek(offsetC);
			tablesCatalog.writeShort(21);
			tablesCatalog.writeInt(2); 
			tablesCatalog.writeByte(1);
			tablesCatalog.writeByte(29);
			tablesCatalog.writeBytes("davisbase_columns");
			
			tablesCatalog.close();
		}
		catch (Exception e) {
			System.out.println(e);
		}
		
		try {
			RandomAccessFile columnsCatalog = new RandomAccessFile("data/davisbase_columns.tbl", "rw");
			columnsCatalog.setLength(pageSize);
			columnsCatalog.seek(0);       
			columnsCatalog.writeByte(0x0D); //leaf table of b-tree
			columnsCatalog.writeByte(0x08); //An array of 2-byte integers that indicate the page offset location of each data cell.The array size is 2n, where n is the number of cells on the page. The array ismaintained in key-sorted order—i.e. rowid order for a table file and index order for an index file.
			
			int[] offset=new int[10]; //[469,422,378,330,281,234,177,128]
			offset[0]=pageSize-43; // 469
			offset[1]=offset[0]-47;// 422
			offset[2]=offset[1]-44;// 378
			offset[3]=offset[2]-48;// 330
			offset[4]=offset[3]-49;// 281
			offset[5]=offset[4]-47;// 234
			offset[6]=offset[5]-57;// 177
			offset[7]=offset[6]-49;// 128
			
			columnsCatalog.writeShort(offset[7]); 
			columnsCatalog.writeInt(0); 
			columnsCatalog.writeInt(0); 
			
			for(int i=0;i<8;i++)
				columnsCatalog.writeShort(offset[i]);

			
			columnsCatalog.seek(offset[0]);
			columnsCatalog.writeShort(33);
			columnsCatalog.writeInt(1); 
			columnsCatalog.writeByte(5);
			columnsCatalog.writeByte(28);
			columnsCatalog.writeByte(17);
			columnsCatalog.writeByte(15);
			columnsCatalog.writeByte(4);
			columnsCatalog.writeByte(14);
			columnsCatalog.writeBytes("davisbase_tables"); 
			columnsCatalog.writeBytes("rowid"); 
			columnsCatalog.writeBytes("INT"); 
			columnsCatalog.writeByte(1); 
			columnsCatalog.writeBytes("NO"); 	
			
			columnsCatalog.seek(offset[1]);
			columnsCatalog.writeShort(39); 
			columnsCatalog.writeInt(2); 
			columnsCatalog.writeByte(5);
			columnsCatalog.writeByte(28);
			columnsCatalog.writeByte(22);
			columnsCatalog.writeByte(16);
			columnsCatalog.writeByte(4);
			columnsCatalog.writeByte(14);
			columnsCatalog.writeBytes("davisbase_tables"); 
			columnsCatalog.writeBytes("table_name"); 
			columnsCatalog.writeBytes("TEXT"); 
			columnsCatalog.writeByte(2);
			columnsCatalog.writeBytes("NO"); 
			
			columnsCatalog.seek(offset[2]);
			columnsCatalog.writeShort(34); 
			columnsCatalog.writeInt(3); 
			columnsCatalog.writeByte(5);
			columnsCatalog.writeByte(29);
			columnsCatalog.writeByte(17);
			columnsCatalog.writeByte(15);
			columnsCatalog.writeByte(4);
			columnsCatalog.writeByte(14);
			columnsCatalog.writeBytes("davisbase_columns");
			columnsCatalog.writeBytes("rowid");
			columnsCatalog.writeBytes("INT");
			columnsCatalog.writeByte(1);
			columnsCatalog.writeBytes("NO");
			
			columnsCatalog.seek(offset[3]);
			columnsCatalog.writeShort(40);
			columnsCatalog.writeInt(4); 
			columnsCatalog.writeByte(5);
			columnsCatalog.writeByte(29);
			columnsCatalog.writeByte(22);
			columnsCatalog.writeByte(16);
			columnsCatalog.writeByte(4);
			columnsCatalog.writeByte(14);
			columnsCatalog.writeBytes("davisbase_columns");
			columnsCatalog.writeBytes("table_name");
			columnsCatalog.writeBytes("TEXT");
			columnsCatalog.writeByte(2);
			columnsCatalog.writeBytes("NO");
			
			columnsCatalog.seek(offset[4]);
			columnsCatalog.writeShort(41);
			columnsCatalog.writeInt(5); 
			columnsCatalog.writeByte(5);
			columnsCatalog.writeByte(29);
			columnsCatalog.writeByte(23);
			columnsCatalog.writeByte(16);
			columnsCatalog.writeByte(4);
			columnsCatalog.writeByte(14);
			columnsCatalog.writeBytes("davisbase_columns");
			columnsCatalog.writeBytes("column_name");
			columnsCatalog.writeBytes("TEXT");
			columnsCatalog.writeByte(3);
			columnsCatalog.writeBytes("NO");
			
			columnsCatalog.seek(offset[5]);
			columnsCatalog.writeShort(39);
			columnsCatalog.writeInt(6); 
			columnsCatalog.writeByte(5);
			columnsCatalog.writeByte(29);
			columnsCatalog.writeByte(21);
			columnsCatalog.writeByte(16);
			columnsCatalog.writeByte(4);
			columnsCatalog.writeByte(14);
			columnsCatalog.writeBytes("davisbase_columns");
			columnsCatalog.writeBytes("data_type");
			columnsCatalog.writeBytes("TEXT");
			columnsCatalog.writeByte(4);
			columnsCatalog.writeBytes("NO");
			
			columnsCatalog.seek(offset[6]);
			columnsCatalog.writeShort(49); 
			columnsCatalog.writeInt(7); 
			columnsCatalog.writeByte(5);
			columnsCatalog.writeByte(29);
			columnsCatalog.writeByte(28);
			columnsCatalog.writeByte(19);
			columnsCatalog.writeByte(4);
			columnsCatalog.writeByte(14);
			columnsCatalog.writeBytes("davisbase_columns");
			columnsCatalog.writeBytes("ordinal_position");
			columnsCatalog.writeBytes("TINYINT");
			columnsCatalog.writeByte(5);
			columnsCatalog.writeBytes("NO");
			
			columnsCatalog.seek(offset[7]);
			columnsCatalog.writeShort(41); 
			columnsCatalog.writeInt(8); 
			columnsCatalog.writeByte(5);
			columnsCatalog.writeByte(29);
			columnsCatalog.writeByte(23);
			columnsCatalog.writeByte(16);
			columnsCatalog.writeByte(4);
			columnsCatalog.writeByte(14);
			columnsCatalog.writeBytes("davisbase_columns");
			columnsCatalog.writeBytes("is_nullable");
			columnsCatalog.writeBytes("TEXT");
			columnsCatalog.writeByte(6);
			columnsCatalog.writeBytes("NO");
			
			columnsCatalog.close();
		}
		catch (Exception e) {
			System.out.println(e);
		}
}

}
