import java.util.HashMap;

class Buffer{
	
	public int b_row_num; 
	public HashMap<Integer, String[]> b_contents;
	public String[] name_of_the_column; 
	public int[] b_format; 
	
	public Buffer(){
		b_row_num = 0;
		b_contents = new HashMap<Integer, String[]>();
	}

	public void value_adds(int rowid, String[] val){
		b_contents.put(rowid, val);
		b_row_num = b_row_num + 1;
	}

	public String b_fix(int b_len, String s) {
		return String.format("%-"+(b_len+3)+"s", s);
	}


	public void b_disp(String[] col){
		
		if(b_row_num == 0){
			System.out.println("EMPTY RESULTSET: Returned Empty resultset.");
		}
		else{
			for(int i = 0; i < b_format.length; i++)
				b_format[i] = name_of_the_column[i].length();
			for(String[] i : b_contents.values())
				for(int j = 0; j < i.length; j++)
					if(b_format[j] < i[j].length())
						b_format[j] = i[j].length();
			
			if(col[0].equals("*")){
				
				for(int l: b_format)
					System.out.print(DavisBase.line("-", l+3));
				
				System.out.println();
				
				for(int i = 0; i< name_of_the_column.length; i++)
					System.out.print(b_fix(b_format[i], name_of_the_column[i])+"|");
				
				System.out.println();
				
				for(int l: b_format)
					System.out.print(DavisBase.line("-", l+3));
				
				System.out.println();

				for(String[] i : b_contents.values()){
					for(int j = 0; j < i.length; j++)
						System.out.print(b_fix(b_format[j], i[j])+"|");
					System.out.println();
				}
			
			}
			else{
				int[] control = new int[col.length];
				for(int j = 0; j < col.length; j++)
					for(int i = 0; i < name_of_the_column.length; i++)
						if(col[j].equals(name_of_the_column[i]))
							control[j] = i;

				for(int j = 0; j < control.length; j++)
					System.out.print(DavisBase.line("-", b_format[control[j]]+3));
				
				System.out.println();
				
				for(int j = 0; j < control.length; j++)
					System.out.print(b_fix(b_format[control[j]], name_of_the_column[control[j]])+"|");
				
				System.out.println();
				
				for(int j = 0; j < control.length; j++)
					System.out.print(DavisBase.line("-", b_format[control[j]]+3));
				
				System.out.println();
				
				for(String[] i : b_contents.values()){
					for(int j = 0; j < control.length; j++)
						System.out.print(b_fix(b_format[control[j]], i[control[j]])+"|");
					System.out.println();
				}
				System.out.println();
			}
		}
	}
}