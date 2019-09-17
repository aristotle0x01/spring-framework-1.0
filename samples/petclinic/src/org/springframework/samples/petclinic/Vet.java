package org.springframework.samples.petclinic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.support.MutableSortDefinition;
import org.springframework.beans.support.PropertyComparator;

/**
 * Simple JavaBean domain object representing a veterinarian.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 */
public class Vet extends Person {

	/** Holds value of property specialties. */
	private Set specialties;

	/** Setter for property specialties.
	 * @param specialties New value of property specialties.
	 */
	protected void setSpecialtiesInternal(Set specialties) {
		this.specialties = specialties;
	}

	/** Getter for property specialties.
	 * @return Value of property specialties.
	 */
	protected Set getSpecialtiesInternal() {
		if (this.specialties == null) {
			this.specialties = new HashSet();
		}
		return this.specialties;
	}

	public List getSpecialties() {
		List sortedSpecs = new ArrayList(getSpecialtiesInternal());
		PropertyComparator.sort(sortedSpecs, new MutableSortDefinition("name", true, true));
		return Collections.unmodifiableList(sortedSpecs);
	}

	public int getNrOfSpecialties() {
		return getSpecialtiesInternal().size();
	}

	public void addSpecialty(Specialty specialty) {
		getSpecialtiesInternal().add(specialty);
	}

}
