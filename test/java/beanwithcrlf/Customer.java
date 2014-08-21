package java.beanwithcrlf;

public class Customer {

	public Object getA() 
	{
		return a;
	}
	
	public void setA(Object a) 
	{
		this.a = a;
	}
	
	public char[] getB() 
	{
		return b;
	}
	
	public void setB(char[] b) 
	{
		this.b = b;
	}
	
	public int[][] getNumbers() 
	{
		return numbers;
	}
	
	public void setNumbers(int[][] numbers) 
	{
		this.numbers = numbers;
	}
	
	Object a;
	char[] b;
	int[][] numbers;
	
}
