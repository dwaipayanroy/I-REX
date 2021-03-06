/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lucdeb.commands;

import common.DocumentVector;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import lucdeb.LucDebObjects;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopScoreDocCollector;

/**
 *
 * @author Dwaipayan 
 */
public class ExplainCommand extends Commands {
    
//    String docid;
    String query;
    int luceneDocid;
    float totalScore;

    public ExplainCommand(LucDebObjects lucivObjects) {
        super(lucivObjects, "explain");
    }

    @Override
    public String help() {
        return "Returns an Explanation that describes how doc scored against query.";
    }

    @Override
    public String usage() {
        return CMD_NAME + " \n" +
              "1- <\"query-terms\" (in quotes)> \n"
            + "2- <\"list of docid\" (in quotes)> \n"
            + "3- <retrieval-functions with parameters "+lucdebObjects.retFuncMap.toString()+"> \n";
    }

    public void resetTotalScore() {totalScore = 0;}

    // "international organized crime" "72613 2241" lmjm/lmdir etc.
    @Override
    public void execute(String[] args, PrintStream out) throws IOException {

    	Options options = new Options();
    	Option queryOption = new Option("q", "queryTerms", true, "Query Terms");
    	queryOption.setRequired(false);
    	options.addOption(queryOption);
    	
    	Option luceneDocIDOption = new Option("i", "luceneDocId", true, "Lucene Doc Id");
    	luceneDocIDOption.setRequired(false);
    	options.addOption(luceneDocIDOption);
    	
    	Option luceneDocNameOption = new Option("n", "docName", true, "Document Name");
    	luceneDocNameOption.setRequired(false);
    	options.addOption(luceneDocNameOption);
    		
    	Option retModelOption = new Option("r", "retrievalParams", true, "Retrieval Models with params");
    	retModelOption.setRequired(false);
    	options.addOption(retModelOption);
    	
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
        String queryTermsValue = cmd.getOptionValue("queryTerms");
        String luceneDocIdNum = cmd.getOptionValue("luceneDocId");
        String docNameValue = cmd.getOptionValue("docName");
        String retrievalParams = cmd.getOptionValue("retrievalParams");
        //System.out.println("<"+luceneDocIdNum + ">, " + docNameValue);
        if(cmd.hasOption("i"))
        {
	        // Parsing the arguments
	        try {
	            luceneDocid = Integer.parseInt(luceneDocIdNum.trim());
	            
	        } catch(NumberFormatException ex) {
	            out.println("error reading docid; expected integers.");
	        }
        }
        else if(cmd.hasOption("n"))
        {
        	try {
        		out.println(docNameValue);
                luceneDocid = lucdebObjects.getLuceneDocid(docNameValue.trim());
            } catch (Exception ex) {
                out.println("Error while getting luceneDocid");
            }
            if(luceneDocid < 0) {
                return;
            }
        }
        else 
        	return;

        if(luceneDocid < 0 || luceneDocid > lucdebObjects.getNumDocs()) {
            out.println(luceneDocid + ": not in the docid range (0 - " + lucdebObjects.getNumDocs() + ")");
            return;
        }
        
        if(cmd.hasOption("r"))
        {
        	retrievalParams = retrievalParams.trim();
        	String param1="", param2= "", param3 = "";
            String[] params = retrievalParams.split(" ");
            switch(params[0]) {
    	        case "lmjm":
    	            param1 = params[1];
    	            break;
    	        case "lmdir":
    	            param1 = params[1];
    	            break;
    	        case "bm25":
    	            param1 = params[1];
    	            param2 = params[2];
    	            break;
    	        case "dfr":
    	            param1 = params[1];
    	            param2 = params[2];
    	            param3 = params[3];
    	            break;
    	        default:
    	            // TODO
    	            break;
            }
            lucdebObjects.setRetreivalParameter(params[0],param1, param2, param3);
        }
        else
        	lucdebObjects.getRetreivalParameter();
    	
        String param1="", param2 ="", param3 ="";
        String queryTerms[];
        String docNames[];
        String[] retrievalModelsWithParam;
        TopScoreDocCollector collector = TopScoreDocCollector.create(10);
        String searchField = lucdebObjects.getSearchField();

        // usage: >4 arguments:
        //  1. set of query terms (in quotes)
        //  2. set of documents (in quotes)
        //  3. retrieval models with params: lmjm/lmdir etc.
        /*
        if (args.length < 3) {
            out.println("Usage: " + usage());
            return;
        }*/

        // query terms and document-ids are in quotes
        queryTerms = queryTermsValue.trim().split(" ");
        
        
            
        IndexSearcher indexSearcher = lucdebObjects.getIndexSearcher();
        System.out.println("Retrieval model set to " + lucdebObjects.retModelName);

        SimilarityFunctions simFunc = new SimilarityFunctions(lucdebObjects.retModelName, lucdebObjects.retModelParam1,lucdebObjects.retModelParam2, lucdebObjects.retModelParam3);
        indexSearcher.setSimilarity(simFunc);

        StringBuilder buf = new StringBuilder();
        for (String queryTerm : queryTerms) {
            buf.append(queryTerm).append(" ");
        }
        query = buf.toString();

        Query luceneQuery;
        try {
            IndexReader indexReader = lucdebObjects.getIndexReader();

            // for each of the documents
            //for(String docid: docNames) {
                DocumentVector dv = new DocumentVector();

                //luceneDocid = lucdebObjects.getLuceneDocid(docid);
                System.out.println(/*luceneDocid + " " + */luceneDocid);
                if(luceneDocid == -1) {
                    return;
                }
                dv = dv.getDocumentVector(luceneDocid, indexReader);
                simFunc.setDocVector(dv);
                DocTermStat dts = new DocTermStat(lucdebObjects.getIndexSearcher().doc(luceneDocid).get(lucdebObjects.idField), luceneDocid);
                simFunc.setDocTermStat(dts);

                // reset totalScore variable to Zero
                resetTotalScore();

                // for each of the query terms q:
                    //  1. tf(q,d),
                    //  2. ntf(q,d),
                    //  3. df(q), idf(q),
                    //  4. retrieval-score(q, d)

                for(String queryTerm : queryTerms) {
                    TermStats ts = new TermStats();
                    simFunc.setTermStats(ts);
                    luceneQuery = lucdebObjects.getAnalyzedQuery(queryTerm.trim(), searchField);
                    ts.term = luceneQuery.toString(searchField);
                    //System.out.printPagination(queryTerm + "\t");

                    Explanation expln = indexSearcher.explain(luceneQuery, luceneDocid);
                    //out.println(expln.toString());
                    if(!expln.isMatch());
                        //System.out.println("TODO: (0\t0\t0\t0)");
                    //System.out.println(expln.getValue());
                    totalScore += expln.getValue();
                    dts.docScore += expln.getValue();
                }
                System.out.println(dts.doclen + "\t" + dts.avgdl);

                System.out.println("term: (cf\tidf\ttf\tcol-proba\tscore)");
                for(TermStats ts : dts.terms) {
                    System.out.printf("%s: (%d\t%.4f\t%.0f\t%.4f\t%.4f)\n", 
                        ts.term, ts.cf, ts.idf, ts.tf, ts.collectionProbability, ts.score);
                }

                    /*
                    luceneQuery = lucdebObjects.getAnalyzedQuery(query, searchField);
                    System.out.println("Query: " + luceneQuery.toString());
                    Explanation expln = indexSearcher.explain(luceneQuery, luceneDocid);
                    out.println(expln.toString());
                    //*/
                System.out.println("Total-score("+luceneDocid + "): " + totalScore);
            //}
        } 
        catch (Exception ex) {
        	ex.printStackTrace(out);
        }
        
    }
}
