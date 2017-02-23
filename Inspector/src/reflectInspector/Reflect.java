package reflectInspector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

public class Reflect {

	public static void main(String[] args) throws IOException {
		Class mistery = null;
		List<Boolean>initialDecision=new ArrayList();
		boolean generate;
		boolean serializable;
        String urlMisteryClass=JOptionPane.showInputDialog( "Give me the path of the jar that contains the class you want to transform ",null);
		String  misteryName=JOptionPane.showInputDialog( "Give me the qualified name of a class",null);

//get the url class
File file  = new File(urlMisteryClass);
URL url = file.toURI().toURL();
URL[] urls = new URL[]{url};
ClassLoader cl = new URLClassLoader(urls);
		try {
			// get the class
			mistery= Class.forName(misteryName, true, cl);    
			//mistery = Class.forName("reflectInspector.Person"); 

		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}
		//check if generate a bean and if it has to implements serializable or not
		initialDecision=new Reflect().discover(mistery);
		
       
		generate=initialDecision.get(0);
		serializable=initialDecision.get(1)	;
		
		
		if(generate){
		
		String filename = mistery.getSimpleName() + "Gen";
		PrintWriter writer = new PrintWriter(filename + ".java", "UTF-8");
		//initialize map used to check get and set methods
		System.out.println(filename+" has been generated");
		Map<String, String> getChecker = new HashMap<>();
		Map<String, String> setChecker = new HashMap<>();
		Map<String, String> variableinfo = new HashMap<>();

		if(serializable=true){
			//start generation serializable bean
			System.out.println("start writing serializable class ");
			initializeSerializableFile(writer, filename,mistery.getPackage().getName());}
		else{
			//start generation normal bean
			System.out.println("start writing not serializable class ");
			initializeFile(writer, filename,mistery.getPackage().getName());}
		//get fields from input class and 
		System.out.println("start writing fields ");
		new Reflect().field(mistery, writer, getChecker, setChecker, variableinfo);
		//get methods from input class
		System.out.println("start writing methods ");
		new Reflect().method(mistery, getChecker, setChecker, writer);
		System.out.println("start writing missing get and set methods ");
		// check if some attributes have not their get method
		for (String key : getChecker.keySet()) {
			if (getChecker.get(key) == "") {
				System.out.println("miss the method get for the attribute " + key + "\n"
						+ " a new getter method for attribute will automatically created");
				writer.println("//missed get method in the input Class for field "+key+"\n//get method autogenerated");
				createGetMethod(writer, variableinfo.get(key), "get" + key, key);
			}
		}

		// check if some attributes have not their setter method
		for (String key : setChecker.keySet()) {
			System.out.println(key + " " + setChecker.get(key));
			if (setChecker.get(key) == "") {
				System.out.println("miss the method set for the attribute " + key + "\n"
						+ " a new setter method for attribute will automatically created");
				writer.println("//missed set method in the input Class for field "+key+"\n//set method autogenerated");
				createSetMethod(writer, variableinfo.get(key), "set" + key, key);
			}
		}
		System.out.println("start writing end of the file ");
		//write file end and close writer
		closeFile(writer);
		writer.close();
		System.out.println("pojo has been complitely transformed in a java bean");
		
		}System.exit(0);
	}

	 List<Boolean> discover(Class mistery) {
		//get class annotation, if annotation is @bean(Serializable=true) return <true,true>
		 // @bean(Serializable=false) return <true,false>
		 //without any annotation return <false,false>
		 return getClassAnnotationInfo(mistery, mistery.getSimpleName());
		}

	void field(Class mistery, PrintWriter writer, Map<String, String> getChecker, Map<String, String> setChecker,
			Map<String, String> variableinfo) {
		Field[] vars2 = mistery.getDeclaredFields();
		ArrayList<Field> vars = new ArrayList<>(Arrays.asList(vars2));
		
		for (Field var : vars) {
			//print in the file the fields
			writer.println("private " + var.getType().getSimpleName() + " " + var.getName() + ";");
			// put informations in the map
			getChecker.put(var.getName(), "");
			setChecker.put(var.getName(), "");
			variableinfo.put(var.getName(), var.getType().getSimpleName());}	
}

	void method(Class mistery, Map<String, String> getChecker, Map<String, String> setChecker, PrintWriter writer) {
//get constructor and generate only if the input class contains a constructor with parameters
		writer.println("\n\n //constructor autogenerated\n" + "public " + mistery.getSimpleName()+ "Gen(){}");
		Constructor[] cons = mistery.getConstructors();
		for (Constructor c : cons) {
			Class[] parTypes = c.getParameterTypes();
			if (parTypes.length != 0) {
				createConstructor(c, parTypes, writer, mistery.getSimpleName().toString());
			}
		}
//pass every method at the method getAnnotation
		Method[] ms = mistery.getDeclaredMethods();
		for (Method m : ms) {
			Class retType = m.getReturnType();
			Class[] parTypes = m.getParameterTypes();
			getMethodAnnotationInfo(m, m.getName(), getChecker, setChecker, writer, m, parTypes);
		}
	}

	
	void getMethodAnnotationInfo(AnnotatedElement o, String annotatedElementName, Map<String, String> getChecker,
			Map<String, String> setChecker, PrintWriter writer, Method m1, Class[] parTypes) {
		
		Annotation[] notes = o.getAnnotations();
		if (notes.length != 0) {
			for (Annotation note : notes) {
				Method[] ms = note.annotationType().getMethods();
				for (int i = 0; i < ms.length; i++) {
					Method m = ms[i];
					String methodName = m.getName();
					try {
						switch (methodName) {
						case "value":
							switch (m.invoke(note).toString()) {
							case "getter":
								if (ms[0].getName() == "referTo") {
									createGetMethod(writer, m1.getReturnType().getSimpleName(), annotatedElementName,
											ms[0].invoke(note).toString());
									getChecker.put(ms[0].invoke(note).toString(), annotatedElementName);
								
								}else{reprintMethod(m1, writer);}
								break;
							case "setter":
								if (ms[0].getName() == "referTo") {
									createSetMethod(writer, parTypes[0].getSimpleName(), annotatedElementName,
											ms[0].invoke(note).toString());
									setChecker.put(ms[0].invoke(note).toString(), annotatedElementName);
									
								}else{reprintMethod(m1, writer);}
								break;
							default:reprintMethod(m1, writer);break;
							}
							
						}

					} catch (SecurityException | IllegalAccessException | IllegalArgumentException
							| InvocationTargetException ex) {
						ex.printStackTrace();

					}
				}
			}

		} else {
			//print method without annotations;
			reprintMethod(m1, writer);}
	}
	
	 List<Boolean> getClassAnnotationInfo(AnnotatedElement o, String annotatedElementName){
		Annotation[] notes = o.getAnnotations();
		List <Boolean> returnState=new ArrayList();
		
		if (notes.length != 0) {
			for (Annotation note : notes){
				if (note.annotationType().getSimpleName().toString().equals("bean")){
					returnState.add(true);
					Method[] ms = note.annotationType().getMethods();
					Method m= ms[0];
					if (m.getName().equals("Serializable")){
						try {
							if(m.invoke(note).toString().equals("true")){
								returnState.add(true);
											
								
							}else{returnState.add(false);}
						} catch (IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}else{
						returnState.add(false);	
					}	
				}else{returnState.add(false);returnState.add(false);}
			}	
		}else{returnState.add(false);returnState.add(false);}
		return returnState;}
	 
	 
	static void initializeSerializableFile(PrintWriter writer, String fileName,String packageName) {
		writer.println("package"+packageName+"\n\n" + "import java.io.Serializable;\n\n" + "public class " + fileName
				+ "Gen implements Serializable{\n");
	}
	
	static void initializeFile(PrintWriter writer, String fileName,String packageName) {
		writer.println("package"+packageName+"\n\n" + "public class " + fileName
				+"{\n");
	}
	
	static void createGetMethod(PrintWriter writer, String type, String name, String variable) {
		writer.println("private " + type + " " + name + "(){");
		writer.println("return " + variable + ";");
		writer.println("}\n");
	}

	static void createSetMethod(PrintWriter writer, String type, String name, String variable) {
		writer.println("private void " + name + "(" + type + " " + variable + "){");
		writer.println("this." + variable + "=" + variable + ";");
		writer.println("}\n");
	}

	static void closeFile(PrintWriter writer) {
		writer.println("}");
		writer.close();
	}

	static void createConstructor(Constructor c, Class[] parTypes, PrintWriter writer, String className) {
		boolean first=true;
		char name = 'a';
		String params = "";
		for (Class t : parTypes) {

			if (first==true) {
				params += (t.getTypeName() + " " + name);
			} else {
				params += (","+t.getTypeName() + " " + name);
			}
			first=false;
			name++;
		}
		writer.println("public " + className + "Gen(" + params + "){}\n\n");
	}

	static void reprintMethod(Method m, PrintWriter writer) {
		String methodName = m.getName();
		String methodType = m.getReturnType().getSimpleName();
		Class[] parTypes = m.getParameterTypes();
		boolean first= true;
		char name = 'a';
		String params = "";
		for (Class t : parTypes) {
			if (first == true) {
				params += (t.getTypeName() + " " + name);
			} else {
				params += (","+t.getTypeName() + " " + name);
			}
			first=false;
			name++;
		}
		writer.println(Modifier.toString(m.getModifiers()) + " " + methodType + " " + methodName + "(" + params + "){");
		if (methodType != "void") {
			writer.println("return null;");
		}
		writer.println("}\n");
	}

	





}
	