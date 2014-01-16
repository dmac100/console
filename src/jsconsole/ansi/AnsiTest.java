package jsconsole.ansi;

public class AnsiTest {
	public void print() {
		for(int x = 30; x <= 37; x++) {
			System.out.print("\u001B[" + x + "m" + "Foreground " + x + "\u001B[0m");
			System.out.print("    ");
			System.out.print("\u001B[" + (x + 10) + "m" + "Background " + (x + 10) + "\u001B[0m");
			System.out.println();
		}
		
		System.out.println();
		
		for(int x = 90; x <= 97; x++) {
			System.out.print("\u001B[" + x + "m" + "Foreground " + x + "\u001B[0m");
			System.out.print("    ");
			System.out.print("\u001B[" + (x + 10) + "m" + "Background " + (x + 10) + "\u001B[0m");
			System.out.println();
		}
		
		System.out.println();
		
		System.out.println("\u001B[1m Bold \u001B[0m");
		System.out.println("\u001B[3m Italic \u001B[0m");
		System.out.println("\u001B[4m Underline \u001B[0m");
	}
	
	public void main(String[] args) {
		new AnsiTest().print();
	}
}
