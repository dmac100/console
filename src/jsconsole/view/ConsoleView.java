package jsconsole.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import jsconsole.util.Callback;

public class ConsoleView {
	private Completion completion = new Completion();
	
	private List<String> history = new ArrayList<String>();
	private int historyPosition = 0;
	private String currentCommand = "";
	
	private JFrame frame;
	private JScrollPane scrollPane;
	private JTextPane textPane;
	private JTextField textField;

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
		this.textField = new JTextField();
		
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
        
        textPane.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent e) {
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
					} else {
						dismissCompletion();
					}
					
					if(e.getKeyCode() == KeyEvent.VK_ENTER) {
						runCommand();
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
		String command = textField.getText();
		append("> " + command + "\n", commandStyle);
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
		append(text + "\n", outputStyle);
	}
	
	public void appendError(String text) {
		append(text + "\n", errorStyle);
	}
	
	private void append(String text, AttributeSet attributes) {
		StyledDocument document = textPane.getStyledDocument();
		
		try {
			document.insertString(document.getLength(), text, attributes);
		} catch (BadLocationException e) {
			throw new RuntimeException("Can't append text", e);
		}
		
		scrollToBottom();
	}
	
	private void scrollToBottom() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JScrollBar scroll = scrollPane.getVerticalScrollBar();
				scroll.setValue(scroll.getMaximum());
			}
		});
	}
}
