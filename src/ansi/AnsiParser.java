package ansi;

import java.util.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A parser to extract the ansi styles from text.
 * Adapted from: https://gist.github.com/sporkmonger/113079.
 */
public class AnsiParser {
	private Pattern controlSequencePattern = Pattern.compile("((\u009B|\u001B\\[)(\\d+;)*(\\d+)?[m])");
	private RGB[] colorTable = new RGB[20];
	
	public AnsiParser() {
		// Normal colors.
		this.colorTable[0] = RGB.fromHex("#000000");
		this.colorTable[1] = RGB.fromHex("#f92672");
		this.colorTable[2] = RGB.fromHex("#54f926");
		this.colorTable[3] = RGB.fromHex("#ecf926");
		this.colorTable[4] = RGB.fromHex("#2631f9");
		this.colorTable[5] = RGB.fromHex("#f926d4");
		this.colorTable[6] = RGB.fromHex("#66d9ef");
		this.colorTable[7] = RGB.fromHex("#eeeeee");
		
		// Bright colors.
		this.colorTable[10] = RGB.fromHex("#000000");
		this.colorTable[11] = RGB.fromHex("#ff0000");
		this.colorTable[12] = RGB.fromHex("#00ff00");
		this.colorTable[13] = RGB.fromHex("#ffff00");
		this.colorTable[14] = RGB.fromHex("#0000ff");
		this.colorTable[15] = RGB.fromHex("#ff00ff");
		this.colorTable[16] = RGB.fromHex("#00ffff");
		this.colorTable[17] = RGB.fromHex("#ffffff");
	}
	
	/**
	 * Parses text and extracts ANSI escape sequences from it.
	 * Returns the modified text and a list of styles to apply.
	 */
	public ParseResult parseText(AnsiStyle lastStyle, String text) {
		List<AnsiStyle> styles = new ArrayList<AnsiStyle>();
		StringBuffer modified = new StringBuffer();

		int removedCharacters = 0;
		
		AnsiStyle style = new AnsiStyle(lastStyle);
		
		Matcher matcher = controlSequencePattern.matcher(text);
		while(matcher.find()) {
			matcher.appendReplacement(modified, "");
			
			String controlSequence = matcher.group();
			
			int[] codes = parseSequenceCodes(controlSequence);
			
			style = buildStyleRange(style, codes);
			style.start = matcher.start() - removedCharacters;
			styles.add(new AnsiStyle(style));
			
			removedCharacters += controlSequence.length();
		}
		matcher.appendTail(modified);
		
		for(int i = 0; i < styles.size() - 1; i++) {
			AnsiStyle thisRange = styles.get(i);
			AnsiStyle nextRange = styles.get(i + 1);
			thisRange.length = nextRange.start - thisRange.start;
		}
		
		if(!styles.isEmpty()) {
			AnsiStyle lastRange = styles.get(styles.size() - 1);
			lastRange.length = text.length() - lastRange.start - removedCharacters;
		}
		
		return new ParseResult(modified.toString(), styles);
	}
	
	private int[] parseSequenceCodes(String controlSequence) {
		String codeSequence = null;
		if (controlSequence.charAt(0) == '\u009B') {
			codeSequence = controlSequence.substring(1, controlSequence.length() - 1);
		} else if (controlSequence.charAt(0) == '\u001B') {
			codeSequence = controlSequence.substring(2, controlSequence.length() - 1);
		} else {
			throw new RuntimeException("Invalid ANSI control sequence: '" + controlSequence + "'");
		}
		String[] codeStrings = codeSequence.toString().split(";");
		if (codeStrings.length == 0) {
			return new int[] { 0 };
		} else {
			int[] codes = new int[codeStrings.length];
			for (int i = 0; i < codeStrings.length; i++) {
				if (codeStrings[i].equals("")) {
					codes[i] = 0;
				} else {
					codes[i] = Integer.parseInt(codeStrings[i]);
				}
			}
			return codes;
		}
	}
	
	private AnsiStyle buildStyleRange(AnsiStyle lastStyle, int[] codes) {
		AnsiStyle newStyleRange = new AnsiStyle(lastStyle);
		
		for (int i = 0; i < codes.length; i++) {
			RGB tempColor = null;
			switch (codes[i]) {
			case 0:
				newStyleRange.foreground = null;
				newStyleRange.background = null;
				newStyleRange.bold = false;
				newStyleRange.underline = false;
				newStyleRange.doubleUnderline = false;
				break;
			case 1:
				newStyleRange.bold = true;
				break;
			case 2:
				// It's actually supposed to be faint, but there's no way to
				// display that
				newStyleRange.bold = false;
				newStyleRange.italic = false;
				break;
			case 3:
				newStyleRange.italic = true;
				break;
			case 4:
				newStyleRange.underline = true;
				newStyleRange.doubleUnderline = false;
				break;
			case 7:
				// Swap foreground and background
				tempColor = newStyleRange.foreground;
				newStyleRange.foreground = newStyleRange.background;
				newStyleRange.background = tempColor;
				break;
			case 21:
				newStyleRange.underline = true;
				newStyleRange.doubleUnderline = true;
				break;
			case 22:
				newStyleRange.bold = false;
				newStyleRange.italic = false;
				break;
			case 24:
				newStyleRange.underline = false;
				newStyleRange.doubleUnderline = false;
				break;
			case 27:
				// Technically, this should just unset reversed foreground, but
				// we're just going to reverse again.
				tempColor = newStyleRange.foreground;
				newStyleRange.foreground = newStyleRange.background;
				newStyleRange.background = tempColor;
				break;
			default:
				if (codes[i] >= 30 && codes[i] < 40) {
					newStyleRange.foreground = colorTable[codes[i] - 30];
				} else if (codes[i] >= 40 && codes[i] < 50) {
					newStyleRange.background = colorTable[codes[i] - 40];
				} else if (codes[i] >= 90 && codes[i] < 100) {
					newStyleRange.foreground = colorTable[codes[i] - 90 + 10];
				} else if (codes[i] >= 100 && codes[i] < 110) {
					newStyleRange.background = colorTable[codes[i] - 100 + 10];
				}
				break;
			}
		}
		return newStyleRange;
	}
}
