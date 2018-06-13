package fr.polytech.arar.cookietransfert;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class SoundManager {
	
	public static final String BELL = "bell.wav";
	
	public static void play(String resource) {
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
