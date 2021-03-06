/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lucdeb.commands;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import lucdeb.LucDebObjects;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.lucene.search.IndexSearcher;

/**
 *
 * @author dwaipayan
 */
public class DumpCommand extends Commands {

    String queryTerms[];
    String fieldName;
    int luceneDocid;

    public DumpCommand(LucDebObjects lucivObjects) {
        super(lucivObjects, "dump");
    }

    @Override
    public String help() {
        return usage();
    }

    @Override
    public void execute(String[] args, PrintStream out) throws IOException {

        LinkedList<String> fieldNames = new LinkedList<>();
        List queryList = null;

        // +
    	/* TODO remove optional flag */
    	Options options = new Options();
    	Option modelOption = new Option("i", "luceneDocId", true, "Lucene Doc Id");
//        modelOption = Option.builder("i").longOpt("luceneDocId").desc("Lucene Doc Id").hasArg(true).build();
    	modelOption.setRequired(false);
    	options.addOption(modelOption);
    	
    	Option paramOption = new Option("n", "docName", true, "Document Name");
    	paramOption.setRequired(false);
    	options.addOption(paramOption);
    	options.addOption("f","fieldName", true, "Field name which will be dumped" );
    	options.addOption("q","query", true, "Query terms to highlight in the dump" );
    	
    	
    	CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);
            return;
        }
        String luceneDocIdNum = cmd.getOptionValue("luceneDocId");
        if(null != luceneDocIdNum)
            luceneDocid = Integer.parseInt(luceneDocIdNum);
        //if(cmd.hasOption("luceneDocId"))
        //    System.out.println(cmd.getOptionValue("luceneDocId"));
        // TODO: to include this option
        String docNameValue = cmd.getOptionValue("docName");
        
        if(cmd.hasOption("n"))
        {
        	try {
        		//out.println(docNameValue);
                luceneDocid = lucdebObjects.getLuceneDocid(docNameValue);
            } catch (Exception ex) {
                out.println("Error while getting luceneDocid");
            }
            if(luceneDocid < 0) {
                return;
            }
        }
    
        String fieldNameValue = cmd.getOptionValue("fieldName");
        
        if(null != fieldNameValue)
            fieldNames.add(fieldNameValue);        
        else {
            fieldNames = (LinkedList<String>) lucdebObjects.getFieldNames();
        }
        String query = cmd.getOptionValue("query");
        if(null != query) {
            queryTerms = query.split(" ");
            queryList = Arrays.asList(queryTerms);
        }
        //System.out.println(luceneDocid);
        // -

        /*
        if (args.length != 1 && args.length != 2) {
            out.println(usage());
            return;
        }

        // Parsing the arguments
        try {
            luceneDocid = Integer.parseInt(args[0]);
            if(!lucdebObjects.isValidLucenDocid(luceneDocid)) {
                return;
            }
        }
        catch(NumberFormatException ex) {
            out.println("Error: parsing lucene-docid (integer input expected)");
            return;
        }

        if (args.length == 2 ) {
            fieldName = args[1];
            fieldNames.add(fieldName);
        }
        else {
            fieldNames = (LinkedList<String>) lucdebObjects.getFieldNames();
//            fieldName = lucdebObjects.getSearchField();
        }
        //*/

        IndexSearcher indexSearcher = lucdebObjects.getIndexSearcher();
        // Term vector for this document and field, or null if term vectors were not indexed
        ArrayList<String> list;
        list = new ArrayList<>();
        
        //int terminalWidth= (int)jline.TerminalFactory.get().getWidth();
        
        // TODO: To rewrite here
        for (String field : fieldNames) {
            //System.out.println(field);
        	String content = indexSearcher.doc(luceneDocid).get(field);
            //System.out.println(content);
        	String tokens[] = content.split("\\s+");

            content = "";
            for(String token : tokens) {
                if(queryList != null && queryList.contains(token))
                    content = content + " <mark style='background: #9ff4df; padding: 0.30em 0.6em; margin: 0 0.10em; line-height: 1; border-radius: 0.35em; box-decoration-break: clone; -webkit-box-decoration-break: clone'>"+token+"</mark>";
                else
                    content = content + " " + token;
            }
            String temp = content;
            
//            temp = temp.replaceAll("(.{80})", "$1\n");
           	//temp = temp.replaceAll("(.{"+(terminalWidth-5)+"})", "$1\n");
            tokens = temp.split("\n");
            for(String token:tokens)
                list.add(token);
     
//            out.println();
        }
       
       	out.println(String.join(" ", list));
       
       	//lucdebObjects.printPagination(list);
    }

    
    
    
    @Override
    public String usage() {
        return "dv <lucene-docid> [ <field-name> ]";
    }
    
}
