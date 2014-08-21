package java.bean;

public class Customer {

	public int getA() {
		return a;
	}
	
	public void setA(int a) {
		this.a = a;
	}
	
	public char[] getB() {
		return b;
	}
	
	public void setB(char[] b) {
		this.b = b;
	}
	
	public int[][] getNumbers() {
		return numbers;
	}
	
	public void setNumbers(int[][] numbers) {
		this.numbers = numbers;
	}
	
	int a;
	char[] b;
	int[][] numbers;
	
}
