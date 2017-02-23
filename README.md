# Writing-an-annotation-preprocessor
Annotations, java reflection, beans

# Introduction:

This assignment is about java annotations and java Reflection. Its goal was to create a project that
take as input a simple annotated java POJO and returns, if requested through proper annotations, a
new java file, containing the same POJO transformed in a java bean. The specification for the
annotations will be explained in the next chapter.
Before explaining how the application work I would like to clarify the meaning of “java bean” in
order to simplify the explanation of some choices:
a java bean is a java class standard. A java class for being a bean must respect the following
conventions:

- It must contain a public no-argument constructor;
- All the class properties must be private and need to be accessible by get and set methods.

Furthermore the class should be serializable (this is not strictly necessary) in order to manage the
bean state independently.

For the whole report please open report.pdf
