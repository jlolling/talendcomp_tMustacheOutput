package de.jlo.talendcomp.mustache.play;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

public class Example {

	List<Item> items() {
		return Arrays.asList(new Item("Item 1", "$19.99", Arrays.asList(new Feature("New!"), new Feature("Awesome!"))),
				new Item("Item 2", "$29.99", Arrays.asList(new Feature("Old."), new Feature("Ugly."))));
	}

	static class Item {
		Item(String name, String price, List<Feature> features) {
			this.name = name;
			this.price = price;
			this.features = features;
		}

		String name, price;
		List<Feature> features;
	}

	static class Feature {
		Feature(String description) {
			this.description = description;
		}

		String description;
	}

	public static void main(String[] args) throws IOException {
		String template = "{{#items}}\n"
			    + "Name: {{name}}\n"
			    + "Price: {{price}}\n"
			    + "  {{#features}}\n"
			    + "  Feature: {{description}}\n"
			    + "  {{/features}}\n"
			    + "{{/items}}";
		MustacheFactory mf = new DefaultMustacheFactory();
		Mustache mustache = mf.compile(new StringReader(template), "test");
		mustache.execute(new PrintWriter(System.out), new Example()).flush();
	}
}