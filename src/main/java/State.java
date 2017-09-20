// State.java

import java.awt.Point;

/** An automaton or Turing Machine state. */
public class State {

   /** debugging flag */
   static final boolean DEBUG = true;

   /** count the number of states created thus far */
   private static int idCount = 0;

   /** this state's unique ID number */
   private int id;

   /** this state's name */
   private String name;

   /** whether this state is an accept state */
   private boolean accept;

   /** whether this state is a current state */
   private boolean current;

   /** whether this state is a selected state */
   private boolean selected;

   /** (x, y) location of this state */
   private Point position;

   /** extra metadata associated with this state */
   private Object meta;

   /** constructs a new State object with the given name;
       if accept is true, the state will be an accept state */
   public State(String name, boolean accept) {
      this(name, accept, null);
   }

   /** constructs a new State object with the given name, accept value,
       and (x, y) position */
   public State(String name, boolean accept, Point pos) {
      id = idCount++;
      this.name = name;
      this.accept = accept;
      current = false;
      selected = false;
      position = pos;
   }

   /** sets this state's name */
   public void setName(String name) { this.name = name; }

   /** sets whether this state is an accept state */
   public void setAccept(boolean accept) { this.accept = accept; }

   /** sets whether this state is a current state */
   public void setCurrent(boolean current) { this.current = current; }

   /** sets whether this state is a selected state */
   public void setSelected(boolean selected) { this.selected = selected; }

   /** sets this state's (x, y) position */
   public void setPosition(Point pos) { position = pos; }

   /** sets this state's associated metadata */
   public void setMetadata(Object o) { meta = o; }

   /** returns this state's name */
   public String getName() { return name; }

   /** returns whether this state is an accept state */
   public boolean isAccept() { return accept; }

   /** returns whether this state is a current state */
   public boolean isCurrent() { return current; }

   /** returns whether this state is a selected state */
   public boolean isSelected() { return selected; }

   /** returns any associated metadata of this state */
   public Object getMetadata() { return meta; }

   /** returns this state's unique ID number */
   public int hashCode() { return id; }

   /** tests whether two States are equal */
   public boolean equals(State s) { return this == s; }

   /** get this state's (x, y) position */
   public Point getPosition() { return position; }

}

