// PushdownAutomaton.java

import java.io.*;
import java.util.*;

/** A non-deterministic pushdown automaton. */
public class PushdownAutomaton extends FiniteStateMachine {

   // STATE VARIABLES

   /** boolean array marking whether each state is a current state */
   private boolean[] b;


   // CONSTRUCTORS

   /** constructs a pushdown automaton with the given set of states and
       start state */
   public PushdownAutomaton(State[] states, State start) {
      this(states, start, new TransitionFunction(false));
   }

   /** constructs a pushdown automaton with the given set of states, start state,
       and transition function */
   public PushdownAutomaton(State[] states, State start,
      TransitionFunction function)
   {
      super(states, start, function);
      for (int i=0; i<states.length; i++) {
         if (states[i].getMetadata() == null) {
            states[i].setMetadata(new Hashtable());
         }
      }
      fixB();
   }


   // MODIFIERS

   /** adds a new state to this pushdown automaton */
   public void addState(State state) {
      super.addState(state);
      if (state.getMetadata() == null) state.setMetadata(new Hashtable());
      fixB();
   }

   /** removes a state from this pushdown automaton */
   public void removeState(State state) {
      synchronized (states) {
         // sever all transition function connections before removing state
         for (int i=0; i<numStates; i++) {
            State s = (State) states.elementAt(i);
            for (char l=0; l<256; l++) {
               function.removeTransition(new TransitionTuple(s, l),
                  new TransitionTuple(state));
            }
         }
      }
      super.removeState(state);
      fixB();
   }


   // REQUIRED MODIFIERS

   /** starts a new computation with the specified word */
   public void startComputation(String word) {
      synchronized (states) {
         this.word = word;
         b = new boolean[numStates];
         for (int j=0; j<numStates; j++) {
            State s = (State) states.elementAt(j);
            b[j] = (s == start);
         }
         step = 0;
         makeEpsilonTransitions(b);
         answer = null;
         synchAndCheck();
      }
   }

   /** advances the current computation one step at a time */
   public void step() {
      synchronized (states) {
         if (answer != null) {
            if (State.DEBUG) {
               System.err.println("PushdownAutomaton.step: attempting to step " +
                  "through this automaton when computation is already done");
            }
            return;
         }
         if (word == null) {
            if (State.DEBUG) {
               System.err.println("PushdownAutomaton.step: attempting to step " +
                  "through this automaton with no computation specified");
            }
            return;
         }

         char l = word.charAt(step);
         boolean[] nextB = new boolean[numStates];
         for (int j=0; j<numStates; j++) nextB[j] = false;

         // loop through each current state
         for (int j=0; j<numStates; j++) {
            if (b[j]) {
               State s = (State) states.elementAt(j);
               Vector next = function.getTransitions(
                  new TransitionTuple(s, l));
               if (next != null) {
                  // mark all resultant states as next states
                  for (int k=0; k<next.size(); k++) {
                     TransitionTuple t = (TransitionTuple) next.elementAt(k);
                     State ns = t.getState();
                     nextB[states.indexOf(ns)] = true;
                  }
               }
            }
         }

         // make epsilon transitions from the new states
         makeEpsilonTransitions(nextB);

         // next states become current states
         b = nextB;

         // check whether all non-deterministic paths have rejected
         boolean dead = true;
         for (int i=0; i<numStates; i++) {
            if (b[i]) dead = false;
         }
         if (dead) {
            answer = new Boolean(false);
            synchAndCheck();
            return;
         }

         // increment position
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
      String list = (String) meta.get(dest);
      if (list == null) list = "";
      if (key == 8) {
         // backspace a transition off the list
         int listLen = list.length();
         if (listLen > 0) {
            char l = list.charAt(listLen - 1);
            if (l == EPSILON_CHAR) {
               // transition is actually an epsilon transition
               l = EPSILON;
            }
            function.removeTransition(new TransitionTuple(source, l),
               new TransitionTuple(dest));
            list = list.substring(0, listLen - 1);
            meta.put(dest, list);
         }
      }
      else {
         // add the typed character to the transition list
         if (key >= 32 && key < 256 && key != 127) {
            if (key == 32) {
               if (list.indexOf(EPSILON_CHAR) >= 0) return;
               function.addTransition(new TransitionTuple(source, EPSILON),
                  new TransitionTuple(dest));
               list = list + EPSILON_CHAR;
            }
            else {
               if (list.indexOf(key) >= 0) return;
               function.addTransition(new TransitionTuple(source, key),
                  new TransitionTuple(dest));
               list = list + key;
            }
            meta.put(dest, list);
         }
      }
   }


   // REQUIRED ACCESSORS

   /** returns a set of strings representing the transitions from the given
       source state to the given destination state */
   protected String[] getTransitionStrings(State source, State dest) {
      Hashtable meta = (Hashtable) source.getMetadata();
      String s = (String) meta.get(dest);
      if (s == null || s.equals("")) return null;
      else return new String[] {s};
   }


   // HELPER METHODS

   /** adjusts the size of the current state array if
       the number of states has changed */
   private void fixB() {
      synchronized (states) {
         boolean[] newB = new boolean[numStates];
         for (int i=0; i<numStates; i++) {
            State s = (State) states.elementAt(i);
            newB[i] = s.isCurrent();
         }
         b = newB;
      }
   }

   /** makes sure each state knows whether or not it's a current state,
       then checks whether the computation is finished */
   private void synchAndCheck() {
      for (int i=0; i<numStates; i++) {
         State s = (State) states.elementAt(i);
         s.setCurrent(b[i]);
      }

      // check if computation is at the end of the word
      if (step == word.length()) {
         // if any current states are accept states, accept
         for (int i=0; i<numStates; i++) {
            if (b[i]) {
               State s = (State) states.elementAt(i);
               if (s.isAccept()) {
                  answer = new Boolean(true);
                  repaint();
                  return;
               }
            }
         }

         // otherwise, reject
         answer = new Boolean(false);
      }

      // redraw state diagram
      repaint();
   }

   /** modifies the current states to include any states accessible through
       epsilon transitions */
   private void makeEpsilonTransitions(boolean[] b) {
      boolean changed = true;
      // loop through current states until new states stop getting added
      while (changed) {
         changed = false;
         for (int i=0; i<numStates; i++) {
            if (b[i]) {
               State s = (State) states.elementAt(i);
               Vector eps = function.getTransitions(
                  new TransitionTuple(s, EPSILON));
               if (eps != null) {
                  // mark all resultant states as current states
                  for (int j=0; j<eps.size(); j++) {
                     TransitionTuple t = (TransitionTuple) eps.elementAt(j);
                     State ns = t.getState();
                     int q = states.indexOf(ns);
                     if (!b[q]) {
                        changed = true;
                        b[q] = true;
                     }
                  }
               }
            }
         }
      }
   }

}

