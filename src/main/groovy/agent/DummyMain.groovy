package agent

class DummyMain {
	public static void main(String[] args) {
		println """
Syntax is java -ea -javaagent:oculus.jar=package1,pack.age2,package3 <other java arguments....>

Please give required arguments."""
		
		System.exit(1);
	}
}
