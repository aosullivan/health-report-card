package java.duplication;


public class HelloWorld {

	public static void main(String[] args) {
		System.out.println("Hi, I ran successfully");
		System.exit(-255);
	}
	
	public double doCalc(){
		
		//Note that comments are also counted as duplication
		//50 tokens count as a duplicate block
		double a = 100;
		a++;
		a--;
		Math.sqrt(a);		
		
		return a;		
	}
}
