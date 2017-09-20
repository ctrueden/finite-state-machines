// FSMApplet.java

import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;

/** An applet for visualizing and steering NFA, PDA and TM computations. */
public class FSMApplet extends Applet implements ActionListener {

   // CONSTANTS

   /** machine type is Turing Machine */
   private static final int TM = 0;

   /** machine type is pushdown automaton */
   private static final int PDA = 1;

   /** machine type is non-deterministic finite automaton */
   private static final int NFA = 2;


   // STATE VARIABLES

   /** finite state machine */
   private FiniteStateMachine fsm;

   /** type of machine (TM, PDA or NFA) */
   private int machineType;

   /** step size */
   private int stepSize = 1;

   /** label for displaying the number of steps taken by the computation */
   private Label stepLabel;

   /** canvas for displaying the computation's position in the word */
   private Component posCanvas;

   /** canvas for displaying whether the computation accepts */
   private Component solCanvas;

   /** text field for entering word for computation */
   private TextField wordbox;

   /** button for stepping through computation */
   private Button step;

   /** button for restarting computation */
   private Button restart;

   /** adds a component to the applet with the specified constraints */
   protected void addComponent(Component c,
      GridBagLayout layout, int x, int y, int w, int h,
      int fill, int pad, int anchor, double wx, double wy)
   {
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.gridx = x;
      gbc.gridy = y;
      gbc.gridwidth = w;
      gbc.gridheight = h;
      gbc.fill = fill;
      gbc.insets = new Insets(pad, pad, pad, pad);
      gbc.anchor = anchor;
      gbc.weightx = wx;
      gbc.weighty = wy;
      layout.setConstraints(c, gbc);
      add(c);
   }

   /** refreshes the applet's display (e.g., after a computation step) */
   private void refreshDisplay() {
      if (machineType == TM) {
         stepLabel.setText("Steps taken: " + fsm.getStepsTaken());
      }
      else posCanvas.repaint();
      solCanvas.repaint();
   }

   /** initializes the applet */
   public void init() {
      // determine finite state machine type and create the machine
      State start = new State("start", false, new Point(20, 20));
      State[] states;
      String machine = getParameter("machine");
      String title;
      if (machine == null) machine = "";
      if (machine.equalsIgnoreCase("tm") ||
         machine.equalsIgnoreCase("Turing Machine") ||
         machine.equalsIgnoreCase("Turing_Machine") ||
         machine.equalsIgnoreCase("TuringMachine"))
      {
         // Turing Machine
         int w = 2 * FiniteStateMachine.STATE_SIZE;
         State accept = new State("accept", true, new Point(20, 20 + w));
         State reject = new State("reject", false, new Point(20 + w, 20 + w / 2));
         states = new State[] {start, accept, reject};
         fsm = new TuringMachine(states, start, accept, reject);
         title = "Turing Machine Simulator";
         machineType = TM;
      }
/* CTR TEMP
      else if (machine.equalsIgnoreCase("PDA") ||
         machine.equalsIgnoreCase("Pushdown Automaton") ||
         machine.equalsIgnoreCase("Pushdown_Automaton") ||
         machine.equalsIgnoreCase("PushdownAutomaton"))
      {
         // pushdown automaton
         states = new State[] {start};
         fsm = new PushdownAutomaton(states, start);
         title = "Pushdown Automaton Simulator";
         machineType = PDA;
      }
*/
      else {
         // finite automaton
         states = new State[] {start};
         fsm = new FiniteAutomaton(states, start);
         title = "Non-deterministic Finite Automaton Simulator";
         machineType = NFA;
      }

      // lay out GUI components using GridBagLayout (yuck!)
      GridBagLayout gridbag = new GridBagLayout();
      setLayout(gridbag);
      setBackground(Color.white);

      // add a Label header
      Label l = new Label(title);
      addComponent(l, gridbag, 0, 0, 4, 1,
         GridBagConstraints.NONE, 0, GridBagConstraints.CENTER, 0.0, 0.0);

      // add the main scrolling pane that displays the automaton graphically
      ScrollPane pane = new ScrollPane(ScrollPane.SCROLLBARS_ALWAYS);
      Adjustable hadj = pane.getHAdjustable();
      Adjustable vadj = pane.getVAdjustable();
      int unit = FiniteStateMachine.STATE_SIZE / 4;
      int block = 4 * FiniteStateMachine.STATE_SIZE;
      hadj.setUnitIncrement(unit);
      vadj.setUnitIncrement(unit);
      hadj.setBlockIncrement(block);
      vadj.setBlockIncrement(block);

      // add the finite state machine's state diagram
      pane.add(fsm);
      addComponent(pane, gridbag, 0, 1, 1, 5,
         GridBagConstraints.BOTH, 3, GridBagConstraints.CENTER, 1.0, 1.0);

      if (machineType == PDA) {
         // add the stack
         // CTR DO THIS
      }

      // add the word label
      l = new Label("Word:");
      addComponent(l, gridbag, 2, 1, 1, 1,
         GridBagConstraints.NONE, 0, GridBagConstraints.SOUTHEAST, 0.0, 0.3);

      // add the word text box
      wordbox = new TextField();
      wordbox.setName("word");
      wordbox.addActionListener(this);
      addComponent(wordbox, gridbag, 3, 1, 2, 1,
         GridBagConstraints.HORIZONTAL, 0, GridBagConstraints.SOUTHWEST,
         0.0, 0.3);

      if (machineType == TM) {
         // add the step number label
         stepLabel = new Label("Steps taken: 0");
         addComponent(stepLabel, gridbag, 2, 2, 2, 1,
            GridBagConstraints.NONE, 0, GridBagConstraints.EAST, 0.0, 0.0);
      }
      else {
         // add the position label
         l = new Label("Position:");
         addComponent(l, gridbag, 2, 2, 1, 1,
            GridBagConstraints.NONE, 0, GridBagConstraints.EAST, 0.0, 0.0);

         // add the position pointer
         posCanvas = new Component() {
            public void paint(Graphics g) {
               // draw position pointer under word text box
               String word = fsm.getWord();
               if (word != null) {
                  FontMetrics fm = getFontMetrics(wordbox.getFont());
                  int pos = fsm.getStepsTaken();
                  int width = fm.stringWidth(word.substring(0, pos));
                  width += (width == 0 ? 4 : 2);
                  g.drawLine(width, 0, width, 20);
                  g.drawLine(width, 0, width - 3, 8);
                  g.drawLine(width, 0, width + 3, 8);
               }
            }
         };
         addComponent(posCanvas, gridbag, 3, 2, 2, 1,
            GridBagConstraints.BOTH, 0, GridBagConstraints.WEST, 0.0, 0.0);
      }

      // add the step size label
      l = new Label("Step size:");
      addComponent(l, gridbag, 2, 3, 1, 1,
         GridBagConstraints.NONE, 0, GridBagConstraints.EAST, 0.0, 0.0);

      // add the step size text box
      TextField text = new TextField("" + stepSize);
      text.setName("step size");
      text.addActionListener(this);
      addComponent(text, gridbag, 3, 3, 1, 1,
         GridBagConstraints.HORIZONTAL, 0, GridBagConstraints.WEST, 0.0, 0.0);

      // add the step button
      step = new Button("Step");
      step.setEnabled(false);
      step.addActionListener(this);
      addComponent(step, gridbag, 2, 4, 2, 1,
         GridBagConstraints.HORIZONTAL, 3, GridBagConstraints.EAST, 0.0, 0.0);

      // add the restart button
      restart = new Button("Restart");
      restart.setEnabled(false);
      restart.addActionListener(this);
      addComponent(restart, gridbag, 4, 4, 1, 1,
         GridBagConstraints.HORIZONTAL, 3, GridBagConstraints.WEST, 0.0, 0.0);

      // add the solution box label
      l = new Label("Solution:");
      addComponent(l, gridbag, 2, 5, 1, 1,
         GridBagConstraints.NONE, 0, GridBagConstraints.NORTHEAST, 0.0, 1.0);

      // add the solution box
      solCanvas = new Component() {
         public void paint(Graphics g) {
            Boolean answer = fsm.accepts();
            g.setColor(Color.black);
            g.drawRect(0, 2, 20, 20);
            if (answer == null) {
               g.drawString("?", 7, 17);
               g.drawString("unknown", 25, 17);
            }
            else {
               if (answer.booleanValue()) {
                  g.drawString("accepts", 25, 17);
                  g.setColor(Color.green);
               }
               else {
                  g.drawString("rejects", 25, 17);
                  g.setColor(Color.red);
               }
               g.fillRect(1, 3, 19, 19);
            }
         }
      };
      addComponent(solCanvas, gridbag, 3, 5, 2, 1,
         GridBagConstraints.BOTH, 0, GridBagConstraints.NORTHWEST, 0.0, 1.0);

      if (machineType == TM) {
         // add the tape
         // CTR DO THIS: use getTape() and getPosition() methods!
      }

      // update the applet's display
      refreshDisplay();
   }

   /** event fired when various events occur */
   public void actionPerformed(ActionEvent e) {
      Object o = e.getSource();
      if (o instanceof Button) {
         // a button was pressed
         Button b = (Button) o;
         String n = b.getLabel();
         if ("Step".equals(n)) {
            // Step button was clicked
            if (fsm.isFinished()) return;
            step.setEnabled(false);
            restart.setEnabled(false);
            for (int i=0; i<stepSize && !fsm.isFinished(); i++) fsm.step();
            refreshDisplay();
            restart.setEnabled(true);
            if (fsm.isFinished()) restart.requestFocus();
            else step.setEnabled(true);
         }
         else if ("Restart".equals(n)) {
            // Restart button was clicked
            fsm.restartComputation();
            refreshDisplay();
            if (!fsm.isFinished()) {
               step.setEnabled(true);
               step.requestFocus();
            }
         }
         else {
            // an unknown button was clicked
         }
      }
      else if (o instanceof TextField) {
         // a line of text was entered into a text field
         TextField t = (TextField) o;
         String n = t.getName();
         if ("word".equals(n)) {
            // word was changed
            String word = t.getText();
            fsm.startComputation(word);
            refreshDisplay();
            step.setEnabled(!fsm.isFinished());
            restart.setEnabled(true);
         }
         else if ("step size".equals(n)) {
            // step size was changed
            int newSize = -1;
            try { newSize = Integer.parseInt(t.getText()); }
            catch (NumberFormatException exc) {
               if (State.DEBUG) exc.printStackTrace();
            }
            if (newSize >= 1) stepSize = newSize;
            t.setText("" + stepSize);
         }
         else {
            // a line of text was entered into an unknown text field
         }
         fsm.requestFocus();
      }
      else {
         // an unknown action occurred
      }
   }

}

