/* 
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */

package jboss.jbw2011.ogm;

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.search.SearchFactory;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.query.DatabaseRetrievalMethod;
import org.hibernate.search.query.ObjectLookupMethod;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.hibernate.search.store.DirectoryProvider;

/**
 * Example service, how the queries I'm aware of could be implemented.
 * All methods tested by jboss.jbw2011.ogm.ServiceTests
 * 
 * @author Sanne Grinovero <sanne@hibernate.org> (C) 2011 Red Hat Inc.
 */
public class ServiceImpl {
	
	/**
	 * this should be injected before use. Also we assume methods are invoked in the scope of a transaction.
	 */
	public FullTextEntityManager fullTextEntityManager;
	private QueryBuilder tweetQueryBuilder;

	/**
	 * creates a FullTextQuery for all tweets mentioning a specific word/technology in the message field.
	 */
	public FullTextQuery messagesMentioning(String keyword) {
		Query query = getQueryBuilder().keyword().onField( "message" ).matching( keyword ).createQuery();
		FullTextQuery fullTextQuery = fullTextEntityManager.createFullTextQuery( query );
		fullTextQuery.initializeObjectsWith( ObjectLookupMethod.PERSISTENCE_CONTEXT, DatabaseRetrievalMethod.FIND_BY_ID );
		return fullTextQuery;
	}
	
	/**
	 * Searches for all tweets from a specific account. This is case-sensitive.
	 */
	public FullTextQuery messagesBy(String name) {
		Query query = getQueryBuilder().keyword().onField( "sender" ).matching( name ).createQuery();
		FullTextQuery fullTextQuery = fullTextEntityManager.createFullTextQuery( query );
		fullTextQuery.initializeObjectsWith( ObjectLookupMethod.PERSISTENCE_CONTEXT, DatabaseRetrievalMethod.FIND_BY_ID );
		return fullTextQuery;
	}
	
	/**
	 * To search for all tweets, sorted in creation order (assuming the timestamp is correct).
	 * @return
	 */
	public FullTextQuery allTweetsSortedByTime() {
		Query query = getQueryBuilder().all().createQuery();
		FullTextQuery fullTextQuery = fullTextEntityManager.createFullTextQuery( query );
		fullTextQuery.initializeObjectsWith( ObjectLookupMethod.PERSISTENCE_CONTEXT, DatabaseRetrievalMethod.FIND_BY_ID );
		fullTextQuery.setSort( new Sort( new SortField( "timestamp", SortField.LONG ) ) );
		return fullTextQuery;
	}

	/**
	 * This is the most complex case, and uses ScoredTerm to represent the return value.
	 * I guess this is a practical way to make a tag cloud out of all indexed terms.
	 * @param inField Will return only scoredTerms in the specified field
	 * @param minimumFrequency a minimum threshold, can be used to reduce not very significant words (see analyzers and stopwords for better results).
	 * @throws IOException
	 */
	Set<ScoredTerm> mostFrequentlyUsedTerms(String inField, int minimumFrequency) throws IOException {
		String internedFieldName = inField.intern();
		SearchFactory searchFactory = fullTextEntityManager.getSearchFactory();
		DirectoryProvider[] directoryProviders = searchFactory.getDirectoryProviders( Tweet.class );
		IndexReader indexReader = searchFactory.getReaderProvider().openReader( directoryProviders );
		TreeSet<ScoredTerm> sortedTerms = new TreeSet<ScoredTerm>();
		try {
			TermEnum termEnum = indexReader.terms();
			while ( termEnum.next() ) {
				Term term = termEnum.term();
				if ( internedFieldName != term.field() ) {
					continue;
				}
				int docFreq = termEnum.docFreq();
				if ( docFreq < minimumFrequency ) {
					continue;
				}
				String text = term.text();
				sortedTerms.add( new ScoredTerm( text, docFreq ) );
			}
		}
		finally {
			searchFactory.getReaderProvider().closeReader( indexReader );
		}
		return sortedTerms;
	}
	
	private QueryBuilder getQueryBuilder() {
		if ( tweetQueryBuilder == null ) {
			tweetQueryBuilder = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity( Tweet.class ).get();
		}
		return tweetQueryBuilder;
	}

}
