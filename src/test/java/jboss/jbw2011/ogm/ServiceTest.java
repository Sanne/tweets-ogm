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

import java.util.List;
import java.util.Set;

import javax.persistence.EntityManagerFactory;

import junit.framework.Assert;

import org.hibernate.ogm.test.jpa.util.JpaTestCase;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Sanne Grinovero <sanne@hibernate.org> (C) 2011 Red Hat Inc.
 */
public class ServiceTest extends JpaTestCase {
	
	private EntityManagerFactory entityManagerFactory;

	@Test
	public void testHibernateSearchJPAAPIUsage() throws Exception {
		getTransactionManager().begin();
		FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager( entityManagerFactory.createEntityManager() );
		ServiceImpl service = new ServiceImpl();
		service.fullTextEntityManager = fullTextEntityManager;
		
		FullTextQuery droolsQuery = service.messagesMentioning( "Drools" );
		Assert.assertEquals( 2, droolsQuery.getResultSize() );
		List list = droolsQuery.getResultList();
		
		// now with weird characters, still works fine:
		droolsQuery = service.messagesMentioning( "dRoÖls" );
		Assert.assertEquals( 2, droolsQuery.getResultSize() );
		
		FullTextQuery infinispanQuery = service.messagesMentioning( "infinispan" );
		Assert.assertEquals( 1, infinispanQuery.getResultSize() );
		Tweet infinispanRelatedTweet = (Tweet) infinispanQuery.getResultList().get( 0 );
		Assert.assertEquals( "we are looking forward to Ìnfinispan", infinispanRelatedTweet.getMessage() );
		
		FullTextQuery messagesBySmartMarketingGuy = service.messagesBy( "SmartMarketingGuy" );
		Assert.assertEquals( 3, messagesBySmartMarketingGuy.getResultSize() );
		
		FullTextQuery timeSortedTweets = service.allTweetsSortedByTime();
		List resultList = timeSortedTweets.getResultList();
		Assert.assertEquals( 6, resultList.size() );
		Assert.assertEquals( 2l, ((Tweet) resultList.get( 0 ) ).getTimestamp() );
		Assert.assertEquals( 30l, ((Tweet) resultList.get( 1 ) ).getTimestamp() );
		Assert.assertEquals( 50l, ((Tweet) resultList.get( 2 ) ).getTimestamp() );
		Assert.assertEquals( 61000l, ((Tweet) resultList.get( 3 ) ).getTimestamp() );
		Assert.assertEquals( 600000l, ((Tweet) resultList.get( 4 ) ).getTimestamp() );
		Assert.assertEquals( 600001l, ((Tweet) resultList.get( 5 ) ).getTimestamp() );
		
		Set<ScoredTerm> mostFrequentlyUsedTerms = service.mostFrequentlyUsedTerms( "message", 1 );
		int i = 0;
		for ( ScoredTerm scoredTerm : mostFrequentlyUsedTerms ) {
			if ( scoredTerm.term.equals( "hibernate" ) ) {
				Assert.assertEquals( scoredTerm, new ScoredTerm( "hibernate", 3 ) );
				i++;
			}
			if ( scoredTerm.term.equals( "drools" ) ) {
				Assert.assertEquals( scoredTerm, new ScoredTerm( "drools", 2 ) );
				i++;
			}
			if ( scoredTerm.term.equals( "are" ) ) {
				Assert.fail( "should not find 'are' as it's in the stopwords list" );
			}
		}
		Assert.assertEquals( 2, i );
		getTransactionManager().commit();
	}
	
	@Before
	public void prepareTestData() throws Exception {
		entityManagerFactory = getFactory();

		getTransactionManager().begin();
		FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager( entityManagerFactory.createEntityManager() );
		
		// make sure the indexes are empty at start as we're using a FS based index:
		fullTextEntityManager.purgeAll( Tweet.class );
		fullTextEntityManager.flushToIndexes();
		
		// store some tweets
		fullTextEntityManager.persist( new Tweet( "What's Drools? never heard of it", "SmartMarketingGuy", 50l ) );
		fullTextEntityManager.persist( new Tweet( "We love Hibernate", "SmartMarketingGuy", 30l ) );
		fullTextEntityManager.persist( new Tweet( "I wouldn't vote for Drools", "SmartMarketingGuy", 2l ) );
		//note the accent on "I", still needs to match search for "infinispan"
		fullTextEntityManager.persist( new Tweet( "we are looking forward to Ìnfinispan", "AnotherMarketingGuy", 600000l ) );
		fullTextEntityManager.persist( new Tweet( "Hibernate OGM", "AnotherMarketingGuy", 600001l ) );
		fullTextEntityManager.persist( new Tweet( "What is Hibernate OGM?", "ME!", 61000l ) );
		
		getTransactionManager().commit();
	}
	
	@After
	public void cleanData() throws Exception {
		entityManagerFactory.close();
	}

	@Override
	public Class<?>[] getEntities() {
		return new Class<?>[]{ Tweet.class };
	}

}
