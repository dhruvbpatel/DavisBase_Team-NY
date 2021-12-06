import java.io.RandomAccessFile;
import java.io.File;
import java.io.IOException;
import java.util.SortedMap;
import java.io.FileReader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;



public class Index{
public static void createIndex(String tbl_name,String col_name, String curr_data_type)
{


    int hex_code = 0;
    int curr_record_size = 0;



 if(curr_data_type.equalsIgnoreCase("int"))
        {
            curr_record_size=curr_record_size+4;
            hex_code=0x06;
        }
        else if(curr_data_type.equalsIgnoreCase("tinyint"))
        {
            curr_record_size=curr_record_size+1;
            hex_code=0x04;
        }
        else if(curr_data_type.equalsIgnoreCase("smallint"))
        {
            curr_record_size=curr_record_size+2;
            hex_code=0x05;
        }
        else if(curr_data_type.equalsIgnoreCase("bigint"))
        {
            curr_record_size=curr_record_size+8;
            hex_code=0x07;
        }
        else if(curr_data_type.equalsIgnoreCase("real"))
        {
            curr_record_size=curr_record_size+4;
            hex_code=0x08;
        }
        else if(curr_data_type.equalsIgnoreCase("double"))
        {
            curr_record_size=curr_record_size+8;
            hex_code=0x09;
        }
        else if(curr_data_type.equalsIgnoreCase("datetime"))
        {
            curr_record_size=curr_record_size+8;
            hex_code=0x0A;
        }

        else if(curr_data_type.equalsIgnoreCase("date"))
        {
            curr_record_size=curr_record_size+8;
            hex_code=0x0B;
        }

        else if(curr_data_type.equalsIgnoreCase("text"))
        {
            //curr_record_size=curr_record_size+8;
            hex_code=0x0C;
        }
    

        int ord_pos = 0;

 ArrayList<String> ord_position = new ArrayList<>();
 

    for(int o=0;o<16;o++){
        ord_position.add(".");
    }

try{
                
    RandomAccessFile column_file=new RandomAccessFile("data/"+ col_name +".ndx", "rw"); // read the index file columns
        column_file.setLength(512);
        column_file.seek(0);
        column_file.writeByte(hex_code);
        int null_val = 1;

        column_file.writeByte(null_val);
        column_file.write(ord_pos);
        ord_pos++;
        column_file.writeByte(0x00);
        column_file.writeByte(0x10);
    
    }
    catch (Exception e)
    {
        System.out.println(e);
    }    

}
}

