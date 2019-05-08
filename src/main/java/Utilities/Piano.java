package main.java;

import javax.sound.midi.*;

public class Piano implements Runnable {
    int note;
    private static MidiChannel[] channels;

    public Piano(int note) {
        this.note = note;
    }

    @Override
    public void run() {
        Synthesizer synth;
        try {
            synth = MidiSystem.getSynthesizer();
            synth.open();
            channels = synth.getChannels();
            play(note, 5000);
            synth.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void play(int note, int duration) throws InterruptedException {
        channels[0].noteOn(id(note), 80);
//        System.out.println(Thread.currentThread());
        Thread.sleep(duration);
        channels[0].noteOff(id(note));
    }

    private static int id(int note) {
        int octave = 6;
        return note + 12 * octave + 12;
    }
}


