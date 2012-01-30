/*
 * Copyright 2012 Mohan Singh.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.luc.searcher;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

public class LuceneFileSearcher {
	
	private Directory dir;
	private PhraseQuery pQuery;
	private BooleanQuery bQuery;
	private IndexSearcher indexSearcher;
	private QueryParser qp;
	private Query q;
	
	public void search(File indexDir, String query ) throws IOException, ParseException{
		
		dir=new SimpleFSDirectory(indexDir,null);
		
		pQuery=new PhraseQuery();
		
		String[] splittedQuery=query.split(" ");
		
		/*
		 * Build a Phrase Query 
		 */
		
		for(int i=0;i<splittedQuery.length;i++){
			
			pQuery.add(new Term("contents", splittedQuery[i]));
			
		}
		
		pQuery.setSlop(5);
		
		indexSearcher=new IndexSearcher(dir);
		
		/*
		 * Call query parser to generate search query
		 */
		
		qp=new QueryParser(Version.LUCENE_35,"contents",new StandardAnalyzer(Version.LUCENE_35));
		
		q=qp.parse(query);
		
		
		/*
		 * create a boolean query using phrase query and output of query parser to get a query for better outputs
		 */
		
		bQuery=new BooleanQuery();		
		bQuery.add(q, BooleanClause.Occur.SHOULD);
		bQuery.add(pQuery, BooleanClause.Occur.SHOULD);
		
		
		
		/*
		 * Retrive files Matching the query
		 */
				
		TopDocs topDocs=indexSearcher.search(bQuery, 10);
		
		for (int i = 0; i < topDocs.totalHits; i++) {
			
			ScoreDoc match = topDocs.scoreDocs[i];		
					
			Document doc = indexSearcher.doc(match.doc);
			
			System.out.println(doc.get("filename"));
			
		}
		
	}
	
	
	

}
