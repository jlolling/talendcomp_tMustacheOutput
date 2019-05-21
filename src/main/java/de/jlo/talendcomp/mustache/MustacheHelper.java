package de.jlo.talendcomp.mustache;

import java.io.StringReader;
import java.io.StringWriter;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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

public class MustacheHelper {
	
	private MustacheFactory mf = null;
	private Mustache mustache = null;
	private List<Object> data = new ArrayList<>();
	private Map<String, String> currentRow = new HashMap<>();
	private String nullReplacement = "";
	private String rootName = null;
	private Map<String, SimpleDateFormat> mapDateFormat = new HashMap<>();
	private Map<String, NumberFormat> mapNumberFormats = new HashMap<>();
	private Locale numberLocal = Locale.getDefault();
	private final ReflectionObjectHandler failIfDataColumnIsMissingReflectionObjectHelper = new ReflectionObjectHandler() {
        @Override
        public Binding createBinding(String name, final TemplateContext tc, final Code code) {
            return new GuardedBinding(this, name, tc, code) {
                @Override
                protected synchronized Wrapper getWrapper(String name, List<Object> scopes) {
                    Wrapper wrapper = super.getWrapper(name, scopes);
                    if (wrapper instanceof MissingWrapper && code instanceof ValueCode) {
                        throw new RuntimeException("Input column with name: " + name + " not found in data row, but expected in template!");
                    }
                    return wrapper;
                }
            };
        }
    };
    
	public void compileTemplate(String template, boolean strict) throws Exception {
		if (mf == null) {
			if (strict) {
				mf = new DefaultMustacheFactory() { 
					@Override 
					public ObjectHandler getObjectHandler() { 
						return failIfDataColumnIsMissingReflectionObjectHelper;
					}
				};
			} else {
				mf = new DefaultMustacheFactory();
			}
		}
		try {
			mustache = mf.compile(new StringReader(template), "template");
		} catch (Exception ex) {
			throw new Exception("Error while compiling template: " + template + ". Failure: " + ex.getMessage(), ex);
		}
	}
	
	public void setDateFormat(String columnName, String pattern) {
		mapDateFormat.put(columnName, new SimpleDateFormat(pattern));
	}
	
    private static Locale createLocale(String localeName) {
        if (localeName == null || localeName.length() == 0) {
            localeName = "en_US";
        }
        Locale locale = null;
        int pos = localeName.indexOf('_');
        if (pos > 1) {
            String language = localeName.substring(0, pos);
            String country = localeName.substring(pos + 1);
            locale = new Locale(language, country);
        } else {
            locale = new Locale(localeName);
        }
        return locale;
    }

	public void setNumberLocale(String localeStr) {
		numberLocal = createLocale(localeStr);
	}
	
	public void setNumberFormat(String columnName, int precision, boolean groupingUsed) {
		NumberFormat nf = NumberFormat.getInstance(numberLocal);
		nf.setGroupingUsed(groupingUsed);
		nf.setMaximumFractionDigits(precision);
		mapNumberFormats.put(columnName, nf);
	}
	
	public void newData() {
		currentRow = new HashMap<>();
	}
	
	public void setValue(String columnName, Object value, boolean nullable) throws Exception {
		if (value instanceof String) {
			currentRow.put(columnName, (String) value);
		} else if (value instanceof Number) {
			NumberFormat nf = mapNumberFormats.get(columnName);
			if (nf != null) {
				currentRow.put(columnName, nf.format(value));
			} else {
				currentRow.put(columnName, value.toString());
			}
		} else if (value instanceof Date) {
			SimpleDateFormat df = mapDateFormat.get(columnName);
			if (df != null) {
				currentRow.put(columnName, df.format((Date) value));
			} else {
				currentRow.put(columnName, value.toString());
			}
		} else if (value != null) {
			currentRow.put(columnName, value.toString());
		} else if (nullable) {
			currentRow.put(columnName, nullReplacement);
		} else {
			throw new Exception("Found null value in not nullable column: " + columnName);
		}
	}
	
	public void addDataToList() {
		data.add(currentRow);
	}
	
	public void addData(Object row) {
		data.add(row);
	}
	
	public void setRootName(String rootName) {
		if (rootName != null && rootName.trim().isEmpty() == false) {
			this.rootName = rootName;
		}
	}
	
	public String render() throws Exception {
		Object scopes = null;
		if (rootName != null) {
			Map<String, Object> mapscope = new HashMap<>();
			mapscope.put(rootName, data);
			for (Map.Entry<String, String> entry : currentRow.entrySet()) {
				if (entry.getKey().equals(rootName) == false) {
					mapscope.put(entry.getKey(), entry.getValue());
				}
			}
			scopes = mapscope;
		} else {
			scopes = currentRow;
		}
		StringWriter writer = new StringWriter();
		mustache.execute(writer, scopes);
		data.clear();
		currentRow.clear();
		return writer.toString();
	}

	public String getNullReplacement() {
		return nullReplacement;
	}

	public void setNullReplacement(String nullReplacement) {
		if (nullReplacement != null) {
			this.nullReplacement = nullReplacement;
		}
	}
	
}
