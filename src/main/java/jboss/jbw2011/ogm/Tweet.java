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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.apache.solr.analysis.ASCIIFoldingFilterFactory;
import org.apache.solr.analysis.LowerCaseFilterFactory;
import org.apache.solr.analysis.StandardTokenizerFactory;
import org.apache.solr.analysis.StopFilterFactory;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.AnalyzerDef;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.NumericField;
import org.hibernate.search.annotations.Parameter;
import org.hibernate.search.annotations.TokenFilterDef;
import org.hibernate.search.annotations.TokenizerDef;

/**
 * An entity to index Tweets on HibernateOGM with Hibernate Search.
 * 
 * It's using a custom analyzer using a set of stopwords,
 * and we filter out noisy terms or weird characters as well.
 * 
 * @author Sanne Grinovero <sanne@hibernate.org> (C) 2011 Red Hat Inc.
 */
@Indexed(index = "tweets")
@Analyzer(definition = "english")
@AnalyzerDef(name = "english",
	tokenizer = @TokenizerDef(factory = StandardTokenizerFactory.class),
	filters = {
		@TokenFilterDef(factory = ASCIIFoldingFilterFactory.class),
		@TokenFilterDef(factory = LowerCaseFilterFactory.class),
		@TokenFilterDef(factory = StopFilterFactory.class, params = {
				@Parameter(name = "words", value = "stoplist.properties"),
				@Parameter(name = "resource_charset", value = "UTF-8"),
				@Parameter(name = "ignoreCase", value = "true")
		})
})
@Entity
public class Tweet {
	
	private String id;
	private String message = "";
	private String sender = "";
	private long timestamp = 0L;
	
	public Tweet() {}
	
	public Tweet(String message, String sender, long timestamp) {
		this.message = message;
		this.sender = sender;
		this.timestamp = timestamp;
	}

	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	public String getId() { return id; }
	public void setId(String id) { this.id = id; }

	@Field
	public String getMessage() { return message; }
	public void setMessage(String message) { this.message = message; }

	@Field(index=Index.UN_TOKENIZED)
	public String getSender() { return sender; }
	public void setSender(String sender) { this.sender = sender; }

	@Field
	@NumericField
	public long getTimestamp() { return timestamp; }
	public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

}
