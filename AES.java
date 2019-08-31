import java.io.*;
import java.util.Scanner;


/**
 * @author Sachindroid
 */
public class AES {
    //Execution command : java AES -e | -d keyfile inputfile

    //Key File -> contains key of 16bytes
    public static File keyFile;
    //Input File -> contains input of 16 bytes each line
    public static File inputFile;
    //Fixed keysize initially as 128 bits
    public static int keysize=128;
    //Buffered Writer for writing into files
    public static BufferedWriter bufferedWriter;

    public static final String EXECUTION_COMMAND = "java AES -e | -d [keyfile] [inputfile]";
    public static void main(String[] args) throws IOException {
        //args[0] contains the option (encryption / decryption)
        //args[1] contains the keyfile location
        //args[2] contains the inputfile location

        //stores the starting time of the program execution (epoch time)
        long starttime = System.currentTimeMillis();

        //take inputs of keyfile location and inputfile location
        keyFile=new File(args[1]);
        inputFile=new File(args[2]);

        //remove input file extension
        String inputFileName = inputFile.getName().substring(0,inputFile.getName().lastIndexOf("."));
        System.out.println(inputFileName);

        //check the option passed as parameter
        if(args[0].equals("-e")){
            //Encryption Mode
            System.out.println("Encryption Mode");
            File encFile = new File(inputFileName+".enc");
            FileWriter fw = new FileWriter(encFile);
            bufferedWriter = new BufferedWriter(fw);
        }else if(args[0].equals("-d")){
            //Decryption Mode
            System.out.println("Decryption Mode");
            File decFile = new File(inputFileName+".dec");
            FileWriter fw = new FileWriter(decFile);
            bufferedWriter=new BufferedWriter(fw);
        }else{
            //Invalid option
            System.out.println("Invalid input option! try "+EXECUTION_COMMAND);
        }


        //Read the keyfile
        Scanner key =new Scanner(new FileReader(keyFile));
        String line;
        //stores 16 byte key as 4x4 array
        byte[][]keyArray=new byte[4][4];
        if(key.hasNextLine()){
            line=key.nextLine();
            int counter=0;
            for (int row = 0; row <4 ; row++) {
                for (int column = 0; column <4 ; column++) {
                    char char1 = line.charAt(counter);
                    char char2 = line.charAt(counter+1);
                    String strByte = char1+""+char2;
                    keyArray[row][column]=(byte)(Integer.parseInt(strByte,16));
                    counter+=2;
                }
            }
        }

        //Initialise KeySchedule for KeyExpansion Algorithm
        KeySchedule keySchedule= new KeySchedule(keyArray);

        //Read lines from inputfile for encrypting/decrypting
        Scanner inputText = new Scanner(new FileReader(inputFile));
        //stores 16 byte input in 4x4 array for each round of encryption
        byte[][] stateArray = new byte[4][4];
        long numBytes=0; //Tracking the number of bytes encrypted
        while(inputText.hasNextLine()){
            line=inputText.nextLine();
            if(line.length()==32){
                int counter = 0;
                for (int row = 0; row <4 ; row++) {
                    for (int column = 0; column <4 ; column++) {
                        char char1=line.charAt(counter);
                        char char2 = line.charAt(counter+1);
                        String strByte=char1+""+char2;
                        stateArray[row][column]=(byte)Integer.parseInt(strByte,16);
                        counter+=2;
                    }
                }
            }else{
                //Some Line in input file doesnot have 16 byte
                System.out.println("Invalid Input in Input File");
                System.exit(0);
            }

            if(args[0].equals("-e")){
                //Running on encryption mode hence instantiating the Encode class
                Encode encode=new Encode(stateArray,keySchedule);

                if(keysize==128){
                    //use cipher key for the initial round -> round 0
                    encode.addRoundKey(0);
                    //9 Rounds
                    for (int round = 1; round <10 ; round++) {
                        encode.subBytes();
                        encode.shiftRows();
                        encode.mixColumns();
                        encode.addRoundKey(round);
                    }
                    //No Mix columns in last round -> round 10
                    encode.subBytes();
                    encode.shiftRows();
                    encode.addRoundKey(10);
                }
                //write each line of encrypted string to .enc file
                for (int row = 0; row <4 ; row++) {
                    for (int column = 0; column <4 ; column++) {
                        String hexStr = String.format("%x",stateArray[row][column]);
                        if(hexStr.length()==1){
                            hexStr="0"+hexStr;
                        }
                        bufferedWriter.write(hexStr.toUpperCase());
                    }

                }
                bufferedWriter.write("\n");
            }else{
                //Running on Decrypt mode hence instantiating the Decode class
                Decode decode = new Decode(stateArray,keySchedule);
                if(keysize==128){
                    //Going backwards -> first round 10
                    decode.invAddRoundKey(10);
                    //9 Rounds back
                    for (int round = 9; round >0 ; round--) {
                        decode.invShiftRows();
                        decode.invSubBytes();
                         decode.invAddRoundKey(round);
                         decode.invMixColumns();

                    }
                    //Last round with no mix columns
                    decode.invShiftRows();
                    decode.invSubBytes();
                    decode.invAddRoundKey(0);
                }
                //writing the decrypted string to .dec file
                for (int row = 0; row <4 ; row++) {
                    for (int column = 0; column < 4; column++) {
                        String hexStr = String.format("%x",stateArray[row][column]).toString();
                        //pad front of hex with 0
                        if (hexStr.length() == 1){
                            hexStr = "0" + hexStr;
                        }
                        bufferedWriter.write(hexStr.toUpperCase());
                    }
                }
                bufferedWriter.write("\n");
            }
            numBytes+=128;

        }
        //Closing the file writer instance
        bufferedWriter.flush();
        bufferedWriter.close();




        //stores the endtime of the program execution
        long endtime = System.currentTimeMillis();
        long elapsed = endtime-starttime;
        double time_taken = elapsed/1000.0;
        System.out.println("Execution Time = "+time_taken+" s");
        System.out.println("Number of bytes processed : "+numBytes);
    }
}
