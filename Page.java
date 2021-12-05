import java.io.RandomAccessFile;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Date;
import java.text.SimpleDateFormat;

public class Page{
	public static int size_of_page = 512;
	public static final String date_pattern = "yyyy-MM-dd_HH:mm:ss";

public static short calPayloadSize(String[] values, String[] dataType){
		int val = dataType.length; 
		for(int i = 1; i < dataType.length; i++){
			String dt = dataType[i];
			switch(dt){
				case "TINYINT":
					val = val + 1;
					break;
				case "SMALLINT":
					val = val + 2;
					break;
				case "INT":
					val = val + 4;
					break;
				case "BIGINT":
					val = val + 8;
					break;
				case "REAL":
					val = val + 4;
					break;		
				case "DOUBLE":
					val = val + 8;
					break;
				case "DATETIME":
					val = val + 8;
					break;
				case "DATE":
					val = val + 8;
					break;
				case "TEXT":
					String text = values[i];
					int len = text.length();
					val = val + len;
					break;
				default:
					break;
			}
		}
		return (short)val;
	}

	public static int makeInteriorPage(RandomAccessFile file){
		int number_pages = 0;
		try{
			number_pages = (int)(file.length()/(new Long(size_of_page)));
			number_pages = number_pages + 1;
			file.setLength(size_of_page * number_pages);
			file.seek((number_pages-1)*size_of_page);
			file.writeByte(0x05); 
		}catch(Exception e){
			System.out.println(e);
		}

		return number_pages;
	}

	public static int makeLeafPage(RandomAccessFile file){
		int number_pages = 0;
		try{
			number_pages = (int)(file.length()/(new Long(size_of_page)));
			number_pages = number_pages + 1;
			file.setLength(size_of_page * number_pages);
			file.seek((number_pages-1)*size_of_page);
			file.writeByte(0x0D); 
		}catch(Exception e){
			System.out.println(e);
		}

		return number_pages;

	}

	public static int findMidKey(RandomAccessFile file, int page){
		int val = 0;
		try{
			file.seek((page-1)*size_of_page);
			byte pageType = file.readByte();
			int number_cells = getCellNumber(file, page);
			int mid = (int) Math.ceil((double) number_cells / 2);
			long loc = getCellLoc(file, page, mid-1);
			file.seek(loc);

			switch(pageType){
				case 0x05:
					file.readInt(); 
					val = file.readInt();
					break;
				case 0x0D:
					file.readShort();
					val = file.readInt();
					break;
			}

		}catch(Exception e){
			System.out.println(e);
		}

		return val;
	}

	
	public static void splitLeafPage(RandomAccessFile file, int curPage, int new_page){
		try{
			
			int number_cells = getCellNumber(file, curPage);
			
			int mid = (int) Math.ceil((double) number_cells / 2);

			int number_cells_A = mid - 1;
			int number_cells_B = number_cells - number_cells_A;
			int contents = 512;

			for(int i = number_cells_A; i < number_cells; i++){
				long loc = getCellLoc(file, curPage, i);
				file.seek(loc);
				int cell_size = file.readShort()+6;
				contents = contents - cell_size;
				file.seek(loc);
				byte[] cell = new byte[cell_size];
				file.read(cell);
				file.seek((new_page-1)*size_of_page+contents);
				file.write(cell);
				setCellOffset(file, new_page, i - number_cells_A, contents);
			}

			
			file.seek((new_page-1)*size_of_page+2);
			file.writeShort(contents);

			
			short offset = getCellOffset(file, curPage, number_cells_A-1);
			file.seek((curPage-1)*size_of_page+2);
			file.writeShort(offset);

			
			int right_most = get_right_most(file, curPage);
			set_right_most(file, new_page, right_most);
			set_right_most(file, curPage, new_page);

			
			int parent = get_parent(file, curPage);
			set_parent(file, new_page, parent);

			
			byte num = (byte) number_cells_A;
			setCellNumber(file, curPage, num);
			num = (byte) number_cells_B;
			setCellNumber(file, new_page, num);
			
		}catch(Exception e){
			System.out.println(e);
			
		}
	}
	
	public static void splitInteriorPage(RandomAccessFile file, int curPage, int new_page){
		try{
			
			int number_cells = getCellNumber(file, curPage);
			
			int mid = (int) Math.ceil((double) number_cells / 2);

			int number_cells_A = mid - 1;
			int number_cells_B = number_cells - number_cells_A - 1;
			short contents = 512;

			for(int i = number_cells_A+1; i < number_cells; i++){
				long loc = getCellLoc(file, curPage, i);
				short cell_size = 8;
				contents = (short)(contents - cell_size);
				file.seek(loc);
				byte[] cell = new byte[cell_size];
				file.read(cell);
				file.seek((new_page-1)*size_of_page+contents);
				file.write(cell);
				file.seek(loc);
				int page = file.readInt();
				set_parent(file, page, new_page);
				setCellOffset(file, new_page, i - (number_cells_A + 1), contents);
			}
			
			int tmp = get_right_most(file, curPage);
			set_right_most(file, new_page, tmp);
			
			long midLoc = getCellLoc(file, curPage, mid - 1);
			file.seek(midLoc);
			tmp = file.readInt();
			set_right_most(file, curPage, tmp);
			
			file.seek((new_page-1)*size_of_page+2);
			file.writeShort(contents);
			
			short offset = getCellOffset(file, curPage, number_cells_A-1);
			file.seek((curPage-1)*size_of_page+2);
			file.writeShort(offset);

			
			int parent = get_parent(file, curPage);
			set_parent(file, new_page, parent);
			
			byte num = (byte) number_cells_A;
			setCellNumber(file, curPage, num);
			num = (byte) number_cells_B;
			setCellNumber(file, new_page, num);
			
		}catch(Exception e){
			System.out.println(e);
		}
	}

	
	public static void splitLeaf(RandomAccessFile file, int page){
		int new_page = makeLeafPage(file);
		int middle_key = findMidKey(file, page);
		splitLeafPage(file, page, new_page);
		int parent = get_parent(file, page);
		if(parent == 0){
			int root_page = makeInteriorPage(file);
			set_parent(file, page, root_page);
			set_parent(file, new_page, root_page);
			set_right_most(file, root_page, new_page);
			insert_interior_cell(file, root_page, page, middle_key);
		}else{
			long ploc = get_pointer_location(file, page, parent);
			set_pointer_location(file, ploc, parent, new_page);
			insert_interior_cell(file, parent, page, middle_key);
			sortCellArray(file, parent);
			while(checkInteriorSpace(file, parent)){
				parent = splitInterior(file, parent);
			}
		}
	}

	public static int splitInterior(RandomAccessFile file, int page){
		int new_page = makeInteriorPage(file);
		int middle_key = findMidKey(file, page);
		splitInteriorPage(file, page, new_page);
		int parent = get_parent(file, page);
		if(parent == 0){
			int root_page = makeInteriorPage(file);
			set_parent(file, page, root_page);
			set_parent(file, new_page, root_page);
			set_right_most(file, root_page, new_page);
			insert_interior_cell(file, root_page, page, middle_key);
			return root_page;
		}else{
			long ploc = get_pointer_location(file, page, parent);
			set_pointer_location(file, ploc, parent, new_page);
			insert_interior_cell(file, parent, page, middle_key);
			sortCellArray(file, parent);
			return parent;
		}
	}

	
	public static void sortCellArray(RandomAccessFile file, int page){
		 byte num = getCellNumber(file, page);
		 int[] keys_array = getKeyArray(file, page);
		 short[] cells_array = getCellArray(file, page);
		 int ltmp;
		 short rtmp;

		 for (int i = 1; i < num; i++) {
            for(int j = i ; j > 0 ; j--){
                if(keys_array[j] < keys_array[j-1]){

                    ltmp = keys_array[j];
                    keys_array[j] = keys_array[j-1];
                    keys_array[j-1] = ltmp;

                    rtmp = cells_array[j];
                    cells_array[j] = cells_array[j-1];
                    cells_array[j-1] = rtmp;
                }
            }
         }

         try{
         	file.seek((page-1)*size_of_page+12);
         	for(int i = 0; i < num; i++){
				file.writeShort(cells_array[i]);
			}
         }catch(Exception e){
         	System.out.println("Error at sortCellArray");
         }
	}

	public static int[] getKeyArray(RandomAccessFile file, int page){
		int num = new Integer(getCellNumber(file, page));
		int[] array = new int[num];

		try{
			file.seek((page-1)*size_of_page);
			byte pageType = file.readByte();
			byte offset = 0;
			switch(pageType){
			    case 0x0d:
				    offset = 2;
				    break;
				case 0x05:
					offset = 4;
					break;
				default:
					offset = 2;
					break;
			}

			for(int i = 0; i < num; i++){
				long loc = getCellLoc(file, page, i);
				file.seek(loc+offset);
				array[i] = file.readInt();
			}

		}catch(Exception e){
			System.out.println(e);
		}

		return array;
	}
	
	public static short[] getCellArray(RandomAccessFile file, int page){
		int num = new Integer(getCellNumber(file, page));
		short[] array = new short[num];

		try{
			file.seek((page-1)*size_of_page+12);
			for(int i = 0; i < num; i++){
				array[i] = file.readShort();
			}
		}catch(Exception e){
			System.out.println(e);
		}

		return array;
	}

	
	public static long get_pointer_location(RandomAccessFile file, int page, int parent){
		long val = 0;
		try{
			int number_cells = new Integer(getCellNumber(file, parent));
			for(int i=0; i < number_cells; i++){
				long loc = getCellLoc(file, parent, i);
				file.seek(loc);
				int childPage = file.readInt();
				if(childPage == page){
					val = loc;
				}
			}
		}catch(Exception e){
			System.out.println(e);
		}

		return val;
	}

	public static void set_pointer_location(RandomAccessFile file, long loc, int parent, int page){
		try{
			if(loc == 0){
				file.seek((parent-1)*size_of_page+4);
			}else{
				file.seek(loc);
			}
			file.writeInt(page);
		}catch(Exception e){
			System.out.println(e);
		}
	} 

	
	public static void insert_interior_cell(RandomAccessFile file, int page, int child, int key){
		try{
			
			file.seek((page-1)*size_of_page+2);
			short contents = file.readShort();
			
			if(contents == 0)
				contents = 512;
			
			contents = (short)(contents - 8);
			
			file.seek((page-1)*size_of_page+contents);
			file.writeInt(child);
			file.writeInt(key);
			
			file.seek((page-1)*size_of_page+2);
			file.writeShort(contents);
			
			byte num = getCellNumber(file, page);
			setCellOffset(file, page ,num, contents);
			
			num = (byte) (num + 1);
			setCellNumber(file, page, num);

		}catch(Exception e){
			System.out.println(e);
		}
	}

	public static void insertLeafCell(RandomAccessFile file, int page, int offset, short plsize, int key, byte[] stc, String[] vals){
		try{
			String s;
			file.seek((page-1)*size_of_page+offset);
			file.writeShort(plsize);
			file.writeInt(key);
			int col = vals.length - 1;
			file.writeByte(col);
			file.write(stc);
			for(int i = 1; i < vals.length; i++){
				switch(stc[i-1]){
					case 0x00:
						file.writeByte(0);
						break;
					case 0x01:
						file.writeShort(0);
						break;
					case 0x02:
						file.writeInt(0);
						break;
					case 0x03:
						file.writeLong(0);
						break;
					case 0x04:
						file.writeByte(new Byte(vals[i]));
						break;
					case 0x05:
						file.writeShort(new Short(vals[i]));
						break;
					case 0x06:
						file.writeInt(new Integer(vals[i]));
						break;
					case 0x07:
						file.writeLong(new Long(vals[i]));
						break;
					case 0x08:
						file.writeFloat(new Float(vals[i]));
						break;
					case 0x09:
						file.writeDouble(new Double(vals[i]));
						break;
					case 0x0A:
						s = vals[i];
						Date temp = new SimpleDateFormat(date_pattern).parse(s.substring(1, s.length()-1));
						long time = temp.getTime();
						file.writeLong(time);
						break;
					case 0x0B:
						s = vals[i];
						s = s.substring(1, s.length()-1);
						s = s+"_00:00:00";
						Date temp2 = new SimpleDateFormat(date_pattern).parse(s);
						long time2 = temp2.getTime();
						file.writeLong(time2);
						break;
					default:
						file.writeBytes(vals[i]);
						break;
				}
			}
			int n = getCellNumber(file, page);
			byte tmp = (byte) (n+1);
			setCellNumber(file, page, tmp);
			file.seek((page-1)*size_of_page+12+n*2);
			file.writeShort(offset);
			file.seek((page-1)*size_of_page+2);
			int contents = file.readShort();
			if(contents >= offset || contents == 0){
				file.seek((page-1)*size_of_page+2);
				file.writeShort(offset);
			}
		}catch(Exception e){
			System.out.println(e);
		}
	}

	public static void updateLeafCell(RandomAccessFile file, int page, int offset, int plsize, int key, byte[] stc, String[] vals){
		try{
			String s;
			file.seek((page-1)*size_of_page+offset);
			file.writeShort(plsize);
			file.writeInt(key);
			int col = vals.length - 1;
			file.writeByte(col);
			file.write(stc);
			for(int i = 1; i < vals.length; i++){
				switch(stc[i-1]){
					case 0x00:
						file.writeByte(0);
						break;
					case 0x01:
						file.writeShort(0);
						break;
					case 0x02:
						file.writeInt(0);
						break;
					case 0x03:
						file.writeLong(0);
						break;
					case 0x04:
						file.writeByte(new Byte(vals[i]));
						break;
					case 0x05:
						file.writeShort(new Short(vals[i]));
						break;
					case 0x06:
						file.writeInt(new Integer(vals[i]));
						break;
					case 0x07:
						file.writeLong(new Long(vals[i]));
						break;
					case 0x08:
						file.writeFloat(new Float(vals[i]));
						break;
					case 0x09:
						file.writeDouble(new Double(vals[i]));
						break;
					case 0x0A:
						s = vals[i];
						Date temp = new SimpleDateFormat(date_pattern).parse(s.substring(1, s.length()-1));
						long time = temp.getTime();
						file.writeLong(time);
						break;
					case 0x0B:
						s = vals[i];
						s = s.substring(1, s.length()-1);
						s = s+"_00:00:00";
						Date temp2 = new SimpleDateFormat(date_pattern).parse(s);
						long time2 = temp2.getTime();
						file.writeLong(time2);
						break;
					default:
						file.writeBytes(vals[i]);
						break;
				}
			}
		}catch(Exception e){
			System.out.println(e);
		}
	}

	
	public static boolean checkInteriorSpace(RandomAccessFile file, int page){
		byte number_cells = getCellNumber(file, page);
		if(number_cells > 30)
			return true;
		else
			return false;
	}

	public static int checkLeafSpace(RandomAccessFile file, int page, int size){
		int val = -1;

		try{
			file.seek((page-1)*size_of_page+2);
			int contents = file.readShort();
			if(contents == 0)
				return size_of_page - size;
			int number_cells = getCellNumber(file, page);
			int space = contents - 20 - 2*number_cells;
			if(size < space)
				return contents - size;
			
		}catch(Exception e){
			System.out.println(e);
		}

		return val;
	}

	
	public static int get_parent(RandomAccessFile file, int page){
		int val = 0;

		try{
			file.seek((page-1)*size_of_page+8);
			val = file.readInt();
		}catch(Exception e){
			System.out.println(e);
		}

		return val;
	}

	public static void set_parent(RandomAccessFile file, int page, int parent){
		try{
			file.seek((page-1)*size_of_page+8);
			file.writeInt(parent);
		}catch(Exception e){
			System.out.println(e);
		}
	}
	
	public static int get_right_most(RandomAccessFile file, int page){
		int rl = 0;

		try{
			file.seek((page-1)*size_of_page+4);
			rl = file.readInt();
		}catch(Exception e){
			System.out.println("Error at get_right_most");
		}

		return rl;
	}

	public static void set_right_most(RandomAccessFile file, int page, int rightLeaf){

		try{
			file.seek((page-1)*size_of_page+4);
			file.writeInt(rightLeaf);
		}catch(Exception e){
			System.out.println("Error at set_right_most");
		}

	}

	public static boolean has_key(RandomAccessFile file, int page, int key){
		int[] keys = getKeyArray(file, page);
		for(int i : keys)
			if(key == i)
				return true;
		return false;
	}
	
	public static long getCellLoc(RandomAccessFile file, int page, int id){
		long loc = 0;
		try{
			file.seek((page-1)*size_of_page+12+id*2);
			short offset = file.readShort();
			long orig = (page-1)*size_of_page;
			loc = orig + offset;
			
		}catch(Exception e){
			System.out.println(e);
		}
		return loc;
	}

	public static byte getCellNumber(RandomAccessFile file, int page){
		byte val = 0;

		try{
			file.seek((page-1)*size_of_page+1);
			val = file.readByte();
			// System.out.println(val);
		}catch(Exception e){
			System.out.println(e);
		}

		return val;
	}

	public static void setCellNumber(RandomAccessFile file, int page, byte num){
		try{
			file.seek((page-1)*size_of_page+1);
			file.writeByte(num);
		}catch(Exception e){
			System.out.println(e);
		}
	}
	
	public static short getCellOffset(RandomAccessFile file, int page, int id){
		short offset = 0;
		try{
			file.seek((page-1)*size_of_page+12+id*2);
			offset = file.readShort();
		}catch(Exception e){
			System.out.println(e);
		}
		return offset;
	}

	public static void setCellOffset(RandomAccessFile file, int page, int id, int offset){
		try{
			file.seek((page-1)*size_of_page+12+id*2);
			file.writeShort(offset);
		}catch(Exception e){
			System.out.println(e);
		}
	}
    
	public static byte getPageType(RandomAccessFile file, int page){
		byte type=0x05;
		try {
			file.seek((page-1)*size_of_page);
			type = file.readByte();
		} catch (Exception e) {
			System.out.println(e);
		}
		return type;
	}
	//public static void main(String[] args){}
}















