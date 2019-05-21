package de.jlo.talendcomp.mustache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

import com.github.mustachejava.MustacheException;

public class TestMustacheHelper {
	
	@Test
	public void testSimple() throws Exception {
		class row1Struct {
			String name;
			String feature;
			Boolean bool;
			Number number;
			Date date;
		}
		row1Struct row1 = new row1Struct();
		row1.name = "NameXYZ";
		row1.feature = "Super-Feature";
		row1.bool = true;
		row1.date = new SimpleDateFormat("yyyy-MM-dd").parse("2019-04-04");
		row1.number = 12345.6789d;
		MustacheHelper h = new MustacheHelper();
		String template = "{{name}}, {{feature}}! {{number}} {{bool}} {{date}}";
		h.compileTemplate(template, true);
		h.setNumberLocale("de_DE");
		h.setDateFormat("date", "yyyy-MM-dd");
		h.setNumberFormat("number", 3, true);
		// set current row values
		h.newData();
		h.setValue("name", row1.name, false);
		h.setValue("feature", row1.feature, false);
		h.setValue("bool", row1.bool, false);
		h.setValue("date", row1.date, false);
		h.setValue("number", row1.number, false);
		String actual = h.render();
		System.out.println(actual);
		String expected = "NameXYZ, Super-Feature! 12.345,679 true 2019-04-04";
		assertEquals("Result doe not match", expected, actual);
	}
	
	@Test
	public void testNullableCheck() throws Exception {
		class row1Struct {
			String feature;
		}
		row1Struct row1 = new row1Struct();
		row1.feature = null;
		MustacheHelper h = new MustacheHelper();
		String template = "{{name}}, {{feature}}! {{number}} {{bool}} {{date}}";
		h.compileTemplate(template, true);
		h.setNumberLocale("de_DE");
		h.setDateFormat("date", "yyyy-MM-dd");
		h.setNumberFormat("number", 3, true);
		// set current row values
		h.newData();
		try {
			h.setValue("feature", row1.feature, false);
			assertTrue("Nullable check failed!", false);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			assertTrue("Null column not mentioned in message", e.getMessage().contains("feature"));
		}
	}

	@Test
	public void testSimpleFailedIfMissing() throws Exception {
		class row1Struct {
			String name;
			Boolean bool;
			Number number;
			Date date;
		}
		row1Struct row1 = new row1Struct();
		row1.name = "NameXYZ";
		row1.bool = true;
		row1.date = new SimpleDateFormat("yyyy-MM-dd").parse("2019-04-04");
		row1.number = 12345.6789d;
		MustacheHelper h = new MustacheHelper();
		String template = "{{name}}, {{my_missing_field}}! {{number}} {{bool}} {{date}}";
		h.compileTemplate(template, true);
		h.setNumberLocale("de_DE");
		h.setDateFormat("date", "yyyy-MM-dd");
		h.setNumberFormat("number", 3, true);
		// set current row values
		h.newData();
		h.setValue("name", row1.name, false);
		h.setValue("bool", row1.bool, false);
		h.setValue("date", row1.date, false);
		h.setValue("number", row1.number, false);
		try {
			h.render();
			assertTrue("Missing field not detected", false);
		} catch (MustacheException e) {
			System.out.println(e.getMessage());
			assertTrue("Missing field not mentioned", e.getMessage().contains("my_missing_field"));
		}
	}

	@Test
	public void testList() throws Exception {
		class row1Struct {
			String name;
			String feature;
			Boolean bool;
			Number number;
			Date date;
		}
		row1Struct row1 = new row1Struct();
		MustacheHelper h = new MustacheHelper();
		String template = "Last feature: {{feature}}\n{{#myList}}{{name}}, {{feature}}! {{number}} {{bool}} {{date}}\n{{/myList}}";
		h.compileTemplate(template, true);
		h.setNumberLocale("de_DE");
		h.setDateFormat("date", "yyyy-MM-dd");
		h.setNumberFormat("number", 3, true);
		h.setRootName("myList");
		// add row
		row1.name = "Name1";
		row1.feature = "Super-Feature1";
		row1.bool = false;
		row1.date = new SimpleDateFormat("yyyy-MM-dd").parse("2019-04-04");
		row1.number = 12345.6789d;
		h.newData();
		h.setValue("name", row1.name, false);
		h.setValue("feature", row1.feature, false);
		h.setValue("bool", row1.bool, false);
		h.setValue("date", row1.date, false);
		h.setValue("number", row1.number, false);
		h.addDataToList();
		// add row
		row1.name = "Name2";
		row1.feature = "Super-Feature2";
		row1.bool = true;
		row1.date = new SimpleDateFormat("yyyy-MM-dd").parse("2019-04-05");
		row1.number = 23456.6789d;
		h.newData();
		h.setValue("name", row1.name, false);
		h.setValue("feature", row1.feature, false);
		h.setValue("bool", row1.bool, false);
		h.setValue("date", row1.date, false);
		h.setValue("number", row1.number, false);
		h.addDataToList();
		String actual = h.render();
		System.out.println(actual);
		String expected = "Last feature: Super-Feature2\nName1, Super-Feature1! 12.345,679 false 2019-04-04\nName2, Super-Feature2! 23.456,679 true 2019-04-05\n";
		assertEquals("Result doe not match", expected, actual);
	}

}
