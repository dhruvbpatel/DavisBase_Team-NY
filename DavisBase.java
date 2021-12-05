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

// merged settings.java with davisbase.java


public class DavisBase {


	public static int size_of_page = 512;  // define global size_of_page

	static String prompt = "davisql> ";

	static String copyright = "Created By Group New York";


	static String version = "v0.01";

	static boolean isExit = false;
		

	static Scanner scanner = new Scanner(System.in).useDelimiter(";"); // read input till delimeter
	
	
    public static void main(String[] args) {
    	Init.init(); // initialization code creating data folder and tables;
		
		splashScreen();

		String userCommand = ""; 

		while(!isExit) {
			System.out.print(prompt); // print use prompt
			userCommand = scanner.next().replace("\n", " ").replace("\r", "").trim().toLowerCase(); 
			// System.out.println(userCommand);
			parseUserCommand(userCommand);
		}
		System.out.println("Exiting...");

	}
	
	// merged Utils java

    public static void splashScreen() {
		System.out.println(line("-",82));
        System.out.println("Welcome to DavisBase");
		System.out.println("DavisBase Version " + version);
		System.out.println(copyright);
		System.out.println("\nType \"help;\" to List supported commands.");
		System.out.println(line("-",82));
	}
	

	// function to return dashed lines
	public static String line(String s,int num) {
		String a = "";
		for(int i=0;i<num;i++) {
			a += s;
		}
		return a;
	}
	
	// help function to display all commands supported.
	public static void help() {
		System.out.println(line("*",82));
		System.out.println("SUPPORTED COMMANDS");
		System.out.println("All commands below are case insensitive");
		System.out.println(); 
		System.out.println(); 
		System.out.println("\tSHOW TABLES;                                                 Display all the tables in the database.");
		System.out.println("\tCREATE TABLE table_name (<column_name datatype>);            Create a new table in the database.");
		System.out.println("\tSELECT * FROM table_name;                                    Display all records in the table.");
		System.out.println("\tSELECT * FROM table_name WHERE column_name operator value;   Display records in the table where the given condition is satisfied.");
		System.out.println("\tINSERT INTO table_name VALUES (value1,value2,..);            Insert a new record into the table.");
		System.out.println("\tDELETE FROM TABLE table_name WHERE row_id = key_value;       Delete a record from the table whose rowid is <key_value>.");
		System.out.println("\tUPDATE table_name SET column_name = value WHERE row_id = ..; Modifies the records in the table.");
		System.out.println("\tCREATE INDEX ON table_name (column_name);                    Create index for the specified column in the table");
		
		System.out.println("\tDROP TABLE table_name;                                       Remove table data (i.e All records) and its schema.");
		System.out.println("\tVERSION;                                                     Display the program version.");
		System.out.println("\tHELP;                                                        Display this help information.");
		System.out.println("\tEXIT;                                                        Exit the program.");
		System.out.println();
		System.out.println();
		System.out.println(line("*",82));
	}


	
	public static boolean checkTableExists(String tablename){
		tablename = tablename+".tbl";
		
		try {

			File dataDir = new File("data"); // initialize data folder

			String[] oldTableFilesArray; // arraylist of tables names

			oldTableFilesArray = dataDir.list();

			for (int i=0; i<oldTableFilesArray.length; i++) {

				if(oldTableFilesArray[i].equals(tablename))
					return true;
			}
		}


		catch (SecurityException se) {

			System.out.println("Failed to create data directory");
			System.out.println(se);
		}

		return false;
	}

	
	// parser Function to parse the string eqn with operators
	public static String[] parserEquation(String eqn){

		String comparator[] = new String[3];

		String temp[] = new String[2];

		if(eqn.contains("=")) {

			temp = eqn.split("=");
			comparator[0] = temp[0].trim();
			comparator[1] = "=";
			comparator[2] = temp[1].trim();
		}

		if(eqn.contains("<")) {
			temp = eqn.split("<");
			comparator[0] = temp[0].trim();
			comparator[1] = "<";
			comparator[2] = temp[1].trim();
	
		}
		
		if(eqn.contains(">")) {
			temp = eqn.split(">");
			comparator[0] = temp[0].trim();
			comparator[1] = ">";
			comparator[2] = temp[1].trim();
		}
		
		if(eqn.contains("<=")) {
			temp = eqn.split("<=");
			comparator[0] = temp[0].trim();
			comparator[1] = "<=";
			comparator[2] = temp[1].trim();
		}

		if(eqn.contains(">=")) {
			temp = eqn.split(">=");
			comparator[0] = temp[0].trim();
			comparator[1] = ">=";
			comparator[2] = temp[1].trim();
		}
		
		
		return comparator;
	}
		
	public static void parseUserCommand (String userCommand) {
		
		ArrayList<String> commandTokens = new ArrayList<String>(Arrays.asList(userCommand.split(" ")));

		switch (commandTokens.get(0)) {

		    case "show":
		    	System.out.println("STUB: Calling the method to process the command (SHOW)");
			    ShowTables.showTables();
			    break;
			
		    case "create":
		    	System.out.println("STUB: Calling the method to process the command (CREATE");
				CreateTable.parseCreateString(userCommand);
			    break;

			case "insert":
				System.out.println("STUB: Calling the method to process the command (INSERT)");
				Insert.parseInsertString(userCommand);
				break;
				
			case "delete":
				System.out.println("STUB: Calling the method to process the command (DELETE)");
				DeleteTable.parseDeleteString(userCommand);
				break;	

			case "update":
				System.out.println("STUB: Calling the method to process the command (UPDATE)");
				UpdateTable.parseUpdateString(userCommand);
				break;
				
			case "select":
				System.out.println("STUB: Calling the method to process the command (SELECT)");
				parseQueryString(userCommand);
				break;

			case "drop":
				System.out.println("STUB: Calling the method to process the command (DROP)");
				DropTable.dropTable(userCommand);
				break;	

			case "help":
				help();
				break;

			case "version":
				System.out.println("DavisBase Version " + version);
				System.out.println(copyright);
				break;

			case "exit":
				isExit=true;
				break;
				
			case "quit":
				isExit=true;
				break;
	
			default:
				System.out.println("I didn't understand the command: \"" + userCommand + "\"");
				System.out.println();
				break;
		}
	} 


    public static void parseQueryString(String queryString) {
		System.out.println("STUB: Calling the method to process the command");
		System.out.println("Parsing the string:\"" + queryString + "\"");
		
		String[] cmp;
		String[] column;
		String[] temp = queryString.split("where");
		if(temp.length > 1){
			String tmp = temp[1].trim();
			cmp = parserEquation(tmp);
		}
		else{
			cmp = new String[0];
		}
		String[] select = temp[0].split("from");
		String tableName = select[1].trim();
		String coloumns = select[0].replace("select", "").trim();
		if(coloumns.contains("*")){
			column = new String[1];
			column[0] = "*";
		}
		else{
			column = coloumns.split(",");
			for(int i = 0; i < column.length; i++)
				column[i] = column[i].trim();
		}
		
		if(!checkTableExists(tableName)){
			System.out.println("Table "+tableName+" does not exist.");
		}
		else
		{
		    ShowTables.select(tableName, column, cmp);
		}
	}
	
	}
		


