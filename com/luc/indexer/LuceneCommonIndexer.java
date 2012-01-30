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

package com.luc.indexer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.ContentHandler;

import org.apache.james.mime4j.dom.Body;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

public class LuceneCommonIndexer {
	
	private IndexWriter indexWriter;
	
	private File source;
	
	private File destination;
	
	private static InputStream is;
	
	private static PDFParser pdfParser;
	
	private static Parser commonParser;
	
	private static BodyContentHandler textHandler;
	
	private static Metadata metadata;
	
	/* Initializing Source and Destination Paths for Reading Files and Saving Index */
	
	public LuceneCommonIndexer ( String sourcePath , String destinationPath) throws IOException{
		
		source = new File(sourcePath);
		destination= new File(destinationPath);
		Directory dir = new SimpleFSDirectory(destination,null);
		indexWriter=new IndexWriter(dir, new StandardAnalyzer(Version.LUCENE_35),true, MaxFieldLength.UNLIMITED);
	}
	
	

	public int index() throws IOException, SAXException, TikaException {
		/*
		 * Check whether Source directory Exist 
		 */
	    if (!source.exists() || !source.isDirectory()) {
	      throw new IOException(source + " does not exist or is not a directory");
	    }

	    indexDirectory(indexWriter, source);

	    int numIndexed = indexWriter.numDocs();
	    indexWriter.optimize();
	    indexWriter.close();
	    return numIndexed;
	}
	
	private static void indexDirectory(IndexWriter writer, File dir)
		    throws IOException, SAXException, TikaException {

		    File[] files = dir.listFiles();

		    for (int i = 0; i < files.length; i++) {
		      File file = files[i];
		      if (file.isDirectory()) {
		        indexDirectory(writer, file); // Recursive call to find more files if handle is a directory
		      } else{
		        indexFile(writer, file);
		      }
		    }
	}
	
	// Index files
	private static void indexFile(IndexWriter writer, File file) throws IOException, SAXException, TikaException {

		    if (file.isHidden() || !file.exists() || !file.canRead()) {
		      return;
		    }

		    System.out.println("Indexing " + file.getCanonicalPath());
		    
		    is = new FileInputStream(file);
		    textHandler =new BodyContentHandler();
		    metadata = new Metadata();
		    
		    commonParser=new AutoDetectParser();
		    commonParser.parse(is, textHandler, metadata, null);		  

		    Document doc = new Document();		    
		    
		    doc.add(new Field("contents", new StringReader(textHandler.toString()))); 
		    doc.add(new Field("filename", file.getCanonicalPath(),Field.Store.YES, Field.Index.NOT_ANALYZED));
		    writer.addDocument(doc);
		  }
	


}
