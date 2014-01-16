package jsconsole.ansi;

public class RGB {
	public final int r;
	public final int g;
	public final int b;

	public RGB(int r, int g, int b) {
		this.r = r;
		this.g = g;
		this.b = b;
	}
	
	public static RGB fromHex(String color) {
		if(color.matches("#[0-9a-fA-F]{3}")) {
			return new RGB(
				parseHex(color.substring(1, 2)),
				parseHex(color.substring(2, 3)),
				parseHex(color.substring(3, 4))
			);
		} else if(color.matches("#[0-9a-fA-F]{6}")) {
			return new RGB(
				parseHex(color.substring(1, 3)),
				parseHex(color.substring(3, 5)),
				parseHex(color.substring(5, 7))
			);
		} else {
			throw new IllegalArgumentException("Invalid color: " + color);
		}
	}

	private static int parseHex(String s) {
		if(s.length() == 2) {
			return Integer.parseInt(s, 16);
		} else if(s.length() == 1) {
			return Integer.parseInt(s + s, 16);
		} else {
			throw new IllegalArgumentException("Invalid hex: " + s);
		}
	}
	
	public String toString() {
		return "RGB(" + r + ", " + g + ", " + b + ")";
	}
}
