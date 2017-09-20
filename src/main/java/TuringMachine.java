// TuringMachine.java

import java.io.*;
import java.util.*;

/** A Turing machine. */
public class TuringMachine extends FiniteStateMachine {

   // CONSTANTS

   /** the blank symbol for the tape */
   public static final char BLANK = EPSILON;


   // STATE VARIABLES

   /** the current state */
   private State current;

   /** the accept state */
   private State accept;

   /** the reject state */
   private State reject;

   /** the contents of the tape.  Characters beyond the end of the string
       are assumed to be the blank symbol. */
   private String tape;

   /** the current computation's position on the tape */
   private int pos;

   /** a buffer containing keystrokes typed */
   private String keyBuf = "";


   // CONSTRUCTORS

   /** constructs a Turing machine with the given set of states and
       start state */
   public TuringMachine(State[] states, State start,
      State accept, State reject)
   {
      this(states, start, accept, reject, new TransitionFunction(true));
   }

   /** constructs a Turing machine with the given set of states, start state,
       and transition function */
   public TuringMachine(State[] states, State start,
      State accept, State reject, TransitionFunction function)
   {
      super(states, start, function);
      this.accept = accept;
      this.reject = reject;
      for (int i=0; i<states.length; i++) {
         if (states[i].getMetadata() == null) {
            states[i].setMetadata(new Hashtable());
         }
      }
   }


   // MODIFIERS

   /** adds a new state to this Turing machine */
   public void addState(State state) {
      super.addState(state);
      if (state.getMetadata() == null) state.setMetadata(new Hashtable());
   }

   /** removes a state from this Turing machine */
   public void removeState(State state) {
      // do not remove TM's accept or reject states
      if (state == accept || state == reject) return;

      synchronized (states) {
         // sever all transition function connections before removing state
         for (int i=0; i<numStates; i++) {
            State s = (State) states.elementAt(i);
            for (char so=33; so<127; so++) {
               for (char de=33; de<127; de++) {
                  TransitionTuple from = new TransitionTuple(s, so);
                  TransitionTuple toL = new TransitionTuple(state, de, false);
                  TransitionTuple toR = new TransitionTuple(state, de, true);
                  function.removeTransition(from, toL);
                  function.removeTransition(from, toR);
               }
            }
         }
      }
      super.removeState(state);
   }


   // REQUIRED MODIFIERS

   /** starts a new computation with the specified word */
   public void startComputation(String word) {
      synchronized (states) {
         this.word = word;
         step = 0;
         answer = null;
         current = start;
         tape = word;
         pos = 0;
         synchAndCheck();
      }
   }

   /** advances the current computation one step at a time */
   public void step() {
      synchronized (states) {
         if (answer != null) {
            if (State.DEBUG) {
               System.err.println("TuringMachine.step: attempting to step " +
                  "through this automaton when computation is already done");
            }
            return;
         }
         if (word == null) {
            if (State.DEBUG) {
               System.err.println("TuringMachine.step: attempting to step " +
                  "through this automaton with no computation specified");
            }
            return;
         }

         // get the transition from the current state
         if (pos == tape.length()) tape = tape + BLANK;
         Vector next = function.getTransitions(
            new TransitionTuple(current, tape.charAt(pos)));
         if (next != null && next.size() > 0) {
            // there is a valid transition
            TransitionTuple t = (TransitionTuple) next.elementAt(0);
            State ns = t.getState();
            char symbol = t.getSymbol();
            boolean dir = t.getDirection();
            current = ns;
            pos += (dir ? 1 : -1);
            if (pos < 0) pos = 0;
            tape = tape.substring(0, pos) + symbol + tape.substring(pos + 1);
         }
         else {
            // computation is dead
            answer = new Boolean(false);
         }

         // increment number of steps taken
         step++;

         // make sure states' current bits are up-to-date
         synchAndCheck();
      }
   }

   /** called whenever a key is pressed and a transition destination state
       exists. Needed so that each subclass can use the keystrokes to set up
       transitions according to its own model */
   protected void transitionKeyPressed(State source, State dest, char key) {
      Hashtable meta = (Hashtable) source.getMetadata();
      String[] list = (String[]) meta.get(dest);
      if (list == null) list = new String[0];
      int listLen = list.length;
      if (key == 8) {
         if (keyBuf.length() > 0) {
            // clear the key buffer
            keyBuf = "";
         }
         else {
            // backspace a transition off the list
            if (listLen > 0) {
               String last = list[listLen - 1];
               char sourceLet = last.charAt(0);
               if (sourceLet == EPSILON_CHAR) {
                  // character is actually an epsilon
                  sourceLet = EPSILON;
               }
               char destLet = last.charAt(3);
               if (destLet == EPSILON_CHAR) {
                  // character is actually an epsilon
                  destLet = EPSILON;
               }
               boolean dir = (last.charAt(5) == 'R');
               String[] newList = new String[listLen - 1];
               System.arraycopy(list, 0, newList, 0, listLen - 1);
               function.removeTransition(
                  new TransitionTuple(source, sourceLet),
                  new TransitionTuple(dest, destLet, dir));
               meta.put(dest, newList);
            }
         }
      }
      else {
         // ignore out-of-range characters
         if (key < 32 || key > 126) return;

         // 
         if (keyBuf.length() == 2) {
            if (key == 'l') key = 'L';
            if (key == 'r') key = 'R';
            if (key != 'L' && key != 'R') return;
         }
         if (key == 32) key = EPSILON_CHAR;
         keyBuf = keyBuf + key;

         if (keyBuf.length() == 3) {
            // three keys have been pressed
            String[] newList = new String[listLen + 1];
            System.arraycopy(list, 0, newList, 0, listLen);
            char sourceLet = keyBuf.charAt(0);
            char destLet = keyBuf.charAt(1);
            char dirLet = keyBuf.charAt(2);
            boolean dir = (dirLet == 'R');
            keyBuf = "";
            newList[listLen] = sourceLet + "->" + destLet + "," + dirLet;

            // make sure this transition isn't already present
            for (int i=0; i<list.length; i++) {
               char so = list[i].charAt(0);
               char de = list[i].charAt(3);
               char di = list[i].charAt(5);
               if (sourceLet == so) {
                  /* CTR DO THIS: need to find the old transition on the meta list and remove it, so that it doesn't show up... also, should add a little GUI box that shows keystrokes typed... keystroke typing needs some clean-up... like if you click anywhere, it should forget the keys you typed... need to add the tape display to the GUI also... and I'm sure other things are wrong/missing as well */
                  // transition already on the list; remove old transition
                  function.removeTransition(new TransitionTuple(source, so),
                     new TransitionTuple(dest, de, di == 'R'));
               }
            }

            // convert epsilon character to epsilon symbol
            if (sourceLet == EPSILON_CHAR) sourceLet = EPSILON;
            if (destLet == EPSILON_CHAR) destLet = EPSILON;

            // add the transition
            function.addTransition(new TransitionTuple(source, sourceLet),
               new TransitionTuple(dest, destLet, dir));
            meta.put(dest, newList);
         }
      }
   }


   // ACCESSORS

   /** returns the TM's tape at the current position in the computation */
   public String getTape() { return tape; }
   
   /** returns the current tape position in the computation */
   public int getPosition() { return pos; }


   // REQUIRED ACCESSORS

   /** returns a set of strings representing the transitions from the given
       source state to the given destination state */
   protected String[] getTransitionStrings(State source, State dest) {
      Hashtable meta = (Hashtable) source.getMetadata();
      String[] s = (String[]) meta.get(dest);
      return s;
   }


   // HELPER METHODS

   /** makes sure each state knows whether or not it's a current state,
       then checks whether the computation is finished */
   private void synchAndCheck() {
      for (int i=0; i<numStates; i++) {
         State s = (State) states.elementAt(i);
         s.setCurrent(s == current);
      }
      if (current == accept) answer = new Boolean(true);
      if (current == reject) answer = new Boolean(false);

      // redraw state diagram
      repaint();
   }

}

