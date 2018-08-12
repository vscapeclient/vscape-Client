package com.runescape.map.floor;

import com.runescape.cache.Archive;

import java.nio.ByteBuffer;

public class FloOverlay {

	public static FloOverlay[] cache;

    public int texture;
    public int rgb;
    public boolean occlude;
    public int anotherRgb;
    public int int_9;
    public boolean boolean_10;
    public int int_11;
    public boolean boolean_12;
    public int int_13;
    public int int_14;
    public int int_15;
    public int int_16;

    public int hue;
    public int saturation;
    public int lumiance;
    
    public int secondaryHue;
    public int secondarySaturation;
    public int secondaryLuminance;
    
    public int blendHue;
    public int blendHueMultiplier;
    public int hslToRgb;
    
    private FloOverlay() {
    	texture = -1;
    	occlude = true;
    }

    public static void unpackConfig(Archive archive) {
        ByteBuffer buffer = ByteBuffer.wrap(archive.getFile("flo2.dat"));
        int count = buffer.getShort();
        cache = new FloOverlay[count];
        for (int i = 0; i < count; i++) {
            if (cache[i] == null) {
            	cache[i] = new FloOverlay();
            }
            cache[i].readValues(buffer);
            cache[i].generateHsl();
        }
    }
    
    public void generateHsl() {
        if (anotherRgb != -1) {
            rgbToHsl(anotherRgb);
            secondaryHue = hue;
            secondarySaturation = saturation;
            secondaryLuminance = lumiance;
        }
        rgbToHsl(rgb);
    }

    private void readValues(ByteBuffer buffer) {
        for (;;) {
            int opcode = buffer.get();
            if (opcode == 0) {
                break;
            } else if (opcode == 1) {
                rgb = ((buffer.get() & 0xff) << 16) + ((buffer.get() & 0xff) << 8) + (buffer.get() & 0xff);
            } else if (opcode == 2) {
                texture = buffer.get() & 0xff;
            } else if (opcode == 3) {
                texture = buffer.getShort() & 0xffff;
                if (texture == 65535) {
                    texture = -1;
                }
            } else if (opcode == 4) {
            	/* empty */
            } else if (opcode == 5) {
                occlude = false;
            } else if (opcode == 6) {
            	/* empty */
            } else if (opcode == 7) {
                anotherRgb = ((buffer.get() & 0xff) << 16) + ((buffer.get() & 0xff) << 8) + (buffer.get() & 0xff);
            } else if (opcode == 8) {
            	/* empty */
            } else if (opcode == 9) {
                int_9 = buffer.getShort() & 0xffff;
            } else if (opcode == 10) {
                boolean_10 = false;
            } else if (opcode == 11) {
                int_11 = buffer.get() & 0xff;
            } else if (opcode == 12) {
                boolean_12 = true;
            } else if (opcode == 13) {
                int_13 = ((buffer.get() & 0xff) << 16) + ((buffer.get() & 0xff) << 8) + (buffer.get() & 0xff);
            } else if (opcode == 14) {
                int_14 = buffer.get() & 0xff;
            } else if (opcode == 15) {
                int_15 = buffer.getShort() & 0xffff;
                if (int_15 == 65535) {
                    int_15 = -1;
                }
            } else if (opcode == 16) {
                int_16 = buffer.get() & 0xff;
            } else {
            	System.out.println("Error unrecognised config code: " + opcode);
            }
        }
    }

    private void rgbToHsl(int rgb) {
        double r = (rgb >> 16 & 0xff) / 256.0;
        double g = (rgb >> 8 & 0xff) / 256.0;
        double b = (rgb & 0xff) / 256.0;
        double min = r;
        if (g < min) {
            min = g;
        }
        if (b < min) {
            min = b;
        }
        double max = r;
        if (g > max) {
            max = g;
        }
        if (b > max) {
            max = b;
        }
        double h = 0.0;
        double s = 0.0;
        double l = (min + max) / 2.0;
        if (min != max) {
            if (l < 0.5) {
                s = (max - min) / (max + min);
            }
            if (l >= 0.5) {
                s = (max - min) / (2.0 - max - min);
            }
            if (r == max) {
                h = (g - b) / (max - min);
            } else if (g == max) {
                h = 2.0 + (b - r) / (max - min);
            } else if (b == max) {
                h = 4.0 + (r - g) / (max - min);
            }
        }
        h /= 6.0;
        hue = (int) (h * 256.0);
        saturation = (int) (s * 256.0);
        lumiance = (int) (l * 256.0);
        if (saturation < 0) {
            saturation = 0;
        } else if (saturation > 255) {
            saturation = 255;
        }
        if (lumiance < 0) {
            lumiance = 0;
        } else if (lumiance > 255) {
            lumiance = 255;
        }
        if (l > 0.5) {
            blendHueMultiplier = (int) ((1.0 - l) * s * 512.0);
        } else {
            blendHueMultiplier = (int) (l * s * 512.0);
        }
        if (blendHueMultiplier < 1) {
            blendHueMultiplier = 1;
        }
        blendHue = (int) (h * blendHueMultiplier);
        hslToRgb = hslToRgb(hue, saturation, lumiance);
    }

    public final static int hslToRgb(int h, int s, int l) {
        if (l > 179) {
            s /= 2;
        }
        if (l > 192) {
            s /= 2;
        }
        if (l > 217) {
            s /= 2;
        }
        if (l > 243) {
            s /= 2;
        }
        int rgb = (h / 4 << 10) + (s / 32 << 7) + l / 2;
        return rgb;
    }
}