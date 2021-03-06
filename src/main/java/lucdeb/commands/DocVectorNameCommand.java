/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lucdeb.commands;

import java.io.IOException;
import java.io.PrintStream;
import lucdeb.LucDebObjects;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;

/**
 *
 * @author dwaipayan
 */
public class DocVectorNameCommand extends Commands {

    String fieldName;
    String docid;
    int luceneDocid;

    public DocVectorNameCommand(LucDebObjects lucivObjects) {
        super(lucivObjects, "dvn");
    }

    @Override
    public String help() {
        return usage();
    }

    @Override
    public void execute(String[] args, PrintStream out) throws IOException {

         if (args.length != 1 && args.length != 2) {
            out.println(help());
            return;
        }

        // Parsing the arguments
        docid = args[0];
        if (args.length == 2 )
            fieldName = args[1];
        else 
            fieldName = lucdebObjects.getSearchField();

        try {
            luceneDocid = lucdebObjects.getLuceneDocid(docid);
        } catch (Exception ex) {
            out.println("Error while getting luceneDocid");
        }
        if(luceneDocid < 0) {
            return;
        }

        IndexReader indexReader = lucdebObjects.getIndexReader();
        // Term vector for this document and field, or null if term vectors were not indexed
        Terms terms = indexReader.getTermVector(luceneDocid, fieldName);
        if(null == terms) {
            out.println("Error: Term vector null: "+luceneDocid);
            return;
        }

        System.out.println("unique-term-count " + terms.size());
        TermsEnum iterator = terms.iterator();
        BytesRef byteRef = null;

        //* for each word in the document
        int docSize = 0;
        while((byteRef = iterator.next()) != null) {
            String term = new String(byteRef.bytes, byteRef.offset, byteRef.length);
            long tf = iterator.totalTermFreq();    // tf of 't'
            out.println(term+" "+tf);
//            int docFreq = indexReader.docFreq(new Term(fieldName, term));      // df of 't'
//            long cf = indexReader.totalTermFreq(new Term(fieldName, term));    // tf of 't'
//            System.out.println(term+": cf: "+cf + " : df: " + docFreq);
            docSize += tf;
        }
        out.println("doc-size: " + docSize);

    }

    @Override
    public String usage() {
        return "dvn <docid> [<field-name>]";
    }
    
}
