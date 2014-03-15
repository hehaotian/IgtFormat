package cantobitext;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import name.ww.utils.IntPair;
import cantobitext.tools.CantoBitextReader;

public class IgtFormat {

	public static void main(String[] args) throws IOException{
   
	 // igtFormatFile();
     igtWordAlignment();

    }
	
   public static void igtWordAlignment() throws IOException {
      Map<Integer, String> id = new HashMap<Integer, String>();
      catchDocIds(id);
	   List<ChineseBitext> chineseBitexts = CantoBitextReader.read("chinese_cantonese.txt",false);
	   PrintStream aln = new PrintStream("igt_aln.pml");
	   head(aln);
	   for (ChineseBitext chineseBitext: chineseBitexts) {
		   int curId = chineseBitext.getId();
	      if (id.containsKey(curId)) {
	    	  aln.println("\t\t<LM xml:id=\"" + curId + "\">");
	    	  aln.println("\t\t\t" + "<tree_a.rf>a#" + curId + "</tree_a.rf>");
	    	  aln.println("\t\t\t" + "<tree_b.rf>b#" + curId + "</tree_b.rf>");
	    	  aln.println("\t\t\t<node_alignment>");  	
	          List<IntPair> wordAlignments = chineseBitext.getWordAlignments();
	          Map<Integer, List<Integer>> alignments = intPairConverter(wordAlignments);
	          printWordAlign(aln, alignments, curId);
              aln.println("\t\t\t</node_alignment>");
              aln.println("\t\t</LM>");
         }
      }
	   tail(aln);
	   aln.close();
   }
   
	public static void igtFormatFile() throws IOException {
	      Map<Integer, String> id = new HashMap<Integer, String>();
	      IdList(id);
	      List<ChineseBitext> chineseBitexts = CantoBitextReader.read("chinese_cantonese.txt",false);
	      PrintStream igt = new PrintStream("yue_top500.txt");
	      int line_num = 1;      
	      for (ChineseBitext chineseBitext: chineseBitexts) {
	    	  
	         if (id.containsKey(chineseBitext.getId())) {
	        	String cantoneseText = chineseBitext.getOriginalCantoneseText();
	        	String mandarinText = chineseBitext.getOriginalMandarinText();    	
	        	List<IntPair> wordAlignments = chineseBitext.getWordAlignments();
	        	Map<Integer, List<Integer>> alignments = intPairConverter(wordAlignments);
	        	String glossText = getGlossText(cantoneseText, mandarinText, alignments);       	
	            igt.println("doc_id=" + chineseBitext.getId() + ".txt " + line_num + " " + (line_num + 2) + " L G T");
	            igt.println("language: cantonese (yue)");
	            igt.println("line=" + line_num + " tag=L:\t" + cantoneseText);
	            igt.println("line=" + (line_num + 1) + " tag=G:\t" + glossText);
	            igt.println("line=" + (line_num + 2) + " tag=T:\t" + mandarinText);
	            igt.println();
	            line_num += 3;
	         }
	      }
	      igt.close();
	}
   
   private static void IdList(Map<Integer, String> id) throws IOException {
      
      BufferedReader rankedIds = new BufferedReader(new FileReader("ranked500ids.txt"));
      String line = "";

      while ((line = rankedIds.readLine()) != null) {
         id.put(Integer.parseInt(line), line);
      }
      
      rankedIds.close();
   }
   
   private static String getGlossText(String cantoneseText, String mandarinText, Map<Integer, List<Integer>> alignments) {
	   String glossText = "";
	   int cantoneseTokensLength = cantoneseText.split(" ").length + 1;
	   String[] mandarinTokens = mandarinText.split(" ");
	   Map<Integer, String> mandarinTokensMap = new HashMap<Integer, String>();
	   mandarinTokensMap = getTokensMap(mandarinTokens);
	   Map<Integer, String> alignedMandarinGloss = getAlignedMandarinGloss(mandarinTokensMap, alignments);
	   for (int i = 1; i < cantoneseTokensLength; i ++) {
		   if (alignedMandarinGloss.containsKey(i)) {
			   glossText += alignedMandarinGloss.get(i) + " ";
		   } else {
			   glossText += "VOID ";
		   }
	   }
	   return glossText;
   }
   
   private static Map<Integer, String> getTokensMap(String[] tokens) {
	   Map<Integer, String> tokensMap = new HashMap<Integer, String>();
	   for (int i = 1; i < tokens.length + 1; i ++) {
		   tokensMap.put(i, tokens[i - 1]);
	   }
	   return tokensMap;
   }
   
   private static Map<Integer, List<Integer>> intPairConverter(List<IntPair> wordAlignments) {
	   Map<Integer, List<Integer>> alignments = new HashMap<Integer, List<Integer>>();
	   for (int i = 0; i < wordAlignments.size(); i ++) {
		   IntPair curPair = wordAlignments.get(i);
		   int x = curPair.getX();
		   int y = curPair.getY();
		   if (!alignments.containsKey(y)) {
			  List<Integer> temp = new ArrayList<Integer>();
			  temp.add(x);
			  alignments.put(y, temp);
		   } else {
			  List<Integer> temp = alignments.get(y);
			  temp.add(x);
			  alignments.put(y, temp);
		   }
	   }
	   return alignments;
   }
   
   private static Map<Integer, String> getAlignedMandarinGloss(Map<Integer, String> mandarinTokens, Map<Integer, List<Integer>> alignments) {
	   Map<Integer, String> alignedMandarinGloss = new HashMap<Integer, String>();
	   for (int i : alignments.keySet()) {
		   List<Integer> mandarinIndices = alignments.get(i);
		   String mandarinGloss = "";
		   for (int j = 0; j < mandarinIndices.size(); j ++) {
			   mandarinGloss += mandarinTokens.get(mandarinIndices.get(j));
			   if (j < mandarinIndices.size() - 1) {
				   mandarinGloss += "-";
			   }
		   }
		   alignedMandarinGloss.put(i, mandarinGloss);
	   }
	   return alignedMandarinGloss;
   }

	private static void catchDocIds(Map<Integer, String> id) throws IOException {
		      
		BufferedReader yue_file = new BufferedReader(new FileReader("yue.txt"));
      String line = "";

		while ((line = yue_file.readLine()) != null) {
			String route = "doc_id=";
			if (line.contains(route)) {
		      line = line.replaceAll(route, "");
            line = line.replaceAll(".txt\\b.*", "");
            id.put(Integer.parseInt(line), line);
			}
		}
		      
		yue_file.close();
	}  
	
	private static void head(PrintStream p) {
		p.println("<tree_alignment>");
		p.println("\t<head>");
		p.println("\t\t<schema href=\"alignment_scheme.xml\">");
		p.println("\t\t<references>");
		p.println("\t\t\t<reffile href=\"igt_yue.pml\" id=\"a\" name=\"document_a\"/>");
		p.println("\t\t\t<reffile href=\"igt_man.pml\" id=\"b\" name=\"document_b\"/>");
		p.println("\t\t\t</references>");
		p.println("\t</head>");
		p.println("\t<body>");
	}
	
	private static void tail(PrintStream p) {
		p.println("\t</body>");
		p.print("</tree_alignment>");
	}
	
	private static void printWordAlign(PrintStream p, Map<Integer, List<Integer>> align, int curId) {
		for (int i : align.keySet()) {
			for (int j = 0; j < align.get(i).size(); j ++) {
				p.println("\t\t\t\t<LM>");
				p.println("\t\t\t\t\t<a.rf>a#" + curId + "-" + i + "</a.rf>");
				p.println("\t\t\t\t\t<b.rf>b#" + curId + "-" + align.get(i).get(j) + "</b.rf>");
				p.println("\t\t\t\t</LM>");
			}
		}
	}

}