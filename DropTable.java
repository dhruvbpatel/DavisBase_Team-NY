
import java.util.Arrays;
import java.io.*;
import java.io.IOException;
import java.util.ArrayList;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Scanner;

import java.util.*;
import java.util.SortedMap;
import java.io.RandomAccessFile;
import java.io.File;

 


public class DropTable {


	public static void dropTable(String QueryString) {

		System.out.println("STACKTRACE: DROP METHOD CALLED");
		System.out.println();
		// System.out.println("Parsing the string:\"" + QueryString + "\"");
		
		String[] tokens = QueryString.split(" ");

		String tbl_name = tokens[2];

		if(!DavisBase.checkTableExists(tbl_name)){
			System.out.println("Table "+tbl_name+" does not exist.");
		}
		else
		{
			drop(tbl_name);
		}		

	}

	
	public static void drop(String tbl_name){
		try{
			
			RandomAccessFile file = new RandomAccessFile("data/davisbase_tables.tbl", "rw");

			int num_pages = Table.pages(file);

			for(int page_iter = 1; page_iter <= num_pages; page_iter ++){


				file.seek((page_iter-1)*Table.size_of_page); // go to the start pos in file

				byte fileType = file.readByte();

				if(fileType == 0x0D) // if leaf
				{
					short[] cell_addr = Page.getCellArray(file, page_iter);

					int k = 0;

					for(int i = 0; i < cell_addr.length; i++)
					{
						long cell_loc = Page.getCellLoc(file, page_iter, i);

						String[] retrieve_vals = Table.retrieveValues(file, cell_loc);

						String tb = retrieve_vals[1];
						if(!tb.equals(tbl_name))
						{
							Page.setCellOffset(file, page_iter, k, cell_addr[i]);
							k++;
						}
					}

					Page.setCellNumber(file, page_iter, (byte)k);
				}
				else
					continue;
			}

			file = new RandomAccessFile("data/davisbase_columns.tbl", "rw");

			num_pages = Table.pages(file);

			for(int page_iter = 1; page_iter <= num_pages; page_iter ++){

				file.seek((page_iter-1)*Table.size_of_page);

				byte fileType = file.readByte();

				if(fileType == 0x0D)  // if leaf
				{
					short[] cell_addr = Page.getCellArray(file, page_iter);

					int k = 0;

					for(int i = 0; i < cell_addr.length; i++)

					{
						long cell_loc = Page.getCellLoc(file, page_iter, i);
						String[] retrieve_vals = Table.retrieveValues(file, cell_loc);

						String tb = retrieve_vals[1];

						if(!tb.equals(tbl_name))
						{
							Page.setCellOffset(file, page_iter, k, cell_addr[i]);
							k++;
						}
					}
					Page.setCellNumber(file, page_iter, (byte)k);

				}
				else
					continue;
			}

			File toDelete_file = new File("data", tbl_name+".tbl"); 

			toDelete_file.delete(); // delete our table file

		}catch(Exception e){
			System.out.println(e);
		}

	}

}
