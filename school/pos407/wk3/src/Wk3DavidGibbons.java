/*
 * Wk3DavidGibbons.java
 * Week 3 Programming Assignment - Mortgage Payment Calculator
 * POS/407 - Computer Programming II
 * David C. Gibbons, dcgibbons@email.uophx.edu
 *
 * Version | Date       | Description
 * --------|------------|-----------------------------------------------------
 *   1.00  | 2006-01-12 | Initial version.
 *   2.00  | 2006-01-14 | Updated for week 3 assignment; stand-alone Swing
 *                      | application instead of an Applet; calculation of
 *                      | mortgage payment detail breakdown and display of the
 *                      | data in a tabular form.
 *   2.10  | 2006-01-21 | Added validation of principal amount so any current
 *                      | mortgage is cleared and the user informed of the
 *                      | invalid input.
 */

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Vector;


/**
 * The week 3 assignment implemented by this class is a stand-alone application
 * that allows the user to calculate a mortgage payment.
 * <p/>
 * Unlike the week 2 assignment, the user can only input the mortgage principal
 * and then must choose from a selection of pre-defined mortgage terms that
 * define the rate and mortgage length.
 * <p/>
 * In addition, the payment breakdown over the length of the mortgage is
 * displayed in a table indicating the interest and principal breakdown amounts.
 * <p/>
 * Also, the mortgage is automatically recalculated whenever the user changes
 * the principal amount or mortgage terms, eliminating the need for a
 * Calculate button.
 */

public class Wk3DavidGibbons {
    /**
     * The following constants are for titles of frames and windows.
     */
    private static final String TITLE_MAINFRAME = "Mortgage Calculator";

    /**
     * The following label constants are used to label each of the data entry
     * fields on the actual user-interface.
     */
    private static final String LABEL_CLOSE = "Close";
    private static final String LABEL_PRINCIPAL = "Principal Amount:";
    private static final String LABEL_MORTGAGE_CHOICES = "Mortgage Terms:";

    /**
     * The following input masks are numeric format masks used when creating the
     * data entry fields themselves. Each mask will be used to display the
     * current numeric value to the user after a new value has been input.
     */
    private static final String MASK_PRINCIPAL = "#,###.##";

    /**
     * The following message constants are used to display header, help, and
     * results of the mortgage payment calculation. Since JLabel can display
     * simple HTML tags embedded within the label text, we get some nice
     * formatting without having to do excessive work at the UI component level.
     */
    private static final String MSG_HEADER = "<html><h1>Mortgage Calculator</h1></html>";
    private static final String MSG_PAYMENT = "<html><b>Payment Amount:</b> {0,number,$ #,##0.00}</html>";
    private static final String MSG_INVALID_PRINCIPAL = "<html><b>Invalid Principal Amount</b></html>";
    private static final String MSG_PRINCIPAL_NEGATIVE = "<html><b>Principal Amount Must Be > 0.00</html>";

    /**
     * The JTable of the payment history needs a default number of visible
     * rows so that its preferredSize can be accurately calculated.
     */
    private static final int VISIBLE_PAYMENT_TABLE_ROWS = 12;

    /**
     * A constant to indicate the minimum number of columns that the data entry
     * fields should provide to the user. This helps to ensure that our
     * components calculate an appropriate preferredSize.
     */
    private static final int FIELD_COLUMNS = 10;

    /**
     * The following constants are default values for the data entry field and
     * are used to populate those fields when the app is initialized. These
     * defaults make it easy for the user to get a result and to see the format
     * of the expceted input data.
     */
    private static final double DEFAULT_PRINCIPAL = 325000.00;

    /**
     * The user is given several mortgage terms to pick from after inputting
     * their mortgage principal. The following array of mortgages are the
     * default mortgage terms available for the user.
     */
    private static final Mortgage[] DEFAULT_MORTGAGES = {
            new Mortgage(0, 5.35, 7),
            new Mortgage(0, 5.50, 15),
            new Mortgage(0, 5.75, 30)
    };

    /**
     * The Java Look & Feel Guidelines discuss the visual alignment between
     * components. Components are generally separated by multiples of a single
     * Look & Feel padding unit, which is 6 pixels. If a component typically
     * contains a 3D-border or shadow, then the padding is a multiple of 6
     * pixels minus 1. Further details are discussed at
     * http://java.sun.com/products/jlf/ed2/book/HIG.Visual2.html
     */
    private static final int LFPAD = 6;

    /**
     * This constant defines the property name to listen to whenever a
     * input field's value changes.
     */
    private static final String VALUE_PROPERTY = "value";

    /**
     * Since we are in a stand-alone application, we need a JFrame to be the
     * root container for all of our other UI elements.
     */
    private JFrame mainFrame;

    /**
     * Variables to define the label and field for the Principal amount.
     */
    private JLabel labelPrincipal;
    private JFormattedTextFieldHelper fieldPrincipal;

    /**
     * Variables to define the label and combo-box menu for the available
     * Mortgage terms from which the user can pick.
     */
    private JLabel labelMortgageChoices;
    private JComboBox fieldMortgageChoices;

    /**
     * Variables to define the header and message labels.
     */
    private JLabel labelHeader;
    private JLabel labelMessage;

    /**
     * Variables to store the mortgage payments table and the table model used
     * to define the data
     */
    private MortgagePaymentsTableModel paymentsModel;
    private JTableHelper paymentsTable;

    /**
     * Variables to define the command buttons presented to the user.
     */
    private JButton closeButton;

    /**
     * main entry point for this application - starts our Mortgage Calculator
     * user interface
     *
     * @param args not used
     */
    public static void main(String[] args) {
        new Wk3DavidGibbons();
        // Swing event loop takes over in another thread; this thread will
        // terminate but the application will not exit until the Swing event
        // thread exits or the application is forced to exit.
    }

    /**
     * A private onstructor for this class so that an instance can only be
     * created via the main method.
     */
    private Wk3DavidGibbons() {
        createComponents();

        // after the components are all created, calculate the mortgage for the
        // default mortgage - this will populate our table model and other
        // fields and allow us to properly resize the table to have a useful
        // preferredSize based on real data
        calculateMortgage();
        paymentsTable.resizeTableColumns();

        layoutComponents();

        // once our components have been laid out then our minimum and preferred
        // sizes should be calculated, so keep an eye out on the mainframe and
        // don't let the user resize it smaller than the minimum size
        mainFrame.addComponentListener(new ResizeWatcher());

        // center the main frame on the user's display before making it visible
        centerWindow(mainFrame);

        // display our main frame - this will start the Swing event loop and
        // essentially take over main-line execution of the program
        mainFrame.setVisible(true);

        // make sure that our principal field requests focus once the frame has
        // been displayed
        fieldPrincipal.requestFocusInWindow();
    }

    /**
     * Creates all of the UI components used by this application.
     */
    protected void createComponents() {
        mainFrame = new JFrame(TITLE_MAINFRAME);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //mainFrame.addWindowListener(new WindowWatcher());

        // the data entry fields need to have their value properties watched...
        final FieldValueWatcher fieldWatcher = new FieldValueWatcher();

        // the data entry fields need their focus watched...
        final FocusWatcher focusWatcher = new FocusWatcher();

        // create the label and field for the mortgage principal amount
        final DecimalFormat df = new DecimalFormat(MASK_PRINCIPAL);
        fieldPrincipal = new JFormattedTextFieldHelper(df);
        fieldPrincipal.addFocusListener(focusWatcher);
        fieldPrincipal.setColumns(FIELD_COLUMNS);
        fieldPrincipal.addPropertyChangeListener(VALUE_PROPERTY, fieldWatcher);

        labelPrincipal = new JLabel(LABEL_PRINCIPAL);
        labelPrincipal.setLabelFor(fieldPrincipal);

        // create the label and field for the mortgage terms combo-box/list
        fieldMortgageChoices = new JComboBox(
                new Vector(Arrays.asList(DEFAULT_MORTGAGES)));
        fieldMortgageChoices.addActionListener(new FieldActionListener());

        labelMortgageChoices = new JLabel(LABEL_MORTGAGE_CHOICES);
        labelMortgageChoices.setLabelFor(fieldMortgageChoices);

        // create the label that will provide the header text for the panel
        labelHeader = new JLabel(MSG_HEADER);

        // create the label that will provide any mesages to the user,
        // including the newly calculated mortgage payment amounts
        labelMessage = new JLabel("");

        // create the table model and table that will display the payment
        // breakdown once a mortgage has been calculated
        paymentsModel = new MortgagePaymentsTableModel();
        paymentsTable = new JTableHelper(paymentsModel);
        paymentsTable.setColumnSelectionAllowed(false);
        paymentsTable.setRowSelectionAllowed(true);
        paymentsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        paymentsTable.setDefaultRenderer(Double.class, new CurrencyRenderer());
        paymentsTable.limitTableViewport(VISIBLE_PAYMENT_TABLE_ROWS, true,
                                         false);

        // create the buttom that will allow the user to close the main frame;
        // the user will have several options, depending on their OS, for
        // shutting down the application besides this button
        closeButton = new JButton(new CloseAction());

        // set default values for all of the fields to make the user feel cozy
        fieldPrincipal.setValue(new Double(DEFAULT_PRINCIPAL));
    }

    /**
     * Lays all of the UI components used by the app into the mainframe's
     * default container panel.
     */
    protected void layoutComponents() {
        final JPanel topPanel = layoutTopPanel();
        final JPanel centerPanel = layoutCenterPanel();
        final JPanel buttonPanel = layoutButtonPanel();

        // when adding the content to the root panel we must follow the look &
        // feel guidelines which state 2 padding units between the edges of the
        // root panel and the components. If the bottom and right components
        // are 3D or shadowed components then it should be 2 padding units - 1
        // for consistency.

        // the top panel gives us our border for the top and left sides
        final Border topBorder = BorderFactory.createEmptyBorder(
                LFPAD * 2,      // top
                LFPAD * 2,      // left
                0,              // bottom
                LFPAD * 2 - 1);  // right
        topPanel.setBorder(topBorder);

        // the center border needs to have a visible separation from the top
        // panel components
        final Border centerBorder = BorderFactory.createEmptyBorder(
                LFPAD * 2,      // top
                LFPAD * 2,      // left
                0,              // bottom
                LFPAD * 2 - 1);  // right
        centerPanel.setBorder(centerBorder);

        // the bottom panel gives us our border for the bottom and right sides
        // and also must be separated from the top panel by 3 padding units - 1
        // to give any buttons appropriate spacing
        final Border bottomBorder = BorderFactory.createEmptyBorder(
                LFPAD * 3 - 1,  // top
                LFPAD * 2,      // left
                LFPAD * 2 - 1,  // bottom
                LFPAD * 2 - 1); // right
        buttonPanel.setBorder(bottomBorder);

        // finally, change our root panel to have a BorderLayout and force the
        // top panel to use the North section and the bottom panel to use the
        // South section. this will allow the root panel to be resized freely
        // and the other panels should react correctly in both grow and shrink
        // resizing scenarios.
        final JPanel contentPanel = (JPanel) mainFrame.getContentPane();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.add(topPanel, BorderLayout.NORTH);
        contentPanel.add(centerPanel, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainFrame.pack();
    }

    /**
     * Utility method to layout the top panel containing all of the messages
     * and data entry fields.
     *
     * @return the new top panel
     */
    private JPanel layoutTopPanel() {
        // create a panel that will be located at the top of our content area
        // and will contain all of our message labels and data entry fields.
        // the complicated GridBagLayout is used, so utility methods are used
        // to help isolate the grungy details.
        final JPanel topPanel = new JPanel(new GridBagLayout());

        // the header label must be seperated from the rest of the components
        addLabel(topPanel, labelHeader, LFPAD * 2, LFPAD * 2);

        addField(topPanel, labelPrincipal, fieldPrincipal);
        addField(topPanel, labelMortgageChoices, fieldMortgageChoices);

        // the message label does not require any bottom padding as it is the
        // last component in the panel
        addLabel(topPanel, labelMessage, 0, LFPAD * 2);

        return topPanel;
    }

    /**
     * Utility method to layout the center panel that holds a scrollable
     * JTable with all of the mortgage payment breakdown.
     */
    private JPanel layoutCenterPanel() {
        // the payments table is placed in a scrollable panel; by default we
        // always wanjt a vertical scrollbar because we don't want it to pop
        // in-and-out of visibility as the user resizes the table, but the
        // horizontal scrollbar isn't usually visible as we resize our columns,
        // so it's OK if it pops in and out of visbility.
        final JScrollPane p = new JScrollPane(paymentsTable,
                                              JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                                              JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // we plop the JScrollPane holding our table into the center of another
        // BorderLayout so that it will always receive both horizontal and
        // vertical space as the main frame is resized.
        final JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(p, BorderLayout.CENTER);

        return centerPanel;
    }

    /**
     * Utility method to create and layout the panel used to hold command
     * buttons.
     *
     * @return the new button panel
     */
    private JPanel layoutButtonPanel() {
        // by Look & Feel convention, command buttons are aligned horizontially
        // with a single padding unit between them (minus 1 because buttons are
        // 3D).
        final GridLayout gridLayout = new GridLayout(1, 0, LFPAD * 2 - 1, 0);
        final JPanel buttonGridPanel = new JPanel(gridLayout);
        buttonGridPanel.add(closeButton);

        // to force all of the bottons to be their preferred size and be
        // aligned to the right of the button panel, a BorderLayout is used and
        // the button grid is placed in the East section of the layout grid.
        final JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.add(buttonGridPanel, BorderLayout.EAST);

        return buttonPanel;
    }


    /**
     * Utility method to add a centered label to our panel using a
     * GridBagLayout.
     *
     * @param panel  the panel to which the component will be added
     * @param label  the actual label object that will be added
     * @param bottom any inset padding on the bottom of the component
     * @param right  any inset padding on the right of the component
     */
    protected void addLabel(final JPanel panel,
                            final JLabel label,
                            final int bottom,
                            final int right) {
        label.setHorizontalAlignment(SwingConstants.CENTER);

        // this label should be centered, take up the entire row, and be resized
        // horizontally whenever the panel changes size
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets = new Insets(0, 0, bottom, right);
        gbc.weightx = 1.0;

        panel.add(label, gbc);
    }


    /**
     * Utility method to add a field consisting of a JLabel and an actual field
     * object. The label will be given minimum spacing while the remainder goes
     * to the field objects themselves.
     *
     * @param panel the panel to which the component will be added
     * @param label the actual label object that will be added
     * @param field the actual field component object that will be added
     */
    protected void addField(final JPanel panel, final JLabel label,
                            final JComponent field) {
        label.setHorizontalAlignment(SwingConstants.RIGHT);

        final GridBagConstraints gbc = new GridBagConstraints();

        // the label should be in a fixed size column and not be resized as
        // the panel changes size
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(0, 0, LFPAD * 2, LFPAD * 2);
        gbc.weightx = 0.0;
        panel.add(label, gbc);

        // the field should receive the remaining space on the row and receive
        // all of the extra space when the panel is resized (or shrunk)
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets = new Insets(0, 0, LFPAD * 2 - 1, LFPAD * 2 - 1);
        gbc.weightx = 1.0;
        panel.add(field, gbc);
    }

    /**
     * Utility method that will calculate a new mortgage payment and payment
     * breakdown based on the user's current selection.
     */
    private void calculateMortgage() {
        // fetch the new values from the data entry fields and calculate a
        // new mortgage
        final double principal = ((Number) fieldPrincipal.getValue()).doubleValue();
        if (principal <= 0.0) {
            invalidInput(MSG_PRINCIPAL_NEGATIVE);
        } else {
            // create a new Mortgage object from the selected mortgage terms and
            // then update the principal with the user's requested value
            final Mortgage mortgageTerms = (Mortgage) fieldMortgageChoices.getSelectedItem();
            final Mortgage mortgage = new Mortgage(mortgageTerms);
            mortgage.setPrincipal(principal);

            // format the mortgage payment into a user-friendly message
            final Object[] values = {new Double(mortgage.getPayment())};
            final String message = MessageFormat.format(MSG_PAYMENT, values);

            // update our message label and payment table model and force our UI
            // to repaint itself with the new mortgage results
            labelMessage.setText(message);
            paymentsModel.setPayments(mortgage.getPayments());
            mainFrame.repaint();
        }
    }

    /**
     * Utility method that informs the user that their input was invalid.
     */
    private void invalidInput(final String msg) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                labelMessage.setText(msg);
                paymentsModel.setPayments(new MortgagePayment[0]);
                fieldPrincipal.requestFocusInWindow();
                mainFrame.repaint();
            }
        });
    }


    /**
     * Utility method that will close and cleanup the main frame and then exit
     * the application.
     */
    private void exitApp() {
        mainFrame.setVisible(false);
        mainFrame.dispose();
        System.exit(0);
    }

    /**
     * Utility method that will center a window on the user's screen.
     * @param window the window or frame to center
     */
    private void centerWindow(final Window window) {
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final Dimension frameSize = window.getSize();

        // if our frame is actually larger than the screen then we'll want the
        // calculation to still work properly
        if (frameSize.height > screenSize.height) {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width) {
            frameSize.width = screenSize.width;
        }

        final int xPos = (screenSize.width - frameSize.width) / 2;
        final int yPos = (screenSize.height - frameSize.height) / 2;
        window.setLocation(xPos, yPos);
    }

    /**
     * This inner class watches for focus events from the text components
     * found within this app. If the focus is gained for a text component
     * then the entire field is selected, allowing the user to overwrite the
     * old value by default. If the focus is lost and the field value has
     * changed, then the payment field has its result erased so the user
     * knows they must calculate the new mortgage.
     */
    private class FocusWatcher extends FocusAdapter {
        public void focusGained(final FocusEvent event) {
            final Component component = event.getComponent();
            if (component instanceof JTextComponent) {
                // select all of the component text whenever the component
                // gains focus - we use invokeLater to ensure that the focus is
                // grabbed after all other work is done, which allows
                // components like JFormattedTextField to work properly
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        ((JTextComponent) component).selectAll();
                    }
                });
            }
        }
    }

    /**
     * This inner class will be activated by the Close button and will
     * gracefully cause the application to exit.
     */
    private class CloseAction extends AbstractAction {
        public CloseAction() {
            putValue(Action.NAME, LABEL_CLOSE);
        }

        public void actionPerformed(final ActionEvent event) {
            exitApp();
        }
    }

    /**
     * This inner class will watch the mainframe for any resize events; if the
     * frame is being resized smaller than the initial minimumSize then the
     * resize will be stopped and the size returned back to the minimumSize.
     * This should prevent the user from making the window so small that
     * components begin to overlap one another.
     */
    private class ResizeWatcher extends ComponentAdapter {
        private final Dimension minSize;

        public ResizeWatcher() {
            minSize = mainFrame.getMinimumSize();
        }

        public void componentResized(final ComponentEvent evt) {
            final Dimension newSize = mainFrame.getSize();

            boolean resize = false;

            if (newSize.width < minSize.width) {
                resize = true;
                newSize.width = minSize.width;
            }

            if (newSize.height < minSize.height) {
                resize = true;
                newSize.height = minSize.height;
            }

            if (resize) {
                mainFrame.setSize(newSize);
            }
        }
    }

    /**
     * This inner class will watch for property changes to the user input fields
     * and will cause the mortgage to recalculate itself whenever a property
     * we are interested in changes.
     */
    private class FieldValueWatcher implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent event) {
            calculateMortgage();
        }
    }

    /**
     * This inner class will recalculate the mortgage whenever the user performs
     * an action on the mortgage task combobox, i.e. selecting a new mortgage
     * term.
     */
    private class FieldActionListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            calculateMortgage();
        }
    }

    /**
     * This inner class provides a custom JTable cell renderer for numeric
     * objects that will format the value as a currency amount.
     */
    private static class CurrencyRenderer extends DefaultTableCellRenderer {
        final DecimalFormat currencyFmt = new DecimalFormat("$#,##0.00");

        public void setValue(Object value) {
            setText(value == null ? "" : currencyFmt.format(value));
            setHorizontalAlignment(SwingConstants.TRAILING);
        }
    }

    /**
     * This inner class provides a mechanism to trap invalid user input from
     * the JFormattedTextField without implementing a custom formatter. If
     * invalid input is detected then the user is informed and the current
     * mortgage calculation is reset.
     */
    private class JFormattedTextFieldHelper extends JFormattedTextField {
        public JFormattedTextFieldHelper(final Format format) {
            super(format);
        }

        public void commitEdit() throws ParseException {
            try {
                super.commitEdit();
            } catch (ParseException ex) {
                invalidInput(MSG_INVALID_PRINCIPAL);
                throw ex;
            }
        }
    }
}
