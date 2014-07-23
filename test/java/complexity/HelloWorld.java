package java.complexity;
public class HelloWorld {
	//CC 1 lines 2
	public int notcomplex() {
		return 1;
	}
	//CC 2 lines 3
	public int complex(int a) {
		if (a < 0) a += 2;
		return 0;
	}
	//CC 3 lines 
	public int verycomplex(int a) {
		if (a > 0) a -= 2; 
		else if (a < 1) a += 2;
		return 0;
	}
}
