package scanner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fa.Automaton;
import fa.AutomatonLoader;
import fa.State;
import tokens.Token;

public class Scanner {

	public static List<Token> scanFile(Map<String, String[]> tokenMap, String fileName) {
		return scanFile(tokenMap, new File(fileName));
	}
	
	public static List<Token> scanFile(Map<String, String[]> tokenMap, File f) {
		List<Token> out = new ArrayList<>();
		
		try {
			FileInputStream fis = new FileInputStream(f);
			
			String currentWord = "";
			int reading = fis.read();
			while(reading > 0) {
				char currentChar = (char) reading;
				currentWord += currentChar;
				
				if(currentChar == '\n') {
					out.addAll(scan(tokenMap, currentWord, false));
					currentWord = "";
				}
				
				reading = fis.read();
			}
			
			out.addAll(scan(tokenMap, currentWord, false));
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		out.add(new Token("EOF"));
		return out;
	}

	public static List<Token> scan(Map<String, String[]> tokenMap, String in) {
		return scan(tokenMap, in, true);
	}

	private static List<Token> scan(Map<String, String[]> tokenMap, String in, boolean withEOF) {
		List<Token> tokenSequence = new ArrayList<>();
		Automaton tokenChecker = AutomatonLoader.loadFromFile("res/lang.dfa");
		String currentWord = "";
		
		for(int i = 0; i < in.length(); i++) {
			char current = in.charAt(i);
			currentWord += current;
			tokenChecker.input(current);
						
			if((i < in.length() - 1 && tokenChecker.softInput(in.charAt(i+1)).size() == 0) || i == in.length()-1) {	
				if(tokenChecker.getCurrentStates().size() == 0) {
					System.err.println("[SCANNER] Error occurred: undefined character sequence");
					System.err.println("\t" + in);
					System.err.println("\t" + in.substring(0, i) + "^");
					
					System.exit(-1);
				} 
				
				else {
					State state = tokenChecker.getCurrentStates().get(0);
					Token tokenToAdd = Token.state2Token(tokenMap, state, currentWord);
					
					if(tokenToAdd.getName().equalsIgnoreCase("ERROR")) {
						System.err.println("[SCANNER@83] Error occurred");
						System.err.println("\t" + in);
						System.err.println("\t" + in.substring(0, i) + "^");
						
						System.exit(-1);
					}	
					
					else if(!tokenToAdd.getName().equalsIgnoreCase("IGNORE")) {
						tokenSequence.add(tokenToAdd);
					}
				}
				
				tokenChecker.reset();
				currentWord = "";
			}
		}

		if(withEOF) tokenSequence.add(new Token("EOF"));
		return tokenSequence;
	}
}
