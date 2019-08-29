import java.io.*;
import java.util.Scanner;


/**
 * @author Sachindroid
 */
public class AES {
    //Execution command : java AES -e | -d keyfile inputfile

    public static File keyFile;
    public static File inputFile;
    public static int keysize=128;
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
            System.out.println("Encryption Mode");
            File encFile = new File(inputFileName+".enc");
            FileWriter fw = new FileWriter(encFile);
            bufferedWriter = new BufferedWriter(fw);
        }else if(args[0].equals("-d")){
            System.out.println("Decryption Mode");
            File decFile = new File(inputFileName+".dec");
            FileWriter fw = new FileWriter(decFile);
            bufferedWriter=new BufferedWriter(fw);
        }else{
            System.out.println("Invalid input option! try "+EXECUTION_COMMAND);
        }


        //Read the keyfile
        Scanner key =new Scanner(new FileReader(keyFile));
        String line;
        byte[][]keyArray=new byte[4][4];
        if(key.hasNextLine()){
            line=key.nextLine();
            System.out.println("Key = "+line+" key-length="+line.length());
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
                System.out.println("Invalid Input in Input File");
                System.exit(0);
            }

            if(args[0].equals("-e")){
                Encode encode=new Encode(stateArray,keySchedule);

                if(keysize==128){
                    //use cipher key for the initial round
                    encode.addRoundKey(0);
                    for (int round = 1; round <10 ; round++) {
                        encode.subBytes();
                        encode.shiftRows();
                        encode.mixColumns();
                        encode.addRoundKey(round);
                    }
                    //No Mix columns in last round
                    encode.subBytes();
                    encode.shiftRows();
                    encode.addRoundKey(10);
                }
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
                //Write Code for decryption

            }
            numBytes+=128;

        }
        bufferedWriter.flush();
        bufferedWriter.close();




        //stores the endtime of the program execution
        long endtime = System.currentTimeMillis();
        long elapsed = endtime-starttime;
        double time_taken = elapsed/1000.0;
        System.out.println("Execution Time = "+time_taken+" s");
    }
}
