package fr.polytech.arar.cookietransfert;

import fr.berger.enhancedlist.lexicon.Lexicon;
import fr.berger.enhancedlist.lexicon.LexiconBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class Log {
	
	private static Lexicon<Function<String, Void>> logEvents = new LexiconBuilder<Function<String, Void>>()
			.setAcceptNullValues(false)
			.setAcceptDuplicates(true)
			.createLexicon();
	
	public static void print(@Nullable String message) {
		System.out.print(message);
		for (Function<String, Void> logEvent : logEvents) {
			if (logEvent != null)
				logEvent.apply(message);
		}
	}
	
	public static void println(@Nullable String message) {
		print(message + "\n");
	}
	
	public static void register(@NotNull Function<String, Void> runnable) {
		logEvents.add(runnable);
	}
}
