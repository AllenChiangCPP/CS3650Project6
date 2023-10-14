import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

class Assembler {
       //defines 0, 1, and 2, as char for A, C, and L commands
       private static final char A_COMMAND = 0; 
       private static final char C_COMMAND = 1;
       private static final char L_COMMAND = 2;

       private Map<String, Integer> symbols = new HashMap<String, Integer>(50); //Hashmapfo r storing symbol to address mapping, initializes with predefined symbols and addresses
       private BufferedReader bufferedReader; //buffedReader for reading assembly file
       private int currentSymbolAddress = 16; //int for tracking current available adress for user defined symbols
       private int currentCodeLine = 0; //int for tracking current line number in the assembly code

       public Assembler(String file) { //constructor takes file and reads and performs a first pass over given assembly code
              //Try/catch for 
		try {
			bufferedReader = new BufferedReader(new FileReader(file)); //initializes buffedReader to open and read an assembly file
                     //constructor populates symbols hashmap with corresponding symbols and thier address to represent memory locations in assembly
                     symbols.put("SP", 0);
                     symbols.put("LCL", 1);
                     symbols.put("ARG", 2);
                     symbols.put("THIS", 3);
                     symbols.put("THAT", 4);
                     symbols.put("SCREEN", 16384);
                     symbols.put("KBD", 24576);
                     symbols.put("R0", 0);
                     symbols.put("R1", 1);
                     symbols.put("R2", 2);
                     symbols.put("R3", 3);
                     symbols.put("R4", 4);
                     symbols.put("R5", 5);
                     symbols.put("R6", 6);
                     symbols.put("R7", 7);
                     symbols.put("R8", 8);
                     symbols.put("R9", 9);
                     symbols.put("R10", 10);
                     symbols.put("R11", 11);
                     symbols.put("R12", 12);
                     symbols.put("R13", 13);
                     symbols.put("R14", 14);
                     symbols.put("R15", 15);
                     firstPass(); //firstPass method call to pass in assembly code,
                     close(); //close buffedReader
                     bufferedReader = new BufferedReader(new FileReader(file)); //reopen buffedReader to start second pass
		} 
              catch (Exception exception) { //catch exception and print details of the exception
			exception.printStackTrace();
		}
              return;
       }

       private void firstPass() throws Exception { //first pass code for input
              String nextLine = nextCommand(); //local variable nextLine, next non empty line from input assembly code
              while(nextLine != null) { //while loop inerates over each line in assembly code until none left
                     if (commandType(nextLine) == L_COMMAND) { //checks for l command
                            //adds label name as key to corresponding line number and puts it in symbols to record a label'nextLine address in the symbol table
                            //checks if a label defined multiple times, if put method returns value taht is not null, throw exception
                            if (symbols.put(nextLine.substring(1, nextLine.length() - 1), currentCodeLine) != null) 
                                   throw new Exception("ERROR. Symbol " + nextLine.substring(1, nextLine.length() - 1) + " is defined multiple times."); 
                } else
                     currentCodeLine += 1; //increment currentCodeLine to next address of next instruction of assembly code
                     nextLine = nextCommand(); //read next command from assembly code for next loop
              }
              return;
       }

       private String nextCommand() throws IOException { //nextCommand for reading and processing next non-empty line from assembly code file
              String line; //used to store current line read
              while(true) { //infinite loop that continues unitl non-empty line found or end of file reached
                     line = bufferedReader.readLine(); //read next line from file, if no file line set to null
                     if (line == null) { //if file line null, end of file reached, close buffered read and return null, no more commands to process
                            close();
                            return null;
                     }
                     line = line.replaceAll("\\s",""); //removes all whitespace characters
                     line = line.replaceAll("//.*", ""); //remove comments from a line
                     if (line.length() == 0) { //checks if line is empty, if so continue to skip current iteration of loop
                            continue;
                     }
                     return line; //line of next asembly command returned
              }
       }

       private char commandType(String command) { //commandType for determing type of command based on input string
              if (command.charAt(0) == '@') { //if first command string starts with @, A_COMMAND found (constant or memory location for addressing)
                     return A_COMMAND;
              }
              if (command.charAt(0) == '(') { //if first command string starts with (, L_COMMAND found (used to define lables)
                     return L_COMMAND;
              }
              return C_COMMAND; //default assumption given C_COMMAND
       }

       private char destination(String command) { //method for extracting and encoding destinatiosn for C commands
              if (command.indexOf('=') == -1) { //if input command has =, no destination specified, return 0command
                     return 0;
              }
              String leftHandString = command.replaceAll("=.*", ""); //extracts left hand side string of command, potential destination registers A, D, and M
              char res = 0; //char for storign results, initialized to 0
              if (leftHandString.indexOf('A') != -1) { //checks for A register, if so sets 4th bit of res to 1 usin bitwise OR operation 4
                     res |= 4;
              }
              if (leftHandString.indexOf('D') != -1) { //checks for D register, if so sets 4th bit of res to 1 usin bitwise OR operation 2
                     res |= 2;
              }
              if (leftHandString.indexOf('M') != -1) { //checks for M register, if so sets 4th bit of res to 1 usin bitwise OR operation 1
                     res |= 1;
              }
              return res; //return result
       }

       private char computation(String command) throws Exception { //computation method for decoding and encoding computation of C command, specifies operations performed by the ALU
              String computationVal = command.replaceAll(".*=", ""); //extracts computation part of command(operation to be performed), removes everyrthing left of =
              computationVal = computationVal.replaceAll(";.*", ""); //removes jump part of computation
              //match extracted computationVal with predefined computation patterns, returns binary value of the computation
              switch (computationVal) {
                     case "0": return 0b0101010;
                     case "1": return 0b0111111;
                     case "-1": return 0b0111010;
                     case "D": return 0b0001100;
                     case "A": return 0b0110000;
                     case "!D": return 0b0001101;
                     case "!A": return 0b0110001;
                     case "-D": return 0b0001111;
                     case "-A": return 0b0110011;
                     case "D+1": return 0b0011111;
                     case "A+1": return 0b0110111;
                     case "D-1": return 0b0001110;
                     case "A-1": return 0b0110010;
                     case "D+A": return 0b0000010;
                     case "D-A": return 0b0010011;
                     case "A-D": return 0b0000111;
                     case "D&A": return 0b0000000;
                     case "D|A": return 0b0010101;
                     case "M": return 0b1110000;
                     case "!M": return 0b1110001;
                     case "-M": return 0b1110011;
                     case "M+1": return 0b1110111;
                     case "M-1": return 0b1110010;
                     case "D+M": return 0b1000010;
                     case "D-M": return 0b1010011;
                     case "M-D": return 0b1000111;
                     case "D&M": return 0b1000000;
                     case "D|M": return 0b1010101;
                     default: throw new Exception("ERROR. Invalid computation."); //default case, if value not recognized throw exception 
              }
       }

       private char jump(String command) { //method for decoding and encoding jump part of C-commands
              if (command.indexOf(';') == -1) { //check if input command contains ;, if not found, no jump specifed and return 0
                     return 0;
              }
              String rightHandString = command.replaceAll(".*;", ""); //right hand side stirng, everything left of character ;, represents jump condition or mnemonic
              //switch case for matching rightHandString with recongized jump conditions or mnemonics
              switch (rightHandString) {
                     case "JGT": return 0b001;
                     case "JEQ": return 0b010;
                     case "JGE": return 0b011;
                     case "JLT": return 0b100;
                     case "JNE": return 0b101;
                     case "JLE": return 0b110;
                     case "JMP": return 0b111;
                     default: return 0; //return 0 as default, no jump condition used
              }
       }

       private void close() { //method for closing input file with bufferedReader
              //try catch block for ahndling exceptions when closing files such as IOExceptions
              try { 
                     if (bufferedReader != null) //if bufferedReader is not null, close the file
                            bufferedReader.close();
              } 
              catch (IOException ex) { //catch ioexceptions and print stack trace
                     ex.printStackTrace();
              }
       }

       public String parseNextCommand() throws Exception { //method for parsing and translating next assembly command
              String command = nextCommand(); //retrieve next command as command
              while(command != null && commandType(command) == L_COMMAND) //cehcks if command is L command, while loop continues until non-L command or end of file rached
                     command = nextCommand(); //continues to go to next command
              if (command == null) { //if null return null
                     return null;
              }
              if (commandType(command) == A_COMMAND) { //check if current command is an A command, translates command to machine code
                     command = command.substring(1); //remove @ symbol form beginning of A command leaving only the address or symbol
                     if (command.charAt(0) < '0' || command.charAt(0) > '9') { //check if first character of command is not a digit by checking command is between 0 and 9
                            Integer addr = (Integer) symbols.get(command); //addr to attempt to look up symbol command in the symbols table
                            if (addr == null) { //if symbol is not found in symbol table, code assigns new memory address using currentSymbolAddress incremented by 1
                                   addr = currentSymbolAddress;
                                   symbols.put(command, addr);
                                   currentSymbolAddress += 1;
                            }
                            //converts address to 16 char binary string and retuns it
                            return String.format("%16s", Integer.toBinaryString(addr)).replace(' ', '0'); 
                     } 
                     //else, processes A command that specifies numeric values, converts values to binary formatted to 16 bits and returns it
                     else {
                            return String.format("%16s", Integer.toBinaryString(Integer.parseInt(command))).replace(' ', '0');
                     }
              }
              //checks for C command, translates command to 16 bit machine code
              int raw = 0b1110000000000000 + (computation(command) << 6) + (destination(command) << 3) + jump(command);
              return String.format("%16s", Integer.toBinaryString(raw)).replace(' ', '0');
       }

       private static String getOutputFileName(String inputFileName) { //emthod for getting assembly file name for file print
              int dotIndex = inputFileName.lastIndexOf('.');
              if (dotIndex != -1) {
                  // Remove the original file extension
                  inputFileName = inputFileName.substring(0, dotIndex);
              }
              return inputFileName + "JAVA.hack"; // Add .hack extension and JAVA label to new file
          }

       public static void main(String args[]) { //main method, code run through command line
              if (args.length == 0) { //checks if no command line argument given, if so end program and print error message
                     System.err.println("ERROR. No input file given");
                     return;
              }
              String inputFileName = args[0]; //use command line input as inputFileName
              String outputFileName = getOutputFileName(inputFileName); // Determine the output file name
              Assembler assembler = new Assembler(args[0]); //assembler object, takes in assembly file
              String machineCode; //string machineCode, stores parsed machien code for each assembly command
              try { //try/catch statment for handling exceptions
                     BufferedWriter writeFile = new BufferedWriter(new FileWriter(outputFileName));
                     while(true) { //infinite loop for parsing and prcessing each assembly command in a file
                            machineCode = assembler.parseNextCommand(); //next assembly command form input file, translates it to machine code
                            if (machineCode == null) { //check if machineCode is null, indicates end of input file, if rached, exit loop and terminate program
                                   writeFile.close();
                                   return;
                            }
                            writeFile.write(machineCode); //write machineCode to file
                            writeFile.newLine();
                            System.out.println(machineCode); //print machine code machineCode to the command line
                     }
              } 
              catch (Exception exception) { //catch exception and print error message
                     exception.printStackTrace();
              }
       }
}
