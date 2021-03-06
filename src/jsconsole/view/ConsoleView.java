package jsconsole.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;

import jsconsole.ansi.AnsiParser;
import jsconsole.ansi.AnsiStyle;
import jsconsole.ansi.ParseResult;
import jsconsole.ansi.RGB;

import jsconsole.util.Callback;

public class ConsoleView {
	private final static int MAX_BUFFER_LINES = 30000;
	
	private Completion completion = new Completion();
	
	private List<String> history = new ArrayList<String>();
	private int historyPosition = 0;
	private String currentCommand = "";
	
	private JFrame frame;
	private JScrollPane scrollPane;
	private JTextPane textPane;
	private JTextArea textField;

	private Callback<String> commandCallback;
	
	private SimpleAttributeSet commandStyle = new SimpleAttributeSet();
	private SimpleAttributeSet outputStyle = new SimpleAttributeSet();
	private SimpleAttributeSet errorStyle = new SimpleAttributeSet();
	
	public ConsoleView() {
		this.frame = new JFrame();
		
		frame.setTitle("Console");
		
		this.textPane = new JTextPane() {
			public void paintComponent(Graphics g) {
                Graphics2D graphics2d = (Graphics2D) g;
                graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                super.paintComponent(g);
            }
		};
		this.textField = new JTextArea();
		
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		textPane.setEditable(false);
		
		textPane.setBorder(new EmptyBorder(3, 3, 3, 3));
		textField.setBorder(new EmptyBorder(3, 3, 3, 3));
		
		this.scrollPane = new JScrollPane(textPane);
		
		panel.add(scrollPane);
		panel.add(textField);
		
		textField.setMaximumSize(new Dimension(Integer.MAX_VALUE, textField.getPreferredSize().height));
		
		frame.add(panel);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(800, 600);
		
		textPane.setFont(new Font("monospaced", Font.PLAIN, 12));
		textPane.setBackground(new Color(39, 40, 34));
        textPane.setForeground(new Color(255, 255, 255));
        
        StyleConstants.setForeground(commandStyle, new Color(102, 217, 239));
		StyleConstants.setForeground(outputStyle, new Color(255, 255, 255));
		StyleConstants.setForeground(errorStyle, new Color(249, 38, 114));
        
		textPane.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				textField.requestFocusInWindow();
			}
		});
        
		textField.setFocusTraversalKeysEnabled(false);
		
		textField.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if(e.getModifiers() == 0) {
					if(e.getKeyCode() == KeyEvent.VK_DOWN) {
						historyDown();
					}
					
					if(e.getKeyCode() == KeyEvent.VK_UP) {
						historyUp();
					}
					
					if(e.getKeyCode() == KeyEvent.VK_TAB) {
						insertCompletion();
						e.consume();
					} else {
						dismissCompletion();
					}
					
					if(e.getKeyCode() == KeyEvent.VK_ENTER) {
						runCommand();
						e.consume();
					}
				} else if(e.getModifiers() == KeyEvent.SHIFT_MASK) {
					if(e.getKeyCode() == KeyEvent.VK_ENTER) {
						textField.setText(textField.getText() + "\n");
					}
				}
			}
		});
	}
	
	public void addWindowListener(WindowListener listener) {
		frame.addWindowListener(listener);
	}
	
	public void setHistory(List<String> history) {
		final int maxSize = 1000;
		if(history.size() > maxSize) {
			this.history = new ArrayList<String>(history).subList(history.size() - maxSize, history.size());
		} else {
			this.history = new ArrayList<String>(history);
		}
		this.historyPosition = this.history.size();
	}
	
	public List<String> getHistory() {
		return new ArrayList<String>(history);
	}

	private void historyUp() {
		if(historyPosition == history.size()) {
			currentCommand = textField.getText();
		}
			
		if(historyPosition > 0) {
			historyPosition -= 1;
			textField.setText(history.get(historyPosition));
		}
	}

	private void historyDown() {
		if(historyPosition >= history.size() - 1) {
			historyPosition = history.size();
			textField.setText(currentCommand);
		} else {
			historyPosition += 1;
			textField.setText(history.get(historyPosition));
		}
	}
	
	private void insertCompletion() {
		if(textField.getCaretPosition() == textField.getText().length()) {
			completion.setHistory(history);
			textField.setText(completion.getCompletion(textField.getText()));
			textField.setCaretPosition(textField.getText().length());
		}
	}
	
	private void dismissCompletion() {
		completion.dismiss();
	}
	
	private void runCommand() {
		String command = textField.getText().trim();
		append("> " + command + "\n", commandStyle, false);
		textField.setText("");
		
		history.add(command);
		historyPosition = history.size();
		
		if(commandCallback != null) {
			commandCallback.onCallback(command);
		}
		
		scrollToBottom();
	}

	public void show() {
		frame.setVisible(true);
		textField.grabFocus();
	}
	
	public void clear() {
		textPane.setText("");
		textField.setText("");
	}
	
	public void setCommandCallback(Callback<String> callback) {
		this.commandCallback = callback;
	}
	
	public void appendOutput(String text) {
		append(text + "\n", outputStyle, true);
	}
	
	public void appendError(String text) {
		append(text + "\n", errorStyle, true);
	}
	
	private void append(String text, AttributeSet attributes, boolean parseAnsi) {
		StyledDocument document = textPane.getStyledDocument();
		
		AnsiStyle lastStyle = new AnsiStyle();
		ParseResult parseResult = new AnsiParser().parseText(lastStyle, text);
		int offset = document.getLength();
		
		try {
			document.insertString(offset, parseResult.getNewText(), attributes);
		} catch (BadLocationException e) {
			throw new RuntimeException("Can't append text", e);
		}
		
		for(AnsiStyle ansiStyle:parseResult.getStyleRanges()) {
			RGB fg = ansiStyle.foreground;
			RGB bg = ansiStyle.background;
			
			SimpleAttributeSet attributeSet = new SimpleAttributeSet();
			
			if(fg != null) {
				StyleConstants.setForeground(attributeSet, new Color(fg.r, fg.g, fg.b));
			}
			if(bg != null) {
				StyleConstants.setBackground(attributeSet, new Color(bg.r, bg.g, bg.b));
			}
			if(ansiStyle.bold) {
				StyleConstants.setBold(attributeSet, true);
			}
			if(ansiStyle.italic) {
				StyleConstants.setItalic(attributeSet, true);
			}
			if(ansiStyle.underline) {
				StyleConstants.setUnderline(attributeSet, true);
			}
			if(ansiStyle.doubleUnderline) {
				StyleConstants.setUnderline(attributeSet, true);
			}
			
			document.setCharacterAttributes(ansiStyle.start + offset, ansiStyle.length, attributeSet, false);
		}
		
		limitBufferLength();
		
		scrollToBottom();
	}
	
	private void limitBufferLength() {
		StyledDocument document = textPane.getStyledDocument();
		
		Element element = document.getDefaultRootElement();
		int lines = element.getElementCount();
		if(lines > MAX_BUFFER_LINES) {
			int offset = element.getElement(lines - MAX_BUFFER_LINES).getEndOffset();
			try {
				document.remove(0, offset);
			} catch (BadLocationException e) {
				throw new RuntimeException("Can't remove text", e);
			}
		}
	}

	private void scrollToBottom() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JScrollBar scroll = scrollPane.getVerticalScrollBar();
				scroll.setValue(scroll.getMaximum());
			}
		});
	}

	public boolean isShowing() {
		return frame.isVisible();
	}
}
