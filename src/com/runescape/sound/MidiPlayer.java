package com.runescape.sound;

import javax.sound.midi.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
 
public final class MidiPlayer implements Receiver
{

    public MidiPlayer() throws Exception
    {
        resetChannels();
        Receiver tempreceiver = null;
        Sequencer tempsequencer = null;
        try{
        	tempreceiver = MidiSystem.getReceiver();   //Old midi code kept as to not create some new unforeseen problems
        	tempsequencer = MidiSystem.getSequencer(false);
        	tempsequencer.getTransmitter().setReceiver(this);
        	tempsequencer.open();
        }
        catch (Exception e) {
        	try{
        		//Windows 10 compatibility code
        		//Chooses a device by name, or defaults to the first device
        		//There is some kind of Windows registry error, but it may just be a preview build issue.
        		//This code may not be needed when Windows 10 releases, who knows
        		System.out.println(e);
        		System.out.println("Trying Windows 10 Midi device compatability");
        		int devicechosen = -1;
        		MidiDevice.Info[] deviceinfo=MidiSystem.getMidiDeviceInfo();
                for (int x=0;x<deviceinfo.length;x++){
                    System.out.println("Midi device: "+deviceinfo[x].getName());
    	            if(deviceinfo[x].getName().equals("Microsoft MIDI Mapper") || deviceinfo[x].getName().equals("Real Time Sequencer")){
    	            	devicechosen = x;
    	            	break;
    	            }
                }
                if(devicechosen >= 0) {
	                System.out.println("Selecting Midi device " + deviceinfo[devicechosen]);
	                MidiSystem.getMidiDevice(deviceinfo[devicechosen]);
	                MidiDevice MidiOutDevice = MidiSystem.getMidiDevice(deviceinfo[devicechosen]);
	                tempreceiver = MidiOutDevice.getReceiver();
	                tempsequencer = MidiSystem.getSequencer(); 
	                tempsequencer.getTransmitter().setReceiver(tempreceiver); 
	                tempsequencer.getTransmitter().setReceiver(this);
	                tempsequencer.open();
                }
        	}catch (Exception e2) {
        		System.out.println(e2);
        		System.out.println("Something went really wrong, bug a dev for better sound support :^)");
        	}
        }
    	receiver = tempreceiver;
    	sequencer = tempsequencer;
        setTick(-1L);
    }
 
    public synchronized void setVolume(int velocity, int volume)
    {
        setVolume(velocity, volume, -1L);
    }
    
    public synchronized void setLooping(boolean loop) {
    	if(sequencer != null) {
    		sequencer.setLoopCount(loop ? Sequencer.LOOP_CONTINUOUSLY : 0);
    	}
	}
 
    public void play(Sequence sequence, boolean loop, int volume)
    {
        try
        {
        	if(sequencer != null) {
	            sequencer.setSequence(sequence);
	            sequencer.setLoopCount(loop ? Sequencer.LOOP_CONTINUOUSLY : 0);
	            setVolume(0, volume, -1L);
	            sequencer.start();
        	}
        }
        catch (InvalidMidiDataException ex) { }
    }
	
	public void play(byte[] data, boolean loop, int volume) {
		try {
			if(sequencer != null) {
				Sequence sequence = MidiSystem.getSequence(new ByteArrayInputStream(data));
				sequencer.setSequence(sequence);
				sequencer.setLoopCount(loop ? Sequencer.LOOP_CONTINUOUSLY : 0);
				setVolume(0, volume, -1L);
				sequencer.start();
			}
		} catch (InvalidMidiDataException ex) {
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean playing()
    {
        if(sequencer == null)
            return false;
        return sequencer.isRunning();
    }
 
    public void stop()
    {
    	if(sequencer != null) {
    		sequencer.stop();
    	}
        setTick(-1L);
    }
 
    public void resetVolume(int volume)
    {
        resetVolume(volume, -1L);
    }
 
    private void setTick(long tick)
    {
        for (int i = 0; i != 16; ++i)
            sendMessage(i + 176, 123, 0, tick);
 
        for (int i = 0; i != 16; ++i)
            sendMessage(i + 176, 120, 0, tick);
 
        for (int i = 0; i != 16; ++i)
            sendMessage(i + 176, 121, 0, tick);
 
        for (int i = 0; i != 16; ++i)
            sendMessage(i + 176, 0, 0, tick);
 
        for (int i = 0; i != 16; ++i)
            sendMessage(i + 176, 32, 0, tick);
 
        for (int i = 0; i != 16; ++i)
            sendMessage(i + 192, 0, 0, tick);
 
    }
 
    private void sendMessage(int status, int data1, int data2, long tick)
    {
        try
        {
        	if(receiver != null) {
	            ShortMessage msg = new ShortMessage();
	            msg.setMessage(status, data1, data2);
	            receiver.send(msg, tick);
        	}
        }
        catch (InvalidMidiDataException ex) { }
    }
 
    public void closeImpl()
    {
    	if(sequencer != null)
    		sequencer.close();
        if(receiver != null)
        	receiver.close();
    }
 
    private void resetVolume(int volume, long tick)
    {
        this.volume = volume;
        resetChannels();
        setVolume(tick);
    }
 
    private void setVolume(long tick)
    {
        for (int i = 0; i != 16; ++i)
        {
            int volume = getVolume(i);
            sendMessage(i + 176, 7, volume >>> 7, tick);
            sendMessage(i + 176, 39, volume & 0x7f, tick);
        }
 
    }
 
    private void setVolume(int velocity, int volume, long tick)
    {
        volume = (int) ((double) volume * Math.pow(0.1D, (double) velocity * 0.0005D) + 0.5D);
        if (this.volume == volume)
            return;
 
        this.volume = volume;
        setVolume(tick);
    }
 
    private int getVolume(int channel)
    {
        channel = channels[channel];
        return (int) (Math.sqrt((double) (channel = ((channel * volume) >>> 8) * channel)) + 0.5D);
    }
 
    private void resetChannels()
    {
        for (int i = 0; i != 16; ++i)
            channels[i] = 12800;
 
    }
 
    private boolean check(int status, int data1, int data2, long tick)
    {
        if ((status & 0xf0) == 176)
        {
            if (data1 == 121)
            {
                sendMessage(status, data1, data2, tick);
                int channel = status & 0xf;
                channels[channel] = 12800;
                int volume = getVolume(channel);
                sendMessage(status, 7, volume >>> 7, tick);
                sendMessage(status, 39, volume & 0x7f, tick);
                return true;
            }
            if (data1 == 7 || data1 == 39)
            {
                int channel = status & 0xf;
                if (data1 == 7)
                    channels[channel] = (channels[channel] & 0x7f) | (data2 << 7);
                else
                    channels[channel] = (channels[channel] & 0x3f80) | data2;
 
                int volume = getVolume(channel);
                sendMessage(status, 7, volume >>> 7, tick);
                sendMessage(status, 39, volume & 0x7f, tick);
                return true;
            }
        }
        return false;
    }
 
    public synchronized void send(MidiMessage msg, long tick)
    {
    	if(receiver != null) { 
	        byte[] data = msg.getMessage();
	        if (data.length < 3 || !check(data[0], data[1], data[2], tick))
	            receiver.send(msg, tick);
    	}
    }
 
    public void close()
    {
    }
 
    protected void finalize() throws Throwable
    {
        try
        {
            closeImpl();
        }
        finally
        {
            super.finalize();
        }
    }
 
    private int volume;
    private final int[] channels = new int[16];
    private final Receiver receiver;
    private final Sequencer sequencer;
}