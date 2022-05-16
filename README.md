
  

# Davisbase - Custom SQL DataBase engine from scratch

The goal of this project is to implement a (very) rudimentary database engine that is based on a simplified file-per-table variation on the SQLite file format, which we call DavisBase. Your implementation should operate entirely from the command line and possibly API calls (no GUI).

**Abstract:**

- Data is encoded in two different kinds of database files—tables files and index files.
 - Each  database file is stored as a single file in the underlying OS.  
- Each DB file is comprised of one or more pages (a virtual subdivision of the file). 
- All pages of a file are  the same size.
	-  For example, if the page size is set to 1024 bytes (1kb), then each DB file size is some  multiple of 1024 bytes.  
-  Each page in a Table file (interior or leaf) is a node in a B+1 tree.  
- Each page in a Table file (interior or leaf) is a node in a B tree.

## Supported Functionalities:

  
- show tables

- create table

- drop table
- create index
- Insert
- Update
- Delete
- Where
  

### dependencies
- JDK 8


  
## How to run:

Download all the project files:

Note: Make sure all files are in the same dir


#### run command

After installing all the dependencies run in CMD

    java Davisbase

   
   This will run the database engine
   
   **Note**: all the file tables and indexes will be created in your current project directory.

