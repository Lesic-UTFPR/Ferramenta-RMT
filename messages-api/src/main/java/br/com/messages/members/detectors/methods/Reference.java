package br.com.messages.members.detectors.methods;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Reference implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String title;

	private final int year;

	private final List<String> authors;

	public Reference() {
		this("", 0, Collections.emptyList());
	}

	public Reference(String title, int year, List<String> authors) {
		this.title = title;
		this.year = year;
		this.authors = authors;
	}

	public String getTitle() {
		return title;
	}

	public int getYear() {
		return year;
	}

	public Collection<String> getAuthors() {
		return authors;
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof Reference) {
			final Reference another = (Reference) object;
			
			if(this.getAuthors().size() != another.getAuthors().size()) {
				return false;
			}
			
			EqualsBuilder eqBuilder = new EqualsBuilder().append(title, another.title).append(year, another.year);
			
			for(int i=0 ; i<this.getAuthors().size() ; i++) {
				eqBuilder = eqBuilder.append(authors.get(i), another.authors.get(i));
			}
			
			return eqBuilder.isEquals();
		}
		return false;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(title).toHashCode();
	}

}
