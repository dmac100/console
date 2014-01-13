package ansi;

public class AnsiStyle {
	public int start;
	public int length;
	public RGB foreground;
	public RGB background;
	public boolean underline;
	public boolean doubleUnderline;
	public boolean bold;
	public boolean italic;
	
	public AnsiStyle() {
	}
	
	public AnsiStyle(AnsiStyle style) {
		this.start = style.start;
		this.length = style.length;
		this.foreground = style.foreground;
		this.background = style.background;
		this.underline = style.underline;
		this.doubleUnderline = style.doubleUnderline;
		this.bold = style.bold;
		this.italic = style.italic;
	}
	
	public String toString() {
		String other = "";
		if(underline) other += "u";
		if(doubleUnderline) other += "d";
		if(bold) other += "b";
		if(italic) other += "i";
		if(!other.isEmpty()) {
			other = "(" + other + ")";
		}
		
		return start + "-" + length + " - " + foreground + "/" + background + " " + other;
	}
}
