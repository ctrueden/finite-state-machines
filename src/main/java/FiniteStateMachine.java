// FiniteStateMachine.java

import java.awt.*;
import java.awt.event.*;
import java.util.*;

/** A graphical component representing a state diagram of a
    finite automaton, pushdown automaton, or Turing Machine. */
public abstract class FiniteStateMachine extends Component
   implements KeyListener, MouseListener, MouseMotionListener
{

   // CONSTANTS

   /** graphical size of a state, in pixels */
   public static final int STATE_SIZE = 40;

   /** graphical pixel offset used in certain places */
   public static final int OFFSET = STATE_SIZE / 2;

   /** graphical size of the triangle marking the start state, in pixels */
   public static final int TRI_SIZE = 8;

   /** graphical size of the canvas, in pixels */
   public static final int CANVAS_SIZE = 1000;

   /** character representing an epsilon transition */
   public static final char EPSILON = 1;

   /** character used to display the epsilon symbol (or close approximation) */
   public static final char EPSILON_CHAR = 163;

   /** assumed height of transition text */
   public static final int TEXT_HEIGHT = 15;


   // STATE VARIABLES (no pun intended)

   /** Vector of states in this finite state machine */
   protected Vector states;

   /** Number of states in this finite state machine */
   protected int numStates;

   /** start state for this finite state machine */
   protected State start;

   /** transition function */
   protected TransitionFunction function;

   /** word for the current computation */
   protected String word;

   /** current position in the computation */
   protected int step;

   /** the solution to the current computation,
       or null if it is not yet finished */
   protected Boolean answer;

   /** current transition destination state */
   private State trans;

   /** currently selected state */
   private State sel;


   // STATIC GRAPHICS UTILITY METHODS

   /** computes the offset vector p3 (from p1) of length r that
       lies on the line from p1 to p2 */
   private static Point getPointOnLine(Point p1, Point p2, int r) {
      Point p3 = new Point();
      int t = p2.x - p1.x;
      if (t == 0) {
         p3.x = 0;
         p3.y = r;
      }
      else {
         double slope = (double) (p2.y - p1.y) / (p2.x - p1.x);
         r *= r;
         double div = slope * slope + 1;
         double d = r / div;
         p3.x = (int) Math.sqrt(d);
         p3.y = (int) Math.sqrt(r - d);
      }
      if (p1.x > p2.x) p3.x = -p3.x;
      if (p1.y > p2.y) p3.y = -p3.y;
      return p3;
   }

   /** computes the offset vector p3 (from p1) of length r such that
       (p2 - p1) is perpendicular to p3 */
   private static Point getPerpendicular(Point p1, Point p2, int r) {
      Point p3 = new Point();
      int x21 = p2.x - p1.x;
      int y21 = p2.y - p1.y;
      double denom = Math.sqrt(x21 * x21 + y21 * y21);
      if (denom == 0) {
         p3.x = 0;
         p3.y = 0;
      }
      else {
         p3.x = (int) -(r * y21 / denom);
         p3.y = (int) (r * x21 / denom);
      }
      return p3;
   }

   /** draws an arrow head at the tip of the line (p2 - p1) using
       the given Graphics context */
   private static void drawArrow(Point p1, Point p2, Graphics g) {
      // compute arrow points
      Point p3 = getPointOnLine(p1, p2, 20);
      p3.x = p2.x - p3.x;
      p3.y = p2.y - p3.y;
      Point t = getPerpendicular(p3, p2, 5);
      Point t1 = new Point();
      Point t2 = new Point();
      t1.x = p3.x + t.x;
      t1.y = p3.y + t.y;
      t2.x = p3.x - t.x;
      t2.y = p3.y - t.y;

      // draw arrow head
      g.drawLine(p2.x, p2.y, t1.x, t1.y);
      g.drawLine(p2.x, p2.y, t2.x, t2.y);
   }

   /** draws the specified text at location (x, y)
       using the given Graphics context */
   private static void drawText(String[] text, int x, int y, Graphics g) {
      // draw text
      for (int i=0; i<text.length; i++) {
         g.drawString(text[i], x, y - TEXT_HEIGHT * i);
      }
   }


   // CONSTRUCTOR

   /** constructs a new finite state machine using the given
       vector of states, start state and transition function */
   public FiniteStateMachine(State[] states, State start,
      TransitionFunction function)
   {
      this.states = new Vector();
      for (int i=0; i<states.length; i++) this.states.addElement(states[i]);
      numStates = this.states.size();
      this.start = start;
      addKeyListener(this);
      addMouseListener(this);
      addMouseMotionListener(this);
      this.function = function;
   }


   // PAINTING ROUTINES

   /** draws the specified text at the halfway point between p1 and p2
       using the given Graphics context */
   private void drawText(String[] text, Point p1, Point p2, Graphics g) {
      // compute text location
      FontMetrics fm = getParent().getFontMetrics(g.getFont());
      int w = 0;
      for (int i=0; i<text.length; i++) {
         int wi = fm.stringWidth(text[i]);
         if (wi > w) w = wi;
      }
      int h = TEXT_HEIGHT * text.length;
      int p21x = p2.x - p1.x;
      int p21y = p2.y - p1.y;
      int halfway = (int) (Math.sqrt(p21x * p21x + p21y * p21y) / 2);
      Point p4 = getPointOnLine(p1, p2, halfway);
      p4.x = p2.x - p4.x;
      p4.y = p2.y - p4.y;
      Point tt = getPerpendicular(p4, p2, 5);
      tt.x += p4.x;
      tt.y += p4.y;
      if (p1.x < p2.x) tt.y += h;
      if (p1.y < p2.y) tt.x -= w;

      // draw text
      drawText(text, tt.x, tt.y, g);
   }

   /** draws the state diagram onscreen */
   public void paint(Graphics g) {
      // draw the finite automaton to the canvas
      FontMetrics fm = getParent().getFontMetrics(g.getFont());
      synchronized (states) {
         int len = states.size();
         for (int i=0; i<len; i++) {
            State s = (State) states.elementAt(i);
            // plot this state as a circle, with its name inside
            Point p = s.getPosition();
            boolean accept = s.isAccept();
            if (s.isCurrent()) {
               // draw this circle in blue if it is a current state
               g.setColor(Color.cyan);
               if (accept) {
                  // draw a filled, double-bordered circle
                  g.fillOval(p.x + 3, p.y + 3, STATE_SIZE - 6, STATE_SIZE - 6);
               }
               else g.fillOval(p.x, p.y, STATE_SIZE, STATE_SIZE);
               g.setColor(Color.black);
            }
            if (accept) {
               // draw a double-bordered circle if it is an accept state
               g.drawOval(p.x + 3, p.y + 3, STATE_SIZE - 6, STATE_SIZE - 6);
            }
            g.drawOval(p.x, p.y, STATE_SIZE, STATE_SIZE);
            String n = s.getName();
            int width = fm.stringWidth(n);
            int px = p.x + (STATE_SIZE - width) / 2;
            int py = p.y + STATE_SIZE / 2 + 4;
            g.drawString(n, px, py);
            if (s.isSelected()) {
               // draw boxes around this state if it is selected
               int px0 = p.x + 1;
               int py0 = p.y + 1;
               int px1 = p.x + STATE_SIZE - 3;
               int py1 = p.y + STATE_SIZE - 3;
               g.fillRect(px0, py0, 4, 4);
               g.fillRect(px1, py0, 4, 4);
               g.fillRect(px0, py1, 4, 4);
               g.fillRect(px1, py1, 4, 4);
            }

            // draw transition arrows
            for (int j=0; j<len; j++) {
               State s2 = (State) states.elementAt(j);
               // get transition list from s to s2
               String[] list = getTransitionStrings(s, s2);
               if (list == null) continue;
               if (s == s2) {
                  // transitions go from state to itself
                  Point p1 = new Point(p);
                  p1.x += STATE_SIZE;
                  p1.y += OFFSET;
                  int off = OFFSET / 2;
                  int off3 = 3 * off;
                  int tx = p1.x + off3 / 2 + 1;
                  int ty = p.y;

                  // draw transition loop
                  g.drawArc(p.x + off3, p.y - off,
                     OFFSET, off3, 270, 265);

                  // draw arrow head
                  g.drawLine(p1.x, p1.y, p1.x + 3, p1.y - 11);
                  g.drawLine(p1.x, p1.y, p1.x + 7, p1.y + 7);

                  // draw transition text
                  drawText(list, tx, ty, g);
                  continue;
               }
               // get transition list from s2 to s
               String[] list2 = getTransitionStrings(s2, s);
               if (list2 == null || list2.length == 0) {
                  // transitions go from s to s2 only
                  Point p1 = new Point(p);
                  Point p2 = new Point(s2.getPosition());
                  Point p3 = new Point();
                  Point p4 = new Point();
                  Point f1 = new Point();
                  Point f2 = new Point();
                  Point tt = new Point();
                  p1.x += OFFSET;
                  p1.y += OFFSET;
                  p2.x += OFFSET;
                  p2.y += OFFSET;

                  // compute endpoints
                  p3 = getPointOnLine(p1, p2, OFFSET + 1);
                  f1.x = p1.x + p3.x;
                  f1.y = p1.y + p3.y;
                  f2.x = p2.x - p3.x;
                  f2.y = p2.y - p3.y;

                  // draw transition line
                  g.drawLine(f1.x, f1.y, f2.x, f2.y);

                  // draw arrow head
                  drawArrow(f1, f2, g);

                  // draw transition text
                  drawText(list, p1, p2, g);
               }
               else {
                  // transitions go back and forth between s and s2
                  Point p1 = new Point(p);
                  Point p2 = new Point(s2.getPosition());
                  p1.x += OFFSET;
                  p1.y += OFFSET;
                  p2.x += OFFSET;
                  p2.y += OFFSET;
                  double dist = 0.9 * OFFSET;
                  Point lineVec = getPointOnLine(p1, p2, (int) dist);
                  Point p2t = new Point();
                  p2t.x = p2.x - lineVec.x;
                  p2t.y = p2.y - lineVec.y;
                  Point perpVec = getPerpendicular(p2t, p2,
                     (int) Math.sqrt(OFFSET * OFFSET - dist * dist) + 1);

                  // compute transition line points
                  Point f1 = new Point();
                  f1.x = p1.x + lineVec.x + perpVec.x;
                  f1.y = p1.y + lineVec.y + perpVec.y;
                  Point f2 = new Point();
                  f2.x = p2t.x + perpVec.x;
                  f2.y = p2t.y + perpVec.y;

                  // draw transition line
                  g.drawLine(f1.x, f1.y, f2.x, f2.y);

                  // draw arrow head
                  drawArrow(f1, f2, g);

                  // draw transition text
                  drawText(list, f1, f2, g);
               }
            }
         }

         // draw black triangle marking start state
         Point pst = start.getPosition();
         int xpt0 = pst.x;
         int xpt1 = pst.x - TRI_SIZE;
         int xpt2 = xpt1;
         int ypt0 = pst.y + OFFSET;
         int ypt1 = ypt0 + TRI_SIZE;
         int ypt2 = ypt0 - TRI_SIZE;
         int[] xpts = {xpt0, xpt1, xpt2};
         int[] ypts = {ypt0, ypt1, ypt2};
         g.fillPolygon(xpts, ypts, 3);

         if (trans != null) {
            // draw red boxes around the transition destination state
            Point p = trans.getPosition();
            int px0 = p.x + 1;
            int py0 = p.y + 1;
            int px1 = p.x + STATE_SIZE - 3;
            int py1 = p.y + STATE_SIZE - 3;
            g.setColor(Color.red);
            g.fillRect(px0, py0, 4, 4);
            g.fillRect(px1, py0, 4, 4);
            g.fillRect(px0, py1, 4, 4);
            g.fillRect(px1, py1, 4, 4);
            g.setColor(Color.black);
         }
      }
   }


   // MODIFIERS

   /** adds a new state to this finite state machine */
   public void addState(State state) {
      synchronized (states) {
         states.addElement(state);
         numStates = states.size();
      }
   }

   /** removes a state from this finite state machine */
   public void removeState(State state) {
      synchronized (states) {
         states.removeElement(state);
         numStates = states.size();
      }
   }

   /** restarts the current computation */
   public void restartComputation() { startComputation(word); }


   // ABSTRACT MODIFIERS

   /** starts a new computation with the specified word */
   public abstract void startComputation(String word);

   /** advances the current computation one step at a time */
   public abstract void step();

   /** called whenever a key is pressed and a transition destination state
       exists. Needed so that each subclass can use the keystrokes to set up
       transitions according to its own model */
   protected abstract void transitionKeyPressed(
      State source, State dest, char key);


   // ACCESSORS

   /** returns a Vector of this finite state machine's states */
   public Vector getStates() { return states; }

   /** returns this finite state machine's start state */
   public State getStartState() { return start; }

   /** returns this finite state machine's transition function */
   public TransitionFunction getTransitionFunction() { return function; }

   /** returns the current computation's word */
   public String getWord() { return word; }

   /** returns the number of steps taken in the current computation */
   public int getStepsTaken() { return step; }

   /** whether the current computation is finished */
   public boolean isFinished() { return (answer != null); }

   /** determines whether this finite state machine accepts the word
       specified in the current computation; returns null if the
       computation is not yet finished */
   public Boolean accepts() { return answer; }

   /** returns the state diagram's minimum size */
   public Dimension getMinimumSize() {
      return new Dimension(CANVAS_SIZE, CANVAS_SIZE);
   }

   /** returns the state diagram's preferred size */
   public Dimension getPreferredSize() {
      return new Dimension(CANVAS_SIZE, CANVAS_SIZE);
   }


   // ABSTRACT ACCESSORS

   /** returns a set of strings representing the transitions from the given
       source state to the given destination state */
   protected abstract String[] getTransitionStrings(State source, State dest);


   // EVENT HANDLING

   /** event fired when a key is pressed */
   public void keyPressed(KeyEvent e) {
      int code = e.getKeyCode();
      char key = e.getKeyChar();

      // change state name of selected state
      if (sel == null) return;
      if (trans == null) {
         // change the name of the selected state
         String name = sel.getName();
         if (code == KeyEvent.VK_BACK_SPACE) {
            // backspace a character off the selected state's name
            int len = name.length();
            if (len > 0) {
               sel.setName(name.substring(0, len - 1));
               repaint();
            }
         }
         else {
            // add the typed character to the selected state's name
            if (key >= 32 && key < 256 && key != 127) {
               sel.setName(name + key);
               repaint();
            }
         }
      }
      else {
         // user is typing in transition information; subclass handles this
         transitionKeyPressed(sel, trans, key);
         repaint();
      }
   }

   /** event fired when a key is released */
   public void keyReleased(KeyEvent e) { }

   /** event fired when a key is pressed then released */
   public void keyTyped(KeyEvent e) { }

   /** event fired when mouse button is pressed then released */
   public void mouseClicked(MouseEvent e) { }

   /** event fired when mouse enters a bounding area */
   public void mouseEntered(MouseEvent e) { }

   /** event fired when mouse exits a bounding area */
   public void mouseExited(MouseEvent e) { }

   /** event fired when mouse button is pressed */
   public void mousePressed(MouseEvent e) {
      requestFocus();
      Point p = e.getPoint();
      int mod = e.getModifiers();
      boolean shift = e.isShiftDown();
      boolean left = ((mod & 0x10) != 0);
      boolean middle = ((mod & 0x0a) != 0);
      boolean right = ((mod & 0x04) != 0);

      trans = null;
      State state = getStateByPosition(p);
      if (shift) {
         if (left) {
            // shift + left press
            if (state == null) {
               // create a new state at the given location
               p.x -= OFFSET;
               p.y -= OFFSET;
               State s = new State("", false, p);
               addState(s);
               selectState(s);
               repaint();
            }
            else {
               // toggle this state's accept flag
               state.setAccept(!state.isAccept());
               repaint();
            }
         }
         else if (right) {
            // shift + right press: delete this state
            if (state != start) {
               // don't allow deletion of the start state!
               removeState(state);
               repaint();
            }
         }
      }
      else if (state != null) {
         if (left) {
            // left press: select this state
            selectState(state);
            repaint();
         }
         else if (right) {
            // right press: add transitions from current state to this state
            if (sel != null) {
               trans = state;
               repaint();
            }
         }
      }
      else {
         // mouse press occurred in cytoplasm; unselect all states
         selectState(null);
         repaint();
      }
   }

   /** event fired when mouse button is released */
   public void mouseReleased(MouseEvent e) { }

   /** event fired when mouse moves */
   public void mouseMoved(MouseEvent e) { }

   /** event fired when mouse moves while button is pressed */
   public void mouseDragged(MouseEvent e) {
      Point p = e.getPoint();
      int mod = e.getModifiers();
      boolean shift = e.isShiftDown();
      boolean left = ((mod & 0x10) != 0);
      boolean middle = ((mod & 0x0a) != 0);
      boolean right = ((mod & 0x04) != 0);

      // mouse drag occurred in the finite automaton canvas
      if (shift) {
         if (left) {
            // shift + left drag
         }
         else if (right) {
            // shift + right drag
         }
      }
      else {
         if (left) {
            // left drag: move selected state
            if (sel != null) {
               p.x -= OFFSET;
               p.y -= OFFSET;
               sel.setPosition(p);
               repaint();
            }
         }
         else if (right) {
            // right drag
         }
      }
   }


   // HELPER METHODS

   /** returns the first state that occupies the given position */
   private State getStateByPosition(Point p) {
      synchronized (states) {
         int len = states.size();
         for (int i=0; i<len; i++) {
            State s = (State) states.elementAt(i);
            Point sp = s.getPosition();
            Rectangle r = new Rectangle(sp.x, sp.y, STATE_SIZE, STATE_SIZE);
            if (r.contains(p)) return s;
         }
      }
      return null;
   }

   /** selects the given state */
   private void selectState(State s) {
      if (sel == s) return;
      if (s != null) s.setSelected(true);
      if (sel != null) sel.setSelected(false);
      sel = s;
   }
}

