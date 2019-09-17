package org.springframework.samples.petclinic;

import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.samples.petclinic.util.EntityUtils;

/**
 * Base class for Clinic tests.
 * Allows subclasses to specify context location.
 *
 * @author Ken Krebs
 */
public abstract class AbstractClinicTests extends TestCase {

	private Clinic clinic;

	protected void setUp() throws Exception {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(getContextLocation());
		clinic = (Clinic) ctx.getBean("clinic");
	}

	protected abstract String getContextLocation();

	public void testGetVets() {
		List vets = clinic.getVets();
		assertEquals(6, vets.size());
		Vet v1 = (Vet) EntityUtils.getById(vets, Vet.class, 2);
		assertEquals("Leary", v1.getLastName());
		assertEquals(1, v1.getNrOfSpecialties());
		assertEquals("radiology", ((Specialty) v1.getSpecialties().get(0)).getName());
		Vet v2 = (Vet) EntityUtils.getById(vets, Vet.class, 3);
		assertEquals("Douglas", v2.getLastName());
		assertEquals(2, v2.getNrOfSpecialties());
		assertEquals("dentistry", ((Specialty) v2.getSpecialties().get(0)).getName());
		assertEquals("surgery", ((Specialty) v2.getSpecialties().get(1)).getName());
	}

	public void testGetPetTypes() {
		List petTypes = clinic.getPetTypes();
		assertEquals(6, petTypes.size());
		PetType t1 = (PetType) EntityUtils.getById(petTypes, PetType.class, 1);
		assertEquals("cat", t1.getName());
		PetType t4 = (PetType) EntityUtils.getById(petTypes, PetType.class, 4);
		assertEquals("snake", t4.getName());
	}

	public void testFindOwners() {
		List owners = clinic.findOwners("Davis");
		assertEquals(2, owners.size());
		owners = clinic.findOwners("Daviss");
		assertEquals(0, owners.size());
	}

	public void testLoadOwner() {
		Owner o1 = clinic.loadOwner(1);
		assertTrue(o1.getLastName().startsWith("Franklin"));
		Owner o10 = clinic.loadOwner(10);
		assertEquals("Carlos", o10.getFirstName());
	}

	public void testInsertOwner() {
		List owners = clinic.findOwners("Schultz");
		int found = owners.size();
		Owner owner = new Owner();
		owner.setLastName("Schultz");
		clinic.storeOwner(owner);
		assertTrue(!owner.isNew());
		owners = clinic.findOwners("Schultz");
		assertEquals(found + 1, owners.size());
	}

	public void testUpdateOwner() throws Exception {
		Owner o1 = clinic.loadOwner(1);
		String old = o1.getLastName();
		o1.setLastName(old + "X");
		clinic.storeOwner(o1);
		o1 = clinic.loadOwner(1);
		assertEquals(old + "X", o1.getLastName());
	}

	public void testLoadPet() {
		List types = clinic.getPetTypes();
		Pet p7 = clinic.loadPet(7);
		assertTrue(p7.getName().startsWith("Samantha"));
		assertEquals(EntityUtils.getById(types, PetType.class, 1).getId(), p7.getType().getId());
		assertEquals("Jean", p7.getOwner().getFirstName());
		Pet p6 = clinic.loadPet(6);
		assertEquals("George", p6.getName());
		assertEquals(EntityUtils.getById(types, PetType.class, 4).getId(), p6.getType().getId());
		assertEquals("Peter", p6.getOwner().getFirstName());
	}

	public void testInsertPet() {
		Owner o6 = clinic.loadOwner(6);
		int found = o6.getPets().size();
		Pet pet = new Pet();
		pet.setName("bowser");
		o6.addPet(pet);
		List types = clinic.getPetTypes();
		pet.setType((PetType) EntityUtils.getById(types, PetType.class, 2));
		pet.setBirthDate(new Date());
		assertEquals(found + 1, o6.getPets().size());
		clinic.storePet(pet);
		assertTrue(!pet.isNew());
		o6 = clinic.loadOwner(6);
		assertEquals(found + 1, o6.getPets().size());
	}

	public void testUpdatePet() throws Exception {
		Pet p7 = clinic.loadPet(7);
		String old = p7.getName();
		p7.setName(old + "X");
		clinic.storePet(p7);
		p7 = clinic.loadPet(7);
		assertEquals(old + "X", p7.getName());
	}

	public void testInsertVisit() {
		Pet p7 = clinic.loadPet(7);
		int found = p7.getVisits().size();
		Visit visit = new Visit();
		p7.addVisit(visit);
		visit.setDescription("test");
		clinic.storeVisit(visit);
		assertTrue(!visit.isNew());
		assertEquals(found + 1, p7.getVisits().size());
	}

}
