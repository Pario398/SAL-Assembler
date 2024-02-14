import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class Assembler {
    static String newFile;
    static BufferedReader readFile;
    static HashMap<String, Integer> codeMap = new HashMap<>();
    static HashMap<String, Integer> dataMap = new HashMap<>();
    static int varAddr = 0;
    static int addrCode = 0;
    static FileOutputStream fileWriter;

    public static void main(String[] args) throws IOException {
        File newfile = new File(args[0]);
        try {
            readFile = new BufferedReader(new FileReader(newfile));
            newFile = Pattern.compile("\\.sal$").matcher(args[0]).replaceAll(".bin");
        } catch (Exception exception) {
            throw new Error("Issue loading files");
        }
        fileWriter = new FileOutputStream(new File(newFile));
        String[] divideLine = nextValLine();
        if (divideLine[0].equals(".data")) {
            handleData();
        }
        handleCode();
        readFile.close();
    }

    public static void handleData() throws IOException {
        String[] dataLine = nextValLine();
        while (!(dataLine == null)) {
            if (dataLine[0].equals(".code")) {
                return;
            }
            if (dataMap.containsKey(dataLine[0]))
                return;
            dataMap.put(dataLine[0], varAddr);
            varAddr++;
            dataLine = nextValLine();
        }
    }

    public static void handleCode() throws IOException {
        String[] codeLine = nextValLine();
        addrCode(codeLine[0]);
        while (!(codeLine == null)) {
            int bitValue;
            int reg;
            switch (codeLine[0]) {
                case "ADD":
                    bitValue = 0B0000 << 12;
                    reg = Character.getNumericValue(codeLine[1].charAt(1));
                    bitValue += reg << 9;
                    if (checkRegister(codeLine[2])) {
                        reg = Character.getNumericValue(codeLine[2].charAt(1));
                        bitValue += 0B00 << 6;
                        bitValue += reg;
                        bitWriter(bitValue);
                    } else {
                        sizeRVA(codeLine[2], bitValue);
                    }
                    break;
                case "SUB":
                    bitValue = 0B0001 << 12;
                    reg = Character.getNumericValue(codeLine[1].charAt(1));
                    bitValue += reg << 9;
                    if (checkRegister(codeLine[2])) {
                        reg = Character.getNumericValue(codeLine[2].charAt(1));
                        bitValue += 0B00 << 6;
                        bitValue += reg;
                        bitWriter(bitValue);

                    } else {
                        sizeRVA(codeLine[2], bitValue);
                    }
                    break;
                case "AND":
                    bitValue = 0B0010 << 12;
                    reg = Character.getNumericValue(codeLine[1].charAt(1));
                    bitValue += reg << 9;
                    if (checkRegister(codeLine[2])) {
                        reg = Character.getNumericValue(codeLine[2].charAt(1));
                        bitValue += 0B00 << 6;
                        bitValue += reg;
                        bitWriter(bitValue);

                    } else {
                        sizeRVA(codeLine[2], bitValue);
                    }
                    break;
                case "OR":
                    bitValue = 0B0011 << 12;
                    reg = Character.getNumericValue(codeLine[1].charAt(1));
                    bitValue += reg << 9;
                    if (checkRegister(codeLine[2])) {
                        reg = Character.getNumericValue(codeLine[2].charAt(1));
                        bitValue += 0B00 << 6;
                        bitValue += reg;
                        bitWriter(bitValue);

                    } else {
                        sizeRVA(codeLine[2], bitValue);
                    }
                    break;
                case "JMP":
                    // opcode
                    bitValue = 0B0100 << 12;
                    
                    bitValue += 0B0;
                    // appropriate size and rva
                    sizeRVA(codeLine[1], bitValue);
                    break;
                case "JGT":
                    bitValue = 0B0101 << 12;
                    reg = Character.getNumericValue(codeLine[1].charAt(1));
                    bitValue += reg << 9;
                    sizeRVA(codeLine[2], bitValue);
                    break;
                case "JLT":
                    bitValue = 0B0110 << 12;
                    reg = Character.getNumericValue(codeLine[1].charAt(1));
                    bitValue += reg << 9;
                    sizeRVA(codeLine[2], bitValue);
                    break;
                case "JEQ":
                    bitValue = 0B0111 << 12;
                    reg = Character.getNumericValue(codeLine[1].charAt(1));
                    bitValue += reg << 9;
                    sizeRVA(codeLine[2], bitValue);
                    break;
                case "INC":
                    bitValue = 0B1001 << 12;
                    reg = Character.getNumericValue(codeLine[1].charAt(1));
                    bitValue += reg << 9;
                    bitValue += 0B0 << 8;
                    bitValue += 0B11 << 6;
                    bitWriter(bitValue);
                    break;
                case "DEC":
                    bitValue = 0B1010 << 12;
                    reg = Character.getNumericValue(codeLine[1].charAt(1));
                    bitValue += reg << 9;
                    bitValue += 0B0 << 8;
                    bitValue += 0B11 << 6;
                    bitWriter(bitValue);
                    break;
                case "NOT":
                    bitValue = 0B1011 << 12;
                    reg = Character.getNumericValue(codeLine[1].charAt(1));
                    bitValue += reg << 9;
                    bitValue += 0B0 << 8;
                    bitValue += 0B11 << 6;
                    bitWriter(bitValue);
                    break;
                case "LOAD":
                    bitValue = 0B1100 << 12;
                    reg = Character.getNumericValue(codeLine[1].charAt(1));
                    bitValue += reg << 9;
                    if (checkRegister(codeLine[2])) {
                        reg = Character.getNumericValue(codeLine[2].charAt(1));
                        bitValue += 0B00 << 6;
                        bitValue += reg;
                        bitWriter(bitValue);

                    } else {
                        sizeRVA(codeLine[2], bitValue);
                    }
                    break;
                case "STORE":
                    bitValue = 0B1101 << 12;
                    reg = Character.getNumericValue(codeLine[1].charAt(1));
                    bitValue += reg << 9;
                    if (checkRegister(codeLine[2])) {
                        reg = Character.getNumericValue(codeLine[2].charAt(1));
                        bitValue += 0B00 << 6;
                        bitValue += reg;
                        bitWriter(bitValue);

                    } else {
                        sizeRVA(codeLine[2], bitValue);
                    }
                    break;
                default:
                    break;
            }
            codeLine = nextValLine();
        }

    }
    public static void sizeRVA(String codeLine, int bitValue) throws IOException {
        if (checkConst(codeLine) || codeLine.startsWith("#")) {
            addressConstantRVA(codeLine, bitValue);
        } else if (dataMap.get(codeLine) != null) { 
            int operand = dataMap.get(codeLine);
            bitValue += 0B0 << 8; 
            bitValue += 0B10 << 6; 
            bitValue += operand; 
            bitWriter(bitValue);
        } else if (codeMap.get(codeLine) != null) { 

            int operand = codeMap.get(codeLine);
            if (operand < 64) {
                bitValue += 0B0 << 8; 
                bitValue += 0B10 << 6; 
                bitValue += operand; 
                bitWriter(bitValue);
            } else {
                bitValue += 0B1 << 8; 
                bitValue += 0B10 << 6; 
                bitValue += 0; // 6bits
                bitWriter(bitValue);
                bitWriter(operand);
            }

        } else {
            return;
        }
    }
    public static void addressConstantRVA(String codeLine, int bitValue) throws IOException {
        if (codeLine.startsWith("#")) {
            if (checkConst(codeLine)) {
                if (retrieveConst(codeLine) < 64) {
                    bitValue += 0B0 << 8; 
                    bitValue += 0B01 << 6; 
                    bitValue += retrieveConst(codeLine); 
                    bitWriter(bitValue);
                } else {
                    bitValue += 0B1 << 8; 
                    bitValue += 0B01 << 6; 
                    
                    bitWriter(bitValue);
                    int p;
                    p = retrieveConst(codeLine); 
                    bitWriter(p);
                }
            }
        } else {
            if (retrieveConst(codeLine) < 64) {
                bitValue += 0B0 << 8; 
                bitValue += 0B10 << 6; 
                bitValue += retrieveConst(codeLine); 
                bitWriter(bitValue);
            } else {
                bitValue += 0B1 << 8; 
                bitValue += 0B10 << 6; 
                
                bitWriter(bitValue);
                int p;
                p = retrieveConst(codeLine); 
                bitWriter(p);
            }
        }
    }
    public static String[] nextValLine() throws IOException {
        String readLine;
        while (!((readLine = readFile.readLine()) == null)) {
            readLine = readLine.trim();
            int index = readLine.indexOf("//");
            if (!(index == -1))
                readLine = readLine.substring(0, index);
            if (readLine.isEmpty())
                continue;
            String[] whiteLine = readLine.split("\\s+|,\\s*", 4);
            return whiteLine;
        }
        return null;
    }
    public static boolean checkRegister(String register) {
        String convert = register.toLowerCase();
        if (convert.matches("(r)[0-7]"))
            return true;
        return false;
    }
    static public boolean bitWriter(int input) throws IOException {
        String writeNum = Integer.toBinaryString(input);
        writeNum = fill(writeNum) + "\n";
        fileWriter.write(writeNum.getBytes());
        addrCode++;
        return true;
    }
    public static String fill(String str) {
        int index = str.length();
        if (index == 16) {
            return str;
        }
        else if (index < 16) {
            StringBuilder fill = new StringBuilder();
            for (int i = 0; i < 16 - index; i++)
                fill.append("0");
            String full = fill.append(str).toString();
            return full;
        } else {
            return null;
        }
    }
    public static boolean checkConst(String compNum) {
        try {
            int i = Integer.parseInt(compNum.substring(1));
            if (i <= 32767 && i > 0)
                return true;
        } catch (NumberFormatException e) {
            try {
                int i = Integer.parseInt(compNum);
                if (i <= 32767 && i > 0)
                    return true;
            } catch (NumberFormatException e2) {
                return false;
            }
        }
        return false;
    }
    public static Integer retrieveConst(String compNum) {
        try {
            int i = Integer.parseInt(compNum.substring(1));
            if (i <= 32767 && i > 0)
                return i;
        } catch (NumberFormatException e) {
            try {
                int i = Integer.parseInt(compNum);
                if (i <= 32767 && i > 0)
                    return i;
            } catch (NumberFormatException e2) {
                return 0;
            }
        }
        return 0;
    }
    public static boolean addrCode(String addr) throws FileNotFoundException {
        int addrIndex = addr.length();
        addr = addr.substring(0, addrIndex - 1);
        if (codeMap.containsKey(addr)) {
            return false;
        }
        fileWriter = new FileOutputStream(new File(newFile));
        codeMap.put(addr, addrCode);
        addrCode++;
        return true;
    }
}