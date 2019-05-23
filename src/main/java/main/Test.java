package main;

public class Test {
	public static void main(String[] args) {
		asd lvl = asd.one;
		System.out.println(lvl.toString());
	}
}
enum asd{
	one(1),
	two(2);
	
	int number;
	
	private asd(int nr) {
		// TODO Auto-generated constructor stub
		this.number = nr;
	}
}
