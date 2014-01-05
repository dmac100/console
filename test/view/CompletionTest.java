package view;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

public class CompletionTest {
	@Test
	public void getCompletion_noCompletion() {
		Completion completion = new Completion();
		completion.setHistory(Arrays.asList(
			"dc",
			"da",
			"db"
		));
		assertEquals("a", completion.getCompletion("a"));
		assertEquals("a", completion.getCompletion("a"));
	}
	
	@Test
	public void getCompletion() {
		Completion completion = new Completion();
		completion.setHistory(Arrays.asList(
			"ac",
			"aa",
			"bb"
		));
		assertEquals("aa", completion.getCompletion("a"));
		assertEquals("ac", completion.getCompletion("aa"));
		assertEquals("a", completion.getCompletion("ac"));
		assertEquals("aa", completion.getCompletion("a"));
	}
	
	@Test
	public void getCompletion_ignoreCase() {
		Completion completion = new Completion();
		completion.setHistory(Arrays.asList(
			"ac",
			"Aa",
			"bb"
		));
		assertEquals("Aa", completion.getCompletion("a"));
		assertEquals("ac", completion.getCompletion("Aa"));
	}
	
	@Test
	public void getCompletion_historyWords() {
		Completion completion = new Completion();
		completion.setHistory(Arrays.asList(
			"ab ac",
			"bd be",
			"aab bbb"
		));
		assertEquals("aab", completion.getCompletion("a"));
		assertEquals("ac", completion.getCompletion("aab"));
		assertEquals("ab", completion.getCompletion("ac"));
		assertEquals("a", completion.getCompletion("ab"));
	}
	
	@Test
	public void getCompletion_completionWords() {
		Completion completion = new Completion();
		completion.setHistory(Arrays.asList(
			"ab ac",
			"bd be",
			"aab bbb"
		));
		assertEquals("dd aab", completion.getCompletion("dd a"));
		assertEquals("dd ac", completion.getCompletion("dd aab"));
		assertEquals("dd ab", completion.getCompletion("dd ac"));
		assertEquals("dd a", completion.getCompletion("dd ab"));
	}
}
