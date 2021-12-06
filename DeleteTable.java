
import java.io.IOException;
import java.util.Scanner;
import java.io.RandomAccessFile;

import java.io.File;
import java.util.ArrayList;
import java.util.SortedMap;
import java.io.FileReader;

import java.util.Arrays;
import java.io.*;
import java.util.*;




public class DeleteTable {


	public static void parseDeleteString(String QueryString) {

		System.out.println("STACKTRACE: DELETE METHOD CALLED");

		// System.out.println("Parsing the string:\"" + QueryString + "\"");
		
		String[] tokens = QueryString.split(" ");

		String table = tokens[3];


		String[] temp = QueryString.split("where");


		String cmpTemp = temp[1];

		String[] cmp = DavisBase.parserEquation(cmpTemp);

		if(!DavisBase.checkTableExists(table)){
			System.out.println("Table "+table+" does not exist.");
		}
		else
		{
			delete(table, cmp);
		}
	}



	public static void delete(String table, String[] cmp){
		try{

		int keyVal = new Integer(cmp[2]);

		RandomAccessFile file = new RandomAccessFile("data/"+table+".tbl", "rw");


		int num_pages = Table.pages(file);

		int page = 0;

		for(int iter = 1; iter <= num_pages; iter++)
			if(Page.has_key(file, iter, keyVal)&Page.getPageType(file, iter)==0x0D){

				page = iter;

				break;
			}
		

		if(page==0)
		{
			System.out.println("KeyERROR: keyVal doesn't exist");
			return;
		}
		
		short[] cell_addr = Page.getCellArray(file, page);

		int k = 0;

 
		for(int i = 0; i < cell_addr.length; i++)
		{
			long cell_loc = Page.getCellLoc(file, page, i);

			String[] retrieved_vals = Table.retrieveValues(file, cell_loc);

			int curr = new Integer(retrieved_vals[0]);

			if(curr!=keyVal)
			{
				Page.setCellOffset(file, page, k, cell_addr[i]);
				k++;
			}
		}


		Page.setCellNumber(file, page, (byte)k);
		
		}catch(Exception e)
		{
			System.out.println(e);
		}
		
	}

}
