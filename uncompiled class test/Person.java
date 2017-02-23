package reflectInspector.test;
@bean(Serializable=true)
public class Person {   
	private String firstname;		
	private String lastname;		
	private String birthdate;
	private Long personId;

	private int i;
	
	public String printHello(){
		return "Hello";}
	
	public Person(int a) {
		
	}
	
    @MethodNote(value="getter", referTo="firstname")    
	public String getFirstname() {
		return firstname;
		
	}
  
    @MethodNote(value="getter", referTo="birthdate") 
	public String getBirthdate() {
		return birthdate;
	}
	 @MethodNote(value="setter", referTo="birthdate")
	public void setBirthdate(String birthdate) {
		this.birthdate = birthdate;
	}

	
}
