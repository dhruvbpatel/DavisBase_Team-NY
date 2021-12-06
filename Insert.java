import java.util.ArrayList;
import java.io.File;
import java.io.RandomAccessFile;
import java.io.FileReader;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.Arrays;
import java.io.IOException;
import java.io.*;
import java.util.*;


public class Insert {

	 public static void parseInsertString(String QueryString) {
			System.out.println("STACKTRACE: INSERT METHOD CALLED");
			System.out.println();
			
			
			String[] tokens=QueryString.split(" ");

			String tbl = tokens[2];

			String[] tmp_val = QueryString.split("values");

			String tmp_str=tmp_val[1].trim();


			String[] insert_vals = tmp_str.substring(1, tmp_str.length()-1).split(",");
			
			for(int i = 0; i < insert_vals.length; i++)

				insert_vals[i] = insert_vals[i].trim();

			
				if(!DavisBase.checkTableExists(tbl)){

					System.out.println("Table "+tbl+" does not exist.");

			}
			else
			{
				CreateTable.insertInto(tbl, insert_vals);
			}

		}
	    
}
