package com.runescape.sound;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Port;

public class SoundUtil {
	public static boolean findMixer() {
		try
        {
			Mixer.Info[] mixers = AudioSystem.getMixerInfo();
			if(mixers.length <= 0) {
				return false;
			}
		    for (Mixer.Info info : mixers) 
		    {
		    	Mixer mixer = AudioSystem.getMixer(info);
				if (mixer.isLineSupported(Port.Info.SPEAKER) || mixer.isLineSupported(Port.Info.HEADPHONE)) {
					return true;
				}
		    }
        }
	    catch (Exception e)
        {
            e.printStackTrace();
        } 
	    return false;
	}
}
