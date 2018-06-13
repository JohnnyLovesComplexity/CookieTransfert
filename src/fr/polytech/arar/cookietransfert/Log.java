package fr.polytech.arar.cookietransfert;

import fr.berger.enhancedlist.lexicon.Lexicon;
import fr.berger.enhancedlist.lexicon.LexiconBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Log {
	
	private static Lexicon<ILog> logEvents = null;
	
	public static void print(@Nullable String message) {
		configureLogEvents();
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
		configureLogEvents();
		if (runnable != null)
			logEvents.add(runnable);
	}
	
	private static void configureLogEvents() {
		if (logEvents == null) {
			logEvents = new Lexicon<>(ILog.class);
			logEvents.setAcceptDuplicates(false);
			logEvents.setAcceptNullValues(false);
			logEvents.add(System.out::print);
		}
	}
	
	public interface ILog {
		
		void log(@NotNull String message);
	}
}
