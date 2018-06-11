package fr.polytech.arar.cookietransfert;

import fr.berger.enhancedlist.lexicon.Lexicon;
import fr.berger.enhancedlist.lexicon.LexiconBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class Log {
	
	private static Lexicon<ILog> logEvents = new LexiconBuilder<ILog>(ILog.class)
			.setAcceptNullValues(false)
			.setAcceptDuplicates(false)
			.add(System.out::print)
			.createLexicon();
	
	public static void print(@Nullable String message) {
		for (ILog logEvent : logEvents) {
			if (logEvent != null)
				logEvent.log(message != null ? message : "(null)");
		}
	}
	
	public static void println(@Nullable String message) {
		print(message + "\n");
	}
	
	@SuppressWarnings("ConstantConditions")
	public static void register(@NotNull ILog runnable) {
		if (runnable != null)
			logEvents.add(runnable);
	}
	
	public interface ILog {
		
		void log(@NotNull String message);
	}
}
