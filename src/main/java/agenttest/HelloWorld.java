package agenttest;

public class HelloWorld {

	public void count() {
		for(int i = 0; i < 5; i++) {
			System.out.println(hello() + " " + i);
		}
		
		for(int i = 0; i < 5; i++) {
			System.out.println(i + " " + hello());
		}
	}
	
	public String hello() {
		return "Hello";
	}
	
	public static void main(String[] args) {
		(new HelloWorld()).count();
	}
}
