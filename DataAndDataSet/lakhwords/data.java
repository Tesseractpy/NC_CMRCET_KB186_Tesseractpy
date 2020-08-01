import java.io.*;
import java.util.*;
import java.lang.*;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.io.File;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;  
public class data  
{  
public static void main(String[] args) throws IOException  
{
FileWriter writer = new FileWriter("out0.txt");
List<String> stopwords = Files.readAllLines(Paths.get("outtext.txt"));
//ArrayList<String> result = new ArrayList<String>();
int x=0;
//parsing a CSV file into Scanner class constructor  
Scanner sc = new Scanner(new File("book.csv"));  
sc.useDelimiter(",");   //sets the delimiter pattern 
Set set = new HashSet(); 
while (sc.hasNext())  //returns a boolean value  
{  
String input_str = sc.next().toLowerCase();  //find and returns the next complete token from this scanner  
input_str = input_str.replaceAll(Arrays.toString(new String[]{"\\p{Punct}", "+","=","*","#","$","~"})," ");
ArrayList<String> allWords = Stream.of(input_str.split(" ")).collect(Collectors.toCollection(ArrayList<String>::new));
allWords.removeAll(stopwords);
String result = String.join("\n",allWords);
if((set.add(result)) && (!result.equals("")) && (!result.equals(" ")) && (!result.equals("\n")) && (!result.equals("\n\n")) && (!result.equals("\n\n\n"))) {
            writer.append(result);
         }
}
System.out.print("Successful");
sc.close();  //closes the scanner  
}  
}