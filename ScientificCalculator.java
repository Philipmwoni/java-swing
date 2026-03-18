import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.text.DecimalFormat;

/**
 * Scientific Calculator - Java Swing GUI
 * Features: Basic arithmetic, trig functions, log, power, sqrt, memory
 */
public class ScientificCalculator extends JFrame implements ActionListener {

    // ── Display ──────────────────────────────────────────────────────────────
    private JTextField expressionField;   // shows the running expression
    private JTextField resultField;       // shows current input / result

    // ── State ─────────────────────────────────────────────────────────────────
    private double      memory      = 0;
    private double      firstNum    = 0;
    private String      operator    = "";
    private boolean     newEntry    = true;   // next digit starts fresh
    private boolean     isDegrees   = true;   // angle mode
    private String      expression  = "";     // expression display string

    // ── Colors ────────────────────────────────────────────────────────────────
    private static final Color BG          = new Color(18,  18,  24);
    private static final Color DISPLAY_BG  = new Color(10,  10,  16);
    private static final Color BTN_DARK    = new Color(30,  30,  40);
    private static final Color BTN_OP      = new Color(50,  100, 180);
    private static final Color BTN_SCI     = new Color(35,  65,  100);
    private static final Color BTN_EQUAL   = new Color(0,   160, 120);
    private static final Color BTN_CLEAR   = new Color(180, 50,  60);
    private static final Color BTN_MEM     = new Color(80,  50,  120);
    private static final Color TEXT_LIGHT  = new Color(220, 235, 255);
    private static final Color TEXT_DIM    = new Color(120, 140, 170);
    private static final Color ACCENT      = new Color(0,   200, 160);

    // ── Button layout ─────────────────────────────────────────
    // Each row: { label, colspan }  (colspan 1 unless specified)
    private static final String[][] BUTTONS = {
        {"MC","MR","MS","M+","M-","DEG"},
        {"sin","cos","tan","log","ln","√"},
        {"x²","xʸ","1/x","(",")","%"},
        {"C","±","⌫","÷"},
        {"7","8","9","×"},
        {"4","5","6","−"},
        {"1","2","3","+"},
        {"0",".",  "="}
    };

    // ─────────────────────────────────────────────────────────────
    public ScientificCalculator() {
        super("Scientific Calculator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        buildUI();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ── UI construction ──────────────────────────────────────────────────────
    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG);
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Title bar
        JLabel title = new JLabel("  SCIENTIFIC  CALCULATOR");
        title.setFont(new Font("Courier New", Font.BOLD, 13));
        title.setForeground(ACCENT);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        root.add(title, BorderLayout.NORTH);

        // Display panel
        root.add(buildDisplay(), BorderLayout.CENTER);

        // Button panel
        root.add(buildButtons(), BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel buildDisplay() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(DISPLAY_BG);
        panel.setBorder(new CompoundBorder(
            new LineBorder(new Color(40, 80, 130), 1, true),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));

        expressionField = new JTextField("0");
        styleDisplay(expressionField, 14, SwingConstants.RIGHT);
        expressionField.setEditable(false);
        expressionField.setForeground(TEXT_DIM);

        resultField = new JTextField("0");
        styleDisplay(resultField, 28, SwingConstants.RIGHT);
        resultField.setEditable(false);
        resultField.setForeground(TEXT_LIGHT);

        panel.add(expressionField);
        panel.add(Box.createVerticalStrut(4));
        panel.add(resultField);
        return panel;
    }

    private void styleDisplay(JTextField field, int fontSize, int align) {
        field.setFont(new Font("Courier New", Font.PLAIN, fontSize));
        field.setBackground(DISPLAY_BG);
        field.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        field.setHorizontalAlignment(align);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, field.getPreferredSize().height + 6));
    }

    private JPanel buildButtons() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(BG);
        outer.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.fill   = GridBagConstraints.BOTH;

        int row = 0;
        for (String[] rowDef : BUTTONS) {
            // Last row: "0" spans 2 cols, "." and "=" each 1
            if (rowDef[0].equals("0")) {
                addBtn(outer, gbc, "0", row, 0, 2, 1, BTN_DARK);
                addBtn(outer, gbc, ".", row, 2, 1, 1, BTN_DARK);
                addBtn(outer, gbc, "=", row, 3, 1, 1, BTN_EQUAL);
            } else {
                int cols = rowDef.length;
                int colSpan = (cols < 4) ? 1 : 1; // all 1 wide; 4-col rows fill naturally
                // For rows with < 4 items fill the missing with colspan
                int btnWidth = (cols == 6) ? 1 : (cols == 4) ? 1 : 1;
                for (int c = 0; c < cols; c++) {
                    Color color = buttonColor(rowDef[c], row);
                    addBtn(outer, gbc, rowDef[c], row, c, btnWidth, 1, color);
                }
            }
            row++;
        }
        return outer;
    }

    private void addBtn(JPanel panel, GridBagConstraints gbc,
                        String label, int row, int col, int cw, int ch, Color bg) {
        gbc.gridx      = col;
        gbc.gridy      = row;
        gbc.gridwidth  = cw;
        gbc.gridheight = ch;
        gbc.weightx    = cw;
        gbc.weighty    = 1;

        CalcButton btn = new CalcButton(label, bg);
        btn.addActionListener(this);
        panel.add(btn, gbc);
    }

    private Color buttonColor(String lbl, int row) {
        if (row == 0) return BTN_MEM;
        if (row == 1 || row == 2) return BTN_SCI;
        switch (lbl) {
            case "C":  return BTN_CLEAR;
            case "=":  return BTN_EQUAL;
            case "÷": case "×": case "−": case "+": return BTN_OP;
            case "⌫": return BTN_CLEAR.darker();
            default:   return BTN_DARK;
        }
    }

    // ── Action handling ──────────────────────────────────────────────────────
    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        try {
            handleInput(cmd);
        } catch (Exception ex) {
            resultField.setText("Error");
            expression = "";
            expressionField.setText("");
            newEntry = true;
        }
    }

    private void handleInput(String cmd) {
        switch (cmd) {
            // ── Digits & decimal ──────────────────────────────────────────
            case "0": case "1": case "2": case "3": case "4":
            case "5": case "6": case "7": case "8": case "9":
                if (newEntry) { resultField.setText(cmd); newEntry = false; }
                else {
                    String cur = resultField.getText();
                    resultField.setText(cur.equals("0") ? cmd : cur + cmd);
                }
                break;
            case ".":
                if (newEntry) { resultField.setText("0."); newEntry = false; }
                else if (!resultField.getText().contains("."))
                    resultField.setText(resultField.getText() + ".");
                break;

            // ── Clear / backspace ─────────────────────────────────────────
            case "C":
                resultField.setText("0");
                expressionField.setText("");
                expression = ""; operator = ""; firstNum = 0; newEntry = true;
                break;
            case "⌫":
                String s = resultField.getText();
                if (s.length() > 1) resultField.setText(s.substring(0, s.length()-1));
                else { resultField.setText("0"); newEntry = true; }
                break;

            // ── Sign ──────────────────────────────────────────────────────
            case "±":
                double v = currentValue();
                resultField.setText(format(-v));
                break;

            // ── Basic operators ───────────────────────────────────────────
            case "+": case "−": case "×": case "÷": case "xʸ":
                firstNum  = currentValue();
                operator  = cmd;
                expression = format(firstNum) + " " + cmd;
                expressionField.setText(expression);
                newEntry = true;
                break;

            // ── Equals ────────────────────────────────────────────────────
            case "=":
                double second = currentValue();
                double result = compute(firstNum, second, operator);
                expressionField.setText(expression + " " + format(second) + " =");
                resultField.setText(format(result));
                firstNum = result;
                operator = "";
                newEntry = true;
                break;

            // ── Percent ───────────────────────────────────────────────────
            case "%":
                double pct = currentValue() / 100.0;
                resultField.setText(format(pct));
                newEntry = true;
                break;

            // ── Scientific functions ───────────────────────────────────────
            case "sin":  applyFunc("sin",  Math.sin(toRad(currentValue()))); break;
            case "cos":  applyFunc("cos",  Math.cos(toRad(currentValue()))); break;
            case "tan":  applyFunc("tan",  Math.tan(toRad(currentValue()))); break;
            case "log":  applyFunc("log",  Math.log10(currentValue()));       break;
            case "ln":   applyFunc("ln",   Math.log(currentValue()));         break;
            case "√":   applyFunc("√",    Math.sqrt(currentValue()));        break;
            case "x²":  applyFunc("x²",   Math.pow(currentValue(), 2));      break;
            case "1/x":  applyFunc("1/x",  1.0 / currentValue());             break;

            // ── Angle mode ────────────────────────────────────────────────
            case "DEG":
                isDegrees = !isDegrees;
                // Update button label live via repaint (we just toggle state)
                expressionField.setText("Mode: " + (isDegrees ? "DEG" : "RAD"));
                break;

            // ── Memory ────────────────────────────────────────────────────
            case "MC": memory = 0; expressionField.setText("M cleared"); break;
            case "MR": resultField.setText(format(memory)); newEntry = true; break;
            case "MS": memory = currentValue(); expressionField.setText("M = " + format(memory)); break;
            case "M+": memory += currentValue(); expressionField.setText("M = " + format(memory)); break;
            case "M-": memory -= currentValue(); expressionField.setText("M = " + format(memory)); break;

            // ── Parentheses (visual only for now) ─────────────────────────
            case "(": case ")":
                expressionField.setText(expressionField.getText() + cmd);
                break;
        }
    }

    private void applyFunc(String name, double result) {
        expressionField.setText(name + "(" + format(currentValue()) + ") =");
        resultField.setText(format(result));
        newEntry = true;
    }

    private double compute(double a, double b, String op) {
        switch (op) {
            case "+":   return a + b;
            case "−":  return a - b;
            case "×":  return a * b;
            case "÷":  if (b == 0) throw new ArithmeticException("÷0"); return a / b;
            case "xʸ": return Math.pow(a, b);
            default:   return b;
        }
    }

    private double toRad(double angle) {
        return isDegrees ? Math.toRadians(angle) : angle;
    }

    private double currentValue() {
        try { return Double.parseDouble(resultField.getText()); }
        catch (NumberFormatException e) { return 0; }
    }

    private String format(double d) {
        if (Double.isNaN(d))      return "NaN";
        if (Double.isInfinite(d)) return "∞";
        // If whole number, show without decimal
        if (d == Math.floor(d) && !Double.isInfinite(d) && Math.abs(d) < 1e15)
            return String.valueOf((long) d);
        DecimalFormat df = new DecimalFormat("#.##########");
        return df.format(d);
    }

    // ── Custom button component ───────────────────────────────────────────────
    static class CalcButton extends JButton {
        private final Color baseColor;
        private boolean hovered = false;

        CalcButton(String label, Color bg) {
            super(label);
            this.baseColor = bg;
            setFont(new Font("Courier New", Font.BOLD, 14));
            setForeground(new Color(220, 235, 255));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setPreferredSize(new Dimension(68, 48));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                public void mouseExited (MouseEvent e) { hovered = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color fill = hovered ? baseColor.brighter() : baseColor;
            g2.setColor(fill);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));

            // subtle top highlight
            g2.setColor(new Color(255, 255, 255, 25));
            g2.fill(new RoundRectangle2D.Float(1, 1, getWidth()-2, getHeight()/2, 9, 9));

            // border
            g2.setColor(new Color(255, 255, 255, 30));
            g2.setStroke(new BasicStroke(1f));
            g2.draw(new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, 10, 10));

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ── Entry point ───────────────────────────────────────────────────────────
    public static void main(String[] args) {
        // Use system look-and-feel as base, then override
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
        catch (Exception ignored) {}

        SwingUtilities.invokeLater(ScientificCalculator::new);
    }
}