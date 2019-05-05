package main.java;

import javax.sound.midi.*;

public class Piano implements Runnable {
    Note note;
    ShortMessage myMsg = new ShortMessage();
    Synthesizer synth = MidiSystem.getSynthesizer();

    public Piano(Note note) throws MidiUnavailableException {
        this.note = note;
    }

    @Override
    public void run() {
        String name = Thread.currentThread().getName();
        synchronized (note) {
            int noteNum = note.getNote();
            noteNum = 60;
            // Start playing the note Middle C (60),
            // moderately loud (velocity = 93).
            long timeStamp = -1;
            try {
                synth.open();
                Receiver rcvr = synth.getReceiver();
                myMsg.setMessage(ShortMessage.NOTE_ON, noteNum, 93);
                rcvr.send(myMsg, timeStamp);
                myMsg.setMessage(ShortMessage.NOTE_OFF, noteNum, 0);
                rcvr.send(myMsg, timeStamp);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}


