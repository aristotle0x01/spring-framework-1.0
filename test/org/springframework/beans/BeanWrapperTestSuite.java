
/*
 * Copyright 2002-2004 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.beans;

import java.beans.PropertyEditorSupport;
import java.beans.PropertyVetoException;
import java.util.Properties;

import junit.framework.TestCase;

/**
 * @author Rod Johnson
 * @version $Id: BeanWrapperTestSuite.java,v 1.17 2004/03/19 16:09:10 jhoeller Exp $
 */
public class BeanWrapperTestSuite extends TestCase {

	public void testSetWrappedInstanceOfSameClass()throws Exception {
		TestBean tb = new TestBean();
		BeanWrapper bw = new BeanWrapperImpl(tb);
		tb.setAge(11);

		TestBean tb2 = new TestBean();
		bw.setWrappedInstance(tb2);

		bw.setPropertyValue("age", new Integer(14));
		assertTrue("2nd changed", tb2.getAge()==14);
		assertTrue("1 didn't change", tb.getAge() == 11);
	}

	public void testToString() {
		TestBean tb = new TestBean();
		tb.setName("kerry");
		tb.setAge(37);
		String s = new BeanWrapperImpl(tb).toString();
		// Test that it lists properties we've set
		assertTrue(s.indexOf("37") != -1);
		assertTrue(s.indexOf("kerry") != -1);
	}

	public void testIsReadablePropertyNotReadable() {
		NoRead nr = new NoRead();
		BeanWrapper bw = new BeanWrapperImpl(nr);
		assertFalse(bw.isReadableProperty("age"));
	}

	/**
	 * Shouldn't throw an exception: should just return false
	 */
	public void testIsReadablePropertyNoSuchProperty() {
		NoRead nr = new NoRead();
		BeanWrapper bw = new BeanWrapperImpl(nr);
		assertFalse(bw.isReadableProperty("xxxxx"));
	}

	public void testIsReadablePropertyNull() {
		NoRead nr = new NoRead();
		BeanWrapper bw = new BeanWrapperImpl(nr);
		try {
			bw.isReadableProperty(null);
			fail("Can't inquire into readability of null property");
		}
		catch (BeansException ex) {
			// expected
		}
	}

	public void testIsWritablePropertyNull() {
		NoRead nr = new NoRead();
		BeanWrapper bw = new BeanWrapperImpl(nr);
		try {
			bw.isWritableProperty(null);
			fail("Can't inquire into writability of null property");
		}
		catch (BeansException ex) {
			// expected
		}
	}

	public void testSetWrappedInstanceOfDifferentClass()throws Exception {
		ThrowsException tex = new ThrowsException();
		BeanWrapper bw = new BeanWrapperImpl(tex);

		TestBean tb2 = new TestBean();
		bw.setWrappedInstance(tb2);

		bw.setPropertyValue("age", new Integer(14));
		assertTrue("2nd changed", tb2.getAge()==14);
	}

	public void testGetterThrowsException() {
		GetterBean gb = new GetterBean();
		BeanWrapper bw = new BeanWrapperImpl(gb);
		bw.setPropertyValue("name","tom");
		assertTrue("Set name to tom", gb.getName().equals("tom"));
	}

	public void testEmptyPropertyValuesSet() {
		TestBean t = new TestBean();
		int age = 50;
		String name = "Tony";
		t.setAge(age);
		t.setName(name);
		try {
			BeanWrapper bw = new BeanWrapperImpl(t);
			assertTrue("age is OK", t.getAge() == age);
			assertTrue("name is OK", name.equals(t.getName()));
			bw.setPropertyValues(new MutablePropertyValues());
			// Check its unchanged
			assertTrue("age is OK", t.getAge() == age);
			assertTrue("name is OK", name.equals(t.getName()));
		}
		catch (BeansException ex) {
			fail("Shouldn't throw exception when everything is valid");
		}
	}

	public void testAllValid() {
		TestBean t = new TestBean();
		String newName = "tony";
		int newAge = 65;
		String newTouchy = "valid";
		try {
			BeanWrapper bw = new BeanWrapperImpl(t);
			//System.out.println(bw);
			MutablePropertyValues pvs = new MutablePropertyValues();
			pvs.addPropertyValue(new PropertyValue("age", new Integer(newAge)));
			pvs.addPropertyValue(new PropertyValue("name", newName));
			pvs.addPropertyValue(new PropertyValue("touchy", newTouchy));
			bw.setPropertyValues(pvs);
			assertTrue("Validly set property must stick", t.getName().equals(newName));
			assertTrue("Validly set property must stick", t.getTouchy().equals(newTouchy));
			assertTrue("Validly set property must stick", t.getAge() == newAge);
		}
		catch (BeansException ex) {
			fail("Shouldn't throw exception when everything is valid");
		}
	}

	public void testBeanWrapperUpdates() {
		TestBean t = new TestBean();
		int newAge = 33;
		try {
			BeanWrapper bw = new BeanWrapperImpl(t);
			t.setAge(newAge);
			Object bwAge = bw.getPropertyValue("age");
			assertTrue("Age is an integer", bwAge instanceof Integer);
			int bwi = ((Integer) bwAge).intValue();
			assertTrue("Bean wrapper must pick up changes", bwi == newAge);
		}
		catch (Exception ex) {
			fail("Shouldn't throw exception when everything is valid");
		}
	}

	public void testValidNullUpdate() {
		TestBean t = new TestBean();
		t.setName("Frank");	// we need to change it back
		t.setSpouse(t);
		BeanWrapper bw = new BeanWrapperImpl(t);
		assertTrue("name is not null to start off", t.getName() != null);
		bw.setPropertyValue("name", null);
		assertTrue("name is now null", t.getName() == null);
		// Now test with non-string
		assertTrue("spouse is not null to start off", t.getSpouse() != null);
		bw.setPropertyValue("spouse", null);
		assertTrue("spouse is now null", t.getSpouse() == null);
	}

	/**
	 * Test default conversion of properties
	 */
	public void testPropertiesProperty() throws Exception {
		PropsTest pt = new PropsTest();
		BeanWrapper bw = new BeanWrapperImpl(pt);
		bw.setPropertyValue("name", "ptest");

		// Note format...
		String ps = "peace=war\nfreedom=slavery";
		bw.setPropertyValue("properties", ps);

		assertTrue("name was set", pt.name.equals("ptest"));
		assertTrue("props non null", pt.props != null);
		String freedomVal = pt.props.getProperty("freedom");
		String peaceVal = pt.props.getProperty("peace");
		assertTrue("peace==war", peaceVal.equals("war"));
		assertTrue("Freedom==slavery", freedomVal.equals("slavery"));
	}

	public void testStringArrayProperty() throws Exception {
		PropsTest pt = new PropsTest();
		BeanWrapper bw = new BeanWrapperImpl(pt);

		bw.setPropertyValue("stringArray", "foo,fi,fi,fum");
		assertTrue("stringArray length = 4", pt.stringArray.length == 4);
		assertTrue("correct values", pt.stringArray[0].equals("foo") && pt.stringArray[1].equals("fi") &&
		                             pt.stringArray[2].equals("fi") && pt.stringArray[3].equals("fum"));

		bw.setPropertyValue("stringArray", new String[] {"foo", "fi", "fi", "fum"});
		assertTrue("stringArray length = 4", pt.stringArray.length == 4);
		assertTrue("correct values", pt.stringArray[0].equals("foo") && pt.stringArray[1].equals("fi") &&
		                             pt.stringArray[2].equals("fi") && pt.stringArray[3].equals("fum"));

		bw.setPropertyValue("stringArray", "one");
		assertTrue("stringArray length = 1", pt.stringArray.length == 1);
		assertTrue("stringArray elt is ok", pt.stringArray[0].equals("one"));
	}

	public void testIntArrayProperty() {
		PropsTest pt = new PropsTest();
		BeanWrapper bw = new BeanWrapperImpl(pt);

		bw.setPropertyValue("intArray", new int[] {4, 5, 2, 3});
		assertTrue("intArray length = 4", pt.intArray.length == 4);
		assertTrue("correct values", pt.intArray[0] == 4 && pt.intArray[1] == 5 &&
		                             pt.intArray[2] == 2 && pt.intArray[3] == 3);

		bw.setPropertyValue("intArray", new String[] {"4", "5", "2", "3"});
		assertTrue("intArray length = 4", pt.intArray.length == 4);
		assertTrue("correct values", pt.intArray[0] == 4 && pt.intArray[1] == 5 &&
		                             pt.intArray[2] == 2 && pt.intArray[3] == 3);

		bw.setPropertyValue("intArray", new Integer(1));
		assertTrue("intArray length = 4", pt.intArray.length == 1);
		assertTrue("correct values", pt.intArray[0] == 1);

		bw.setPropertyValue("intArray", new String[] {"1"});
		assertTrue("intArray length = 4", pt.intArray.length == 1);
		assertTrue("correct values", pt.intArray[0] == 1);
	}

	public void testIndividualAllValid() {
		TestBean t = new TestBean();
		String newName = "tony";
		int newAge = 65;
		String newTouchy = "valid";
		try {
			BeanWrapper bw = new BeanWrapperImpl(t);
			bw.setPropertyValue("age", new Integer(newAge));
			bw.setPropertyValue(new PropertyValue("name", newName));
			bw.setPropertyValue(new PropertyValue("touchy", newTouchy));
			assertTrue("Validly set property must stick", t.getName().equals(newName));
			assertTrue("Validly set property must stick", t.getTouchy().equals(newTouchy));
			assertTrue("Validly set property must stick", t.getAge() == newAge);
		}
		catch (BeansException ex) {
			fail("Shouldn't throw exception when everything is valid");
		}
	}

	public void test2Invalid() {
		TestBean t = new TestBean();
		String newName = "tony";
		String invalidTouchy = ".valid";
		try {
			BeanWrapper bw = new BeanWrapperImpl(t);
			//System.out.println(bw);
			MutablePropertyValues pvs = new MutablePropertyValues();
			pvs.addPropertyValue(new PropertyValue("age", "foobar"));
			pvs.addPropertyValue(new PropertyValue("name", newName));
			pvs.addPropertyValue(new PropertyValue("touchy", invalidTouchy));
			bw.setPropertyValues(pvs);
			fail("Should throw exception when everything is valid");
		}
		catch (PropertyAccessExceptionsException ex) {
			assertTrue("Must contain 2 exceptions", ex.getExceptionCount() == 2);
			// Test validly set property matches
			assertTrue("Validly set property must stick", t.getName().equals(newName));
			assertTrue("Invalidly set property must retain old value", t.getAge() == 0);
			assertTrue("New value of dodgy setter must be available through exception",
				ex.getPropertyAccessException("touchy").getPropertyChangeEvent().getNewValue().equals(invalidTouchy));
		}
		catch (Exception ex) {
			fail("Shouldn't throw exception other than pvee");
		}
	}

	public void testTypeMismatch() {
		TestBean t = new TestBean();
		try {
			BeanWrapper bw = new BeanWrapperImpl(t);
			bw.setPropertyValue("age", "foobar");
			fail("Should throw exception on type mismatch");
		}
		catch (TypeMismatchException ex) {
			// expected
		}
		catch (Exception ex) {
			fail("Shouldn't throw exception other than Type mismatch");
		}
	}

	public void testEmptyValueForPrimitiveProperty() {
		TestBean t = new TestBean();
		try {
			BeanWrapper bw = new BeanWrapperImpl(t);
			bw.setPropertyValue("age", "");
			fail("Should throw exception on type mismatch");
		}
		catch (TypeMismatchException ex) {
			// expected
		}
		catch (Exception ex) {
			fail("Shouldn't throw exception other than Type mismatch");
		}
	}

	public void testSetPropertyValuesIgnoresInvalidNestedOnRequest() {
		ITestBean rod = new TestBean();
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue(new PropertyValue("name", "rod"));
		pvs.addPropertyValue(new PropertyValue("graceful.rubbish", "tony"));
		pvs.addPropertyValue(new PropertyValue("more.garbage", new Object()));
		BeanWrapper bw = new BeanWrapperImpl(rod);
		bw.setPropertyValues(pvs, true);
		assertTrue("Set valid and ignored invalid", rod.getName().equals("rod"));
		try {
			// Don't ignore: should fail
			bw.setPropertyValues(pvs, false);
			fail("Shouldn't have ignored invalid updates");
		}
		catch (NotWritablePropertyException ex) {
			// OK: but which exception??
		}
	}

	public void testGetNestedProperty() {
		ITestBean rod = new TestBean("rod", 31);
		ITestBean kerry = new TestBean("kerry", 35);
		rod.setSpouse(kerry);
		kerry.setSpouse(rod);
		BeanWrapper bw = new BeanWrapperImpl(rod);
		Integer KA = (Integer) bw.getPropertyValue("spouse.age");
		assertTrue("kerry is 35", KA.intValue() == 35);
		Integer RA = (Integer) bw.getPropertyValue("spouse.spouse.age");
		assertTrue("rod is 31, not" + RA, RA.intValue() == 31);
		ITestBean spousesSpouse = (ITestBean) bw.getPropertyValue("spouse.spouse");
		assertTrue("spousesSpouse = initial point", rod == spousesSpouse);
	}

	public void testGetNestedPropertyNullValue() throws Exception {
		ITestBean rod = new TestBean("rod", 31);
		ITestBean kerry = new TestBean("kerry", 35);
		rod.setSpouse(kerry);

		BeanWrapper bw = new BeanWrapperImpl(rod);
		try {
			bw.getPropertyValue("spouse.spouse.age");
			fail("Shouldn't have succeded with null path");
		}
		catch (NullValueInNestedPathException ex) {
			// ok
			assertTrue("it was the spouse property that was null, not " + ex.getPropertyName(), ex.getPropertyName().equals("spouse"));
		}
	}

	public void testSetNestedProperty() throws Exception {
		ITestBean rod = new TestBean("rod", 31);
		ITestBean kerry = new TestBean("kerry", 0);

		BeanWrapper bw = new BeanWrapperImpl(rod);
		bw.setPropertyValue("spouse", kerry);

		assertTrue("nested set worked", rod.getSpouse() == kerry);
		assertTrue("no back relation", kerry.getSpouse() == null);
		bw.setPropertyValue(new PropertyValue("spouse.spouse", rod));
		assertTrue("nested set worked", kerry.getSpouse() == rod);
		assertTrue("kerry age not set", kerry.getAge()==0);
		bw.setPropertyValue(new PropertyValue("spouse.age", new Integer(35)));
		assertTrue("Set primitive on spouse", kerry.getAge()==35);
	}

	public void testSetNestedPropertyNullValue() throws Exception {
		ITestBean rod = new TestBean("rod", 31);
		BeanWrapper bw = new BeanWrapperImpl(rod);
		try {
			bw.setPropertyValue("spouse.age", new Integer(31));
			fail("Shouldn't have succeded with null path");
		}
		catch (NullValueInNestedPathException ex) {
			// ok
			assertTrue("it was the spouse property that was null, not " + ex.getPropertyName(), ex.getPropertyName().equals("spouse"));
		}
	}

	public void testSetNestedPropertyPolymorphic() throws Exception {
		ITestBean rod = new TestBean("rod", 31);
		ITestBean kerry = new Employee();

		BeanWrapper bw = new BeanWrapperImpl(rod);
		bw.setPropertyValue("spouse", kerry);
		bw.setPropertyValue("spouse.age", new Integer(35));
		bw.setPropertyValue("spouse.name", "Kerry");
		bw.setPropertyValue("spouse.company", "Lewisham");
		assertTrue("kerry name is Kerry", kerry.getName().equals("Kerry"));

		assertTrue("nested set worked", rod.getSpouse() == kerry);
		assertTrue("no back relation", kerry.getSpouse() == null);
		bw.setPropertyValue(new PropertyValue("spouse.spouse", rod));
		assertTrue("nested set worked", kerry.getSpouse() == rod);

		BeanWrapper kbw = new BeanWrapperImpl(kerry);
		assertTrue("spouse.spouse.spouse.spouse.company=Lewisham",
			"Lewisham".equals(kbw.getPropertyValue("spouse.spouse.spouse.spouse.company")));
	}

	public void testNewWrappedInstancePropertyValuesGet() {
		BeanWrapper bw = new BeanWrapperImpl();

		TestBean t = new TestBean("Tony", 50);
		bw.setWrappedInstance(t);
		assertEquals("Bean wrapper returns wrong property value", new Integer(t.getAge()), bw.getPropertyValue("age"));

		TestBean u = new TestBean("Udo", 30);
		bw.setWrappedInstance(u);
		assertEquals("Bean wrapper returns cached property value", new Integer(u.getAge()), bw.getPropertyValue("age"));
	}

	public void testNewWrappedInstanceNestedPropertyValuesGet() {
		BeanWrapper bw = new BeanWrapperImpl();

		TestBean t = new TestBean("Tony", 50);
		t.setSpouse(new TestBean("Sue", 40));
		bw.setWrappedInstance(t);
		assertEquals("Bean wrapper returns wrong nested property value", new Integer(t.getSpouse().getAge()), bw.getPropertyValue("spouse.age"));

		TestBean u = new TestBean("Udo", 30);
		u.setSpouse(new TestBean("Vera", 20));
		bw.setWrappedInstance(u);
		assertEquals("Bean wrapper returns cached nested property value", new Integer(u.getSpouse().getAge()), bw.getPropertyValue("spouse.age"));
	}

	public void testNullObject() {
		try {
			BeanWrapper bw = new BeanWrapperImpl((Object) null);
			fail ("Must throw an exception when constructed with null object");
		}
		catch (BeansException ex) {
			// We should get here
			//ex.printStackTrace();
		}
	}

	public void testNestedProperties() {
		String doctorCompany = "";
		String lawyerCompany = "Dr. Sueem";
		TestBean tb = new TestBean();
		BeanWrapper bw = new BeanWrapperImpl(tb);
		bw.setPropertyValue("doctor.company", doctorCompany);
		bw.setPropertyValue("lawyer.company", lawyerCompany);
		assertEquals(doctorCompany, tb.getDoctor().getCompany());
		assertEquals(lawyerCompany, tb.getLawyer().getCompany());
	}

	public void testIndexedProperties() {
		IndexedTestBean bean = new IndexedTestBean();
		BeanWrapper bw = new BeanWrapperImpl(bean);
		TestBean tb0 = bean.getArray()[0];
		TestBean tb1 = bean.getArray()[1];
		TestBean tb2 = ((TestBean) bean.getList().get(0));
		TestBean tb3 = ((TestBean) bean.getList().get(1));
		TestBean tb6 = ((TestBean) bean.getSet().toArray()[0]);
		TestBean tb7 = ((TestBean) bean.getSet().toArray()[1]);
		TestBean tb4 = ((TestBean) bean.getMap().get("key1"));
		TestBean tb5 = ((TestBean) bean.getMap().get("key2"));
		assertEquals("name0", tb0.getName());
		assertEquals("name1", tb1.getName());
		assertEquals("name2", tb2.getName());
		assertEquals("name3", tb3.getName());
		assertEquals("name6", tb6.getName());
		assertEquals("name7", tb7.getName());
		assertEquals("name4", tb4.getName());
		assertEquals("name5", tb5.getName());
		assertEquals("name0", bw.getPropertyValue("array[0].name"));
		assertEquals("name1", bw.getPropertyValue("array[1].name"));
		assertEquals("name2", bw.getPropertyValue("list[0].name"));
		assertEquals("name3", bw.getPropertyValue("list[1].name"));
		assertEquals("name6", bw.getPropertyValue("set[0].name"));
		assertEquals("name7", bw.getPropertyValue("set[1].name"));
		assertEquals("name4", bw.getPropertyValue("map[key1].name"));
		assertEquals("name5", bw.getPropertyValue("map[key2].name"));
		assertEquals("name4", bw.getPropertyValue("map['key1'].name"));
		assertEquals("name5", bw.getPropertyValue("map[\"key2\"].name"));

		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("array[0].name", "name5");
		pvs.addPropertyValue("array[1].name", "name4");
		pvs.addPropertyValue("list[0].name", "name3");
		pvs.addPropertyValue("list[1].name", "name2");
		pvs.addPropertyValue("set[0].name", "name8");
		pvs.addPropertyValue("set[1].name", "name9");
		pvs.addPropertyValue("map[key1].name", "name1");
		pvs.addPropertyValue("map['key2'].name", "name0");
		bw.setPropertyValues(pvs);
		assertEquals("name5", tb0.getName());
		assertEquals("name4", tb1.getName());
		assertEquals("name3", tb2.getName());
		assertEquals("name2", tb3.getName());
		assertEquals("name1", tb4.getName());
		assertEquals("name0", tb5.getName());
		assertEquals("name5", bw.getPropertyValue("array[0].name"));
		assertEquals("name4", bw.getPropertyValue("array[1].name"));
		assertEquals("name3", bw.getPropertyValue("list[0].name"));
		assertEquals("name2", bw.getPropertyValue("list[1].name"));
		assertEquals("name8", bw.getPropertyValue("set[0].name"));
		assertEquals("name9", bw.getPropertyValue("set[1].name"));
		assertEquals("name1", bw.getPropertyValue("map[\"key1\"].name"));
		assertEquals("name0", bw.getPropertyValue("map['key2'].name"));
	}

	public void testIndexedPropertiesWithCustomEditorForType() {
		IndexedTestBean bean = new IndexedTestBean();
		BeanWrapper bw = new BeanWrapperImpl(bean);
		bw.registerCustomEditor(String.class, null, new PropertyEditorSupport() {
			public void setAsText(String text) throws IllegalArgumentException {
				setValue("prefix" + text);
			}
		});
		TestBean tb0 = bean.getArray()[0];
		TestBean tb1 = bean.getArray()[1];
		TestBean tb2 = ((TestBean) bean.getList().get(0));
		TestBean tb3 = ((TestBean) bean.getList().get(1));
		TestBean tb4 = ((TestBean) bean.getMap().get("key1"));
		TestBean tb5 = ((TestBean) bean.getMap().get("key2"));
		assertEquals("name0", tb0.getName());
		assertEquals("name1", tb1.getName());
		assertEquals("name2", tb2.getName());
		assertEquals("name3", tb3.getName());
		assertEquals("name4", tb4.getName());
		assertEquals("name5", tb5.getName());
		assertEquals("name0", bw.getPropertyValue("array[0].name"));
		assertEquals("name1", bw.getPropertyValue("array[1].name"));
		assertEquals("name2", bw.getPropertyValue("list[0].name"));
		assertEquals("name3", bw.getPropertyValue("list[1].name"));
		assertEquals("name4", bw.getPropertyValue("map[key1].name"));
		assertEquals("name5", bw.getPropertyValue("map[key2].name"));
		assertEquals("name4", bw.getPropertyValue("map['key1'].name"));
		assertEquals("name5", bw.getPropertyValue("map[\"key2\"].name"));

		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("array[0].name", "name5");
		pvs.addPropertyValue("array[1].name", "name4");
		pvs.addPropertyValue("list[0].name", "name3");
		pvs.addPropertyValue("list[1].name", "name2");
		pvs.addPropertyValue("map[key1].name", "name1");
		pvs.addPropertyValue("map['key2'].name", "name0");
		bw.setPropertyValues(pvs);
		assertEquals("prefixname5", tb0.getName());
		assertEquals("prefixname4", tb1.getName());
		assertEquals("prefixname3", tb2.getName());
		assertEquals("prefixname2", tb3.getName());
		assertEquals("prefixname1", tb4.getName());
		assertEquals("prefixname0", tb5.getName());
		assertEquals("prefixname5", bw.getPropertyValue("array[0].name"));
		assertEquals("prefixname4", bw.getPropertyValue("array[1].name"));
		assertEquals("prefixname3", bw.getPropertyValue("list[0].name"));
		assertEquals("prefixname2", bw.getPropertyValue("list[1].name"));
		assertEquals("prefixname1", bw.getPropertyValue("map[\"key1\"].name"));
		assertEquals("prefixname0", bw.getPropertyValue("map['key2'].name"));
	}

	public void testIndexedPropertiesWithCustomEditorForProperty() {
		IndexedTestBean bean = new IndexedTestBean();
		BeanWrapper bw = new BeanWrapperImpl(bean);
		bw.registerCustomEditor(String.class, "array.name", new PropertyEditorSupport() {
			public void setAsText(String text) throws IllegalArgumentException {
				setValue("array" + text);
			}
		});
		bw.registerCustomEditor(String.class, "list.name", new PropertyEditorSupport() {
			public void setAsText(String text) throws IllegalArgumentException {
				setValue("list" + text);
			}
		});
		bw.registerCustomEditor(String.class, "map.name", new PropertyEditorSupport() {
			public void setAsText(String text) throws IllegalArgumentException {
				setValue("map" + text);
			}
		});
		TestBean tb0 = bean.getArray()[0];
		TestBean tb1 = bean.getArray()[1];
		TestBean tb2 = ((TestBean) bean.getList().get(0));
		TestBean tb3 = ((TestBean) bean.getList().get(1));
		TestBean tb4 = ((TestBean) bean.getMap().get("key1"));
		TestBean tb5 = ((TestBean) bean.getMap().get("key2"));
		assertEquals("name0", tb0.getName());
		assertEquals("name1", tb1.getName());
		assertEquals("name2", tb2.getName());
		assertEquals("name3", tb3.getName());
		assertEquals("name4", tb4.getName());
		assertEquals("name5", tb5.getName());
		assertEquals("name0", bw.getPropertyValue("array[0].name"));
		assertEquals("name1", bw.getPropertyValue("array[1].name"));
		assertEquals("name2", bw.getPropertyValue("list[0].name"));
		assertEquals("name3", bw.getPropertyValue("list[1].name"));
		assertEquals("name4", bw.getPropertyValue("map[key1].name"));
		assertEquals("name5", bw.getPropertyValue("map[key2].name"));
		assertEquals("name4", bw.getPropertyValue("map['key1'].name"));
		assertEquals("name5", bw.getPropertyValue("map[\"key2\"].name"));

		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("array[0].name", "name5");
		pvs.addPropertyValue("array[1].name", "name4");
		pvs.addPropertyValue("list[0].name", "name3");
		pvs.addPropertyValue("list[1].name", "name2");
		pvs.addPropertyValue("map[key1].name", "name1");
		pvs.addPropertyValue("map['key2'].name", "name0");
		bw.setPropertyValues(pvs);
		assertEquals("arrayname5", tb0.getName());
		assertEquals("arrayname4", tb1.getName());
		assertEquals("listname3", tb2.getName());
		assertEquals("listname2", tb3.getName());
		assertEquals("mapname1", tb4.getName());
		assertEquals("mapname0", tb5.getName());
		assertEquals("arrayname5", bw.getPropertyValue("array[0].name"));
		assertEquals("arrayname4", bw.getPropertyValue("array[1].name"));
		assertEquals("listname3", bw.getPropertyValue("list[0].name"));
		assertEquals("listname2", bw.getPropertyValue("list[1].name"));
		assertEquals("mapname1", bw.getPropertyValue("map[\"key1\"].name"));
		assertEquals("mapname0", bw.getPropertyValue("map['key2'].name"));
	}

	public void testIndexedPropertiesWithIndividualCustomEditorForProperty() {
		IndexedTestBean bean = new IndexedTestBean();
		BeanWrapper bw = new BeanWrapperImpl(bean);
		bw.registerCustomEditor(String.class, "array[0].name", new PropertyEditorSupport() {
			public void setAsText(String text) throws IllegalArgumentException {
				setValue("array0" + text);
			}
		});
		bw.registerCustomEditor(String.class, "array[1].name", new PropertyEditorSupport() {
			public void setAsText(String text) throws IllegalArgumentException {
				setValue("array1" + text);
			}
		});
		bw.registerCustomEditor(String.class, "list[0].name", new PropertyEditorSupport() {
			public void setAsText(String text) throws IllegalArgumentException {
				setValue("list0" + text);
			}
		});
		bw.registerCustomEditor(String.class, "list[1].name", new PropertyEditorSupport() {
			public void setAsText(String text) throws IllegalArgumentException {
				setValue("list1" + text);
			}
		});
		bw.registerCustomEditor(String.class, "map[key1].name", new PropertyEditorSupport() {
			public void setAsText(String text) throws IllegalArgumentException {
				setValue("mapkey1" + text);
			}
		});
		bw.registerCustomEditor(String.class, "map[key2].name", new PropertyEditorSupport() {
			public void setAsText(String text) throws IllegalArgumentException {
				setValue("mapkey2" + text);
			}
		});
		TestBean tb0 = bean.getArray()[0];
		TestBean tb1 = bean.getArray()[1];
		TestBean tb2 = ((TestBean) bean.getList().get(0));
		TestBean tb3 = ((TestBean) bean.getList().get(1));
		TestBean tb4 = ((TestBean) bean.getMap().get("key1"));
		TestBean tb5 = ((TestBean) bean.getMap().get("key2"));
		assertEquals("name0", tb0.getName());
		assertEquals("name1", tb1.getName());
		assertEquals("name2", tb2.getName());
		assertEquals("name3", tb3.getName());
		assertEquals("name4", tb4.getName());
		assertEquals("name5", tb5.getName());
		assertEquals("name0", bw.getPropertyValue("array[0].name"));
		assertEquals("name1", bw.getPropertyValue("array[1].name"));
		assertEquals("name2", bw.getPropertyValue("list[0].name"));
		assertEquals("name3", bw.getPropertyValue("list[1].name"));
		assertEquals("name4", bw.getPropertyValue("map[key1].name"));
		assertEquals("name5", bw.getPropertyValue("map[key2].name"));
		assertEquals("name4", bw.getPropertyValue("map['key1'].name"));
		assertEquals("name5", bw.getPropertyValue("map[\"key2\"].name"));

		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("array[0].name", "name5");
		pvs.addPropertyValue("array[1].name", "name4");
		pvs.addPropertyValue("list[0].name", "name3");
		pvs.addPropertyValue("list[1].name", "name2");
		pvs.addPropertyValue("map[key1].name", "name1");
		pvs.addPropertyValue("map['key2'].name", "name0");
		bw.setPropertyValues(pvs);
		assertEquals("array0name5", tb0.getName());
		assertEquals("array1name4", tb1.getName());
		assertEquals("list0name3", tb2.getName());
		assertEquals("list1name2", tb3.getName());
		assertEquals("mapkey1name1", tb4.getName());
		assertEquals("mapkey2name0", tb5.getName());
		assertEquals("array0name5", bw.getPropertyValue("array[0].name"));
		assertEquals("array1name4", bw.getPropertyValue("array[1].name"));
		assertEquals("list0name3", bw.getPropertyValue("list[0].name"));
		assertEquals("list1name2", bw.getPropertyValue("list[1].name"));
		assertEquals("mapkey1name1", bw.getPropertyValue("map[\"key1\"].name"));
		assertEquals("mapkey2name0", bw.getPropertyValue("map['key2'].name"));
	}

	public void testNestedIndexedPropertiesWithCustomEditorForProperty() {
		IndexedTestBean bean = new IndexedTestBean();
		TestBean tb0 = bean.getArray()[0];
		TestBean tb1 = bean.getArray()[1];
		TestBean tb2 = ((TestBean) bean.getList().get(0));
		TestBean tb3 = ((TestBean) bean.getList().get(1));
		TestBean tb4 = ((TestBean) bean.getMap().get("key1"));
		TestBean tb5 = ((TestBean) bean.getMap().get("key2"));
		tb0.setNestedIndexedBean(new IndexedTestBean());
		tb1.setNestedIndexedBean(new IndexedTestBean());
		tb2.setNestedIndexedBean(new IndexedTestBean());
		tb3.setNestedIndexedBean(new IndexedTestBean());
		tb4.setNestedIndexedBean(new IndexedTestBean());
		tb5.setNestedIndexedBean(new IndexedTestBean());
		BeanWrapper bw = new BeanWrapperImpl(bean);
		bw.registerCustomEditor(String.class, "array.nestedIndexedBean.array.name", new PropertyEditorSupport() {
			public void setAsText(String text) throws IllegalArgumentException {
				setValue("array" + text);
			}
		});
		bw.registerCustomEditor(String.class, "list.nestedIndexedBean.list.name", new PropertyEditorSupport() {
			public void setAsText(String text) throws IllegalArgumentException {
				setValue("list" + text);
			}
		});
		bw.registerCustomEditor(String.class, "map.nestedIndexedBean.map.name", new PropertyEditorSupport() {
			public void setAsText(String text) throws IllegalArgumentException {
				setValue("map" + text);
			}
		});
		assertEquals("name0", tb0.getName());
		assertEquals("name1", tb1.getName());
		assertEquals("name2", tb2.getName());
		assertEquals("name3", tb3.getName());
		assertEquals("name4", tb4.getName());
		assertEquals("name5", tb5.getName());
		assertEquals("name0", bw.getPropertyValue("array[0].nestedIndexedBean.array[0].name"));
		assertEquals("name1", bw.getPropertyValue("array[1].nestedIndexedBean.array[1].name"));
		assertEquals("name2", bw.getPropertyValue("list[0].nestedIndexedBean.list[0].name"));
		assertEquals("name3", bw.getPropertyValue("list[1].nestedIndexedBean.list[1].name"));
		assertEquals("name4", bw.getPropertyValue("map[key1].nestedIndexedBean.map[key1].name"));
		assertEquals("name5", bw.getPropertyValue("map['key2'].nestedIndexedBean.map[\"key2\"].name"));

		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("array[0].nestedIndexedBean.array[0].name", "name5");
		pvs.addPropertyValue("array[1].nestedIndexedBean.array[1].name", "name4");
		pvs.addPropertyValue("list[0].nestedIndexedBean.list[0].name", "name3");
		pvs.addPropertyValue("list[1].nestedIndexedBean.list[1].name", "name2");
		pvs.addPropertyValue("map[key1].nestedIndexedBean.map[\"key1\"].name", "name1");
		pvs.addPropertyValue("map['key2'].nestedIndexedBean.map[key2].name", "name0");
		bw.setPropertyValues(pvs);
		assertEquals("arrayname5", tb0.getNestedIndexedBean().getArray()[0].getName());
		assertEquals("arrayname4", tb1.getNestedIndexedBean().getArray()[1].getName());
		assertEquals("listname3", ((TestBean) tb2.getNestedIndexedBean().getList().get(0)).getName());
		assertEquals("listname2", ((TestBean) tb3.getNestedIndexedBean().getList().get(1)).getName());
		assertEquals("mapname1", ((TestBean) tb4.getNestedIndexedBean().getMap().get("key1")).getName());
		assertEquals("mapname0", ((TestBean) tb5.getNestedIndexedBean().getMap().get("key2")).getName());
		assertEquals("arrayname5", bw.getPropertyValue("array[0].nestedIndexedBean.array[0].name"));
		assertEquals("arrayname4", bw.getPropertyValue("array[1].nestedIndexedBean.array[1].name"));
		assertEquals("listname3", bw.getPropertyValue("list[0].nestedIndexedBean.list[0].name"));
		assertEquals("listname2", bw.getPropertyValue("list[1].nestedIndexedBean.list[1].name"));
		assertEquals("mapname1", bw.getPropertyValue("map['key1'].nestedIndexedBean.map[key1].name"));
		assertEquals("mapname0", bw.getPropertyValue("map[key2].nestedIndexedBean.map[\"key2\"].name"));
	}

	public void testIndexedPropertiesWithDirectAccess() {
		IndexedTestBean bean = new IndexedTestBean();
		BeanWrapper bw = new BeanWrapperImpl(bean);
		TestBean tb0 = bean.getArray()[0];
		TestBean tb1 = bean.getArray()[1];
		TestBean tb2 = ((TestBean) bean.getList().get(0));
		TestBean tb3 = ((TestBean) bean.getList().get(1));
		TestBean tb6 = ((TestBean) bean.getSet().toArray()[0]);
		TestBean tb7 = ((TestBean) bean.getSet().toArray()[1]);
		TestBean tb4 = ((TestBean) bean.getMap().get("key1"));
		TestBean tb5 = ((TestBean) bean.getMap().get("key2"));
		assertEquals(tb0, bw.getPropertyValue("array[0]"));
		assertEquals(tb1, bw.getPropertyValue("array[1]"));
		assertEquals(tb2, bw.getPropertyValue("list[0]"));
		assertEquals(tb3, bw.getPropertyValue("list[1]"));
		assertEquals(tb6, bw.getPropertyValue("set[0]"));
		assertEquals(tb7, bw.getPropertyValue("set[1]"));
		assertEquals(tb4, bw.getPropertyValue("map[key1]"));
		assertEquals(tb5, bw.getPropertyValue("map[key2]"));
		assertEquals(tb4, bw.getPropertyValue("map['key1']"));
		assertEquals(tb5, bw.getPropertyValue("map[\"key2\"]"));

		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("array[0]", tb5);
		pvs.addPropertyValue("array[1]",tb4);
		pvs.addPropertyValue("list[0]", tb3);
		pvs.addPropertyValue("list[1]", tb2);
		pvs.addPropertyValue("list[2]",tb0);
		pvs.addPropertyValue("list[4]",tb1);
		pvs.addPropertyValue("map[key1]", tb1);
		pvs.addPropertyValue("map['key2']", tb0);
		pvs.addPropertyValue("map[key5]", tb4);
		pvs.addPropertyValue("map['key9']", tb5);
		bw.setPropertyValues(pvs);
		assertEquals(tb5, bean.getArray()[0]);
		assertEquals(tb4, bean.getArray()[1]);
		assertEquals(tb3, ((TestBean) bean.getList().get(0)));
		assertEquals(tb2, ((TestBean) bean.getList().get(1)));
		assertEquals(tb0, ((TestBean) bean.getList().get(2)));
		assertEquals(null, ((TestBean) bean.getList().get(3)));
		assertEquals(tb1, ((TestBean) bean.getList().get(4)));
		assertEquals(tb1, ((TestBean) bean.getMap().get("key1")));
		assertEquals(tb0, ((TestBean) bean.getMap().get("key2")));
		assertEquals(tb4, ((TestBean) bean.getMap().get("key5")));
		assertEquals(tb5, ((TestBean) bean.getMap().get("key9")));
		assertEquals(tb5, bw.getPropertyValue("array[0]"));
		assertEquals(tb4, bw.getPropertyValue("array[1]"));
		assertEquals(tb3, bw.getPropertyValue("list[0]"));
		assertEquals(tb2, bw.getPropertyValue("list[1]"));
		assertEquals(tb0, bw.getPropertyValue("list[2]"));
		assertEquals(null, bw.getPropertyValue("list[3]"));
		assertEquals(tb1, bw.getPropertyValue("list[4]"));
		assertEquals(tb1, bw.getPropertyValue("map[\"key1\"]"));
		assertEquals(tb0, bw.getPropertyValue("map['key2']"));
		assertEquals(tb4, bw.getPropertyValue("map[\"key5\"]"));
		assertEquals(tb5, bw.getPropertyValue("map['key9']"));
	}

	public void testArrayToArrayConversion() throws PropertyVetoException {
		IndexedTestBean tb = new IndexedTestBean();
		BeanWrapper bw = new BeanWrapperImpl(tb);
		bw.registerCustomEditor(TestBean.class, new PropertyEditorSupport() {
			public void setAsText(String text) throws IllegalArgumentException {
				setValue(new TestBean(text, 99));
			}
		});
		bw.setPropertyValue("array", new String[] {"a", "b"});
		assertEquals(2, tb.getArray().length);
		assertEquals("a", tb.getArray()[0].getName());
		assertEquals("b", tb.getArray()[1].getName());
	}

	public void testArrayToStringConversion() throws PropertyVetoException {
		TestBean tb = new TestBean();
		BeanWrapper bw = new BeanWrapperImpl(tb);
		bw.registerCustomEditor(String.class, new PropertyEditorSupport() {
			public void setAsText(String text) throws IllegalArgumentException {
				setValue("-" + text + "-");
			}
		});
		bw.setPropertyValue("name", new String[] {"a", "b"});
		assertEquals("-a,b-", tb.getName());
	}

	public void testPrimitiveArray() {
		PrimitiveArrayBean tb = new PrimitiveArrayBean();
		BeanWrapper bw = new BeanWrapperImpl(tb);
		bw.setPropertyValue("array", new String[] {"1", "2"});
		assertEquals(2, tb.getArray().length);
		assertEquals(1, tb.getArray()[0]);
		assertEquals(2, tb.getArray()[1]);
	}


	private static class NoRead {

		public void setAge(int age) {
		}
	}


	private static class PropsTest {

		private Properties props;

		private String name;

		private String[] stringArray;

		private int[] intArray;

		public void setProperties(Properties p) {
			props = p;
		}

		public void setName(String name) {
			this.name = name;
		}

		public void setStringArray(String[] sa) {
			this.stringArray = sa;
		}

		public void setIntArray(int[] intArray) {
			this.intArray = intArray;
		}
	}


	private static class GetterBean {

		private String name;

		public void setName(String name) {
			this.name = name;
		}

		public String getName() {
			if (this.name == null) {
				throw new RuntimeException("name property must be set");
			}
			return name;
		}
	}


	private static class ThrowsException {

		public void doSomething(Throwable t) throws Throwable {
			throw t;
		}
	}


	private static class PrimitiveArrayBean {

		private int[] array;

		public int[] getArray() {
			return array;
		}

		public void setArray(int[] array) {
			this.array = array;
		}
	}

}
