
public class Insert {
	 public static void parseInsertString(String insertString) {
			System.out.println("INSERT METHOD");
			System.out.println("Parsing the string:\"" + insertString + "\"");
			
			String[] tokens=insertString.split(" ");
			String table = tokens[2];
			String[] temp = insertString.split("values");
			String temporary=temp[1].trim();
			String[] insert_vals = temporary.substring(1, temporary.length()-1).split(",");
			for(int i = 0; i < insert_vals.length; i++)
				insert_vals[i] = insert_vals[i].trim();
			if(!DavisBase.tableExists(table)){
				System.out.println("Table "+table+" does not exist.");
			}
			else
			{
				CreateTable.insertInto(table, insert_vals);
			}

		}
	    
}
