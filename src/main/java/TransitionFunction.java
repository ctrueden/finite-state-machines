// TransitionFunction.java

import java.util.*;

/** A finite state machine transition function, providing a mapping
    from one set of tuples to another set of tuples. */
public class TransitionFunction {

   /* Notes:
   1) NFA function maps (state, letter) -> state
   2) PDA function maps (state, letter, symbol) -> (state, symbol)
   3) TM function maps (state, symbol) -> (state, symbol, direction)

   NFAs and PDAs are non-deterministic, meaning that a single
   (state, letter) or (state, letter, symbol) tuple can map to multiple
   destination (state) or (state, symbol) tuples, respectively.

   TMs are deterministic, meaning that for any given tuple of the domain set,
   there will be at most one corresponding tuple in the destination set.
   */

   // STATE VARIABLES

   /** array of transitions */
   private Hashtable transitions = new Hashtable();

   /** whether this transition function is deterministic */
   private boolean deterministic;


   // CONSTRUCTOR

   /** construct a deterministic or non-deterministic transition function */
   public TransitionFunction(boolean deterministic) {
      this.deterministic = deterministic;
   }


   // MODIFIERS

   /** adds a transition from one tuple to another tuple */
   public void addTransition(TransitionTuple fromTuple,
      TransitionTuple toTuple)
   {
      // get the Vector of transitions from the Hashtable
      Vector v = (Vector) transitions.get(fromTuple);
      if (v == null) {
         // Vector does not exist yet; create it and add to the Hashtable
         v = new Vector();
         transitions.put(fromTuple, v);
      }

      // add the new transition to the transition Vector
      if (deterministic) v.removeAllElements();
      v.addElement(toTuple);
   }

   /** removes the transition from one tuple to another tuple */
   public void removeTransition(TransitionTuple fromTuple,
      TransitionTuple toTuple)
   {
      // get the Vector of transitions from the Hashtable
      Vector v = (Vector) transitions.get(fromTuple);
      if (v == null) return;

      // remove the transition from the transition Vector
      if (v.contains(toTuple)) v.removeElement(toTuple);
   }


   // ACCESSORS

   /** returns a vector of the resulting tuple(s) of the given
       domain tuple, or null for no transitions */
   public Vector getTransitions(TransitionTuple fromTuple) {
      // get the Vector of transitions from the Hashtable
      return (Vector) transitions.get(fromTuple);
   }

   /** returns the first resulting tuple of the given domain tuple,
       or null for no transitions */
   public TransitionTuple getTransition(TransitionTuple fromTuple) {
      Vector v = (Vector) transitions.get(fromTuple);
      if (v == null || v.size() < 1) return null;
      return (TransitionTuple) v.elementAt(0);
   }

   /** tests whether a given transition is present in the function */
   public boolean hasTransition(TransitionTuple fromTuple,
      TransitionTuple toTuple)
   {
      // get the Vector of transitions from the Hashtable
      Vector v = getTransitions(fromTuple);
      return v != null && v.contains(toTuple);
   }

}

