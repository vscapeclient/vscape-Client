package com.runescape.sound;

import javax.sound.sampled.*;
import java.io.InputStream;

public class SoundPlayer implements Runnable {

	private AudioInputStream stream;
	private DataLine.Info info;
	private Clip sound;

	private InputStream soundStream;
	private Thread player;
	private int delay;
	private int soundLevel;
	public static int volume;

	/**
	 * Initializes the sound player.
	 * @param stream
	 * @param delay
	 */
	public SoundPlayer(InputStream stream, int delay) {
		int level = 9;
		if (level == 0 || volume == 4 || level - volume <= 0) {
			return;
		}
		this.soundStream = stream;
		this.soundLevel = level;
		this.delay = delay;
		player = new Thread(this);
		player.start();
	}

	/**
	 * Plays the sound.
	 */
	@Override
	public void run() {
		try {
			stream = AudioSystem.getAudioInputStream(soundStream);
			info = new DataLine.Info(Clip.class, stream.getFormat());
			sound = (Clip) AudioSystem.getLine(info);
			sound.open(stream);
			FloatControl volume;
			if (sound.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
				volume = (FloatControl) sound.getControl(FloatControl.Type.MASTER_GAIN);
				volume.setValue(getDecibels(soundLevel - getVolume()));
			} else if (sound.isControlSupported(FloatControl.Type.VOLUME)) {
				volume = (FloatControl) sound.getControl(FloatControl.Type.VOLUME);
				volume.setValue(((soundLevel - getVolume()) / 10f) * volume.getMaximum());
			}

			if (delay > 0) {
				Thread.sleep(delay);
			}
			sound.start();
			while (sound.isRunning()) {
				Thread.sleep(250);
			}
			Thread.sleep(10000);
			sound.close();
			stream.close();
			player.interrupt();
		} catch (Exception e) {
			player.interrupt();
			e.printStackTrace();
		}
	}

	/**
	 * Sets the client's volume level.
	 * @param level
	 */
	public static void setVolume(int level) {
		volume = level;
	}

	/**
	 * Returns the client's volume level.
	 */
	public static int getVolume() {
		return volume;
	}

	/**
	 * Returns the decibels for a given volume level.
	 * @param level
	 * @return
	 */
	public float getDecibels(int level) {
		switch (level) {
			case 1:
				return (float) -80.0;
			case 2:
				return (float) -70.0;
			case 3:
				return (float) -60.0;
			case 4:
				return (float) -50.0;
			case 5:
				return (float) -40.0;
			case 6:
				return (float) -30.0;
			case 7:
				return (float) -20.0;
			case 8:
				return (float) -10.0;
			case 9:
				return (float) -0.0;
			case 10:
				return (float) 6.0;
			default:
				return (float) 0.0;
		}
	}
}