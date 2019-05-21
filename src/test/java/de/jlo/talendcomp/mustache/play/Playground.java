package de.jlo.talendcomp.mustache.play;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.mustachejava.Binding;
import com.github.mustachejava.Code;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.github.mustachejava.ObjectHandler;
import com.github.mustachejava.TemplateContext;
import com.github.mustachejava.codes.ValueCode;
import com.github.mustachejava.reflect.GuardedBinding;
import com.github.mustachejava.reflect.MissingWrapper;
import com.github.mustachejava.reflect.ReflectionObjectHandler;
import com.github.mustachejava.util.Wrapper;

public class Playground {

	public static void main(String[] args) throws IOException {
		play1();
	}

	private static final ReflectionObjectHandler REFLECTION_OBJECT_HANDLER = new ReflectionObjectHandler() {
        @Override
        public Binding createBinding(String name, final TemplateContext tc, final Code code) {
            return new GuardedBinding(this, name, tc, code) {
                @Override
                protected synchronized Wrapper getWrapper(String name, List<Object> scopes) {
                    Wrapper wrapper = super.getWrapper(name, scopes);
                    if (wrapper instanceof MissingWrapper && code instanceof ValueCode) {
                        throw new RuntimeException("Variable with name: " + name + " not found in data");
                    }
                    return wrapper;
                }
            };
        }
    };

	public static void play1() throws IOException {
		HashMap<String, Object> scopes = new HashMap<String, Object>();
		scopes.put("name", "Mustache");
//		scopes.put("feature", "Perfect?");
		scopes.put("number", 1234);
		scopes.put("bool", true);
		scopes.put("date", new Date());

		Writer writer = new OutputStreamWriter(System.out);
		MustacheFactory mf = new DefaultMustacheFactory() { 
		      @Override 
		      public ObjectHandler getObjectHandler() { 
		    	  return REFLECTION_OBJECT_HANDLER;
		      }
		};
		Mustache mustache = mf.compile(new StringReader("{{name}}, {{feature}}! {{number}} {{bool}} {{date}}"), "example");
		mustache.execute(writer, scopes);
		writer.flush();
	}

	public static void play2() throws IOException {
		Map<String, Object> scope = new HashMap<>();
		List<Object> list = new ArrayList<>(); 
		Value value1 = new Value();
		value1.name = "Jan";
		value1.bool = true;
		value1.date = new Date();
		list.add(value1);
		Value value2 = new Value();
		value2.name = "Lolling";
		value2.bool = false;
		value2.date = new Date();
		list.add(value2);
		scope.put("list", list);

		Writer writer = new OutputStreamWriter(System.out);
		MustacheFactory mf = new DefaultMustacheFactory();
		Mustache mustache = mf.compile(new StringReader("Begin\n{{#list}}"
				+ "\n{{name}} {{number}} {{bool}} {{date}} "
				+ "\n{{/list}}End"), "template");
		mustache.execute(writer, scope);
		writer.flush();
	}
	
	public static class Value {
		String name;
//		Number number;
		Boolean bool;
		Date date;

	}

}
