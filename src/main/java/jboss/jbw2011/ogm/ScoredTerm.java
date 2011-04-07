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

/**
 * A simple indexed term value representation with it's frequency count.
 * It's comparable so that you can iterate a sorted collection from the most
 * frequent term.
 *
 * @author Sanne Grinovero <sanne@hibernate.org> (C) 2011 Red Hat Inc.
 */
public class ScoredTerm implements Comparable<ScoredTerm> {
	
	public final String term;
	public final int frequency;

	public ScoredTerm(String term, int frequency) {
		if ( term == null ) throw new IllegalArgumentException( "term argument is not optional" );
		this.term = term;
		this.frequency = frequency;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + frequency;
		result = prime * result + ( ( term == null ) ? 0 : term.hashCode() );
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj )
			return true;
		if ( obj == null )
			return false;
		if ( getClass() != obj.getClass() )
			return false;
		ScoredTerm other = (ScoredTerm) obj;
		if ( frequency != other.frequency )
			return false;
		if ( term == null ) {
			if ( other.term != null )
				return false;
		}
		else if ( !term.equals( other.term ) )
			return false;
		return true;
	}

	@Override
	public int compareTo(ScoredTerm o) {
		int diff = o.frequency - this.frequency;
		if ( diff == 0 ) {
			return this.term.compareTo( o.term );
		}
		else {
			return diff;
		}
	}

	@Override
	public String toString() {
		return "ScoredTerm [term=" + term + ", frequency=" + frequency + "]";
	}

}
