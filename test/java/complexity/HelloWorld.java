package java.complexity;
public class HelloWorld {
	//ccn 1 ncss 2 (declaration and body)
	public int notcomplex() {
		return 1;
	}
	//ccn 2 ncss 4 (dec, if, then, return)
	public int complex(int a) {
		if (a < 0) a += 2;
		return 0;
	}
	//ccn 3 ncss 7  (dec, if, then, else, if, then, return)
	public int verycomplex(int a) {
		if (a > 0) a -= 2; 
		else if (a < 1) a += 2;
		return 0;
	}
}
