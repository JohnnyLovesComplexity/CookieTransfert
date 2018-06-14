package fr.polytech.arar.cookietransfert;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class SoundManager {
	
	public static final String BELL = "bell.wav";
	
	private static boolean soundActivated = true;
	
	public static void play(String resource) {
		if (isSoundActivated()) {
			try {
				AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(SoundManager.class.getResourceAsStream("/sounds/" + resource));
				Clip clip = AudioSystem.getClip();
				clip.open(audioInputStream);
				clip.start();
			} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
				e.printStackTrace();
			}
		}
	}
	
	/* GETTER & SETTER */
	
	public static boolean isSoundActivated() {
		return soundActivated;
	}
	
	public static void setSoundActivated(boolean soundActivated) {
		SoundManager.soundActivated = soundActivated;
	}
}
