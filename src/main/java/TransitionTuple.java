// TransitionTuple.java

/** Tuples of various forms for use with the TransitionFunction Hashtable. */
public class TransitionTuple {

   // STATE VARIABLES

   /** state */
   private State state;

   /** alphabet letter */
   private char letter;

   /** tape symbol */
   private char symbol;

   /** direction */
   private Boolean direction;

   /** ID number */
   private int id;


   // CONSTRUCTORS

   /** constructs a (state) tuple */
   public TransitionTuple(State state) {
      this(state, '\0', '\0', null);
   }

   /** constructs a (state, letter) tuple */
   public TransitionTuple(State state, char letter) {
      this(state, letter, '\0', null);
   }

   /** constructs a (state, letter, symbol) tuple */
   public TransitionTuple(State state, char letter, char symbol) {
      this(state, letter, symbol, null);
   }

   /** constructs a (state, symbol, direction) tuple */
   public TransitionTuple(State state, char symbol, boolean direction) {
      this(state, '\0', symbol, new Boolean(direction));
   }

   /** main constructor */
   private TransitionTuple(State state, char letter, char symbol,
      Boolean direction)
   {
      this.state = state;
      this.letter = letter;
      this.symbol = symbol;
      this.direction = direction;
      id = 256 * 256 * 3 * state.hashCode() +
         256 * 3 * (int) letter +
         3 * (int) symbol +
         (direction == null ? 2 : (direction.booleanValue() ? 1 : 0));
   }


   // ACCESSORS

   /** returns the tuple's state element */
   public State getState() { return state; }

   /** returns the tuple's alphabet letter element */
   public char getLetter() { return letter; }

   /** returns the tuple's tape symbol element */
   public char getSymbol() { return symbol; }

   /** returns the tuple's direction element */
   public boolean getDirection() {
      return direction != null && direction.booleanValue();
   }


   // HASHTABLE-RELATED METHODS

   /** returns an ID number for this TransitionTuple */
   public int hashCode() { return id; }

   /** tests whether two TransitionTuples are equal */
   public boolean equals(Object o) {
      if (!(o instanceof TransitionTuple)) return false;
      TransitionTuple tuple = (TransitionTuple) o;
      return (id == tuple.id);
   }

}

