import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.undo.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.HttpURLConnection;
import java.net.URL;


// ====================== AI Helper Simple =========================
 class SimpleAI {
    static String API_KEY = "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";

    public static String summarize(String text) throws Exception {

        String json = "{ \"model\": \"openai/gpt-4o-mini\", "
                + "\"messages\": [{\"role\":\"user\", \"content\": \"Summarize this: " + text + "\"}] }";

        // OpenRouter endpoint
        URL url = new URL("https://openrouter.ai/api/v1/chat/completions");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
        conn.setRequestProperty("HTTP-Referer", "https://example.com"); // required field
        conn.setRequestProperty("X-Title", "Java Notepad AI"); // optional but recommended
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        conn.getOutputStream().write(json.getBytes());

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder reply = new StringBuilder();
        String line;

        while ((line = br.readLine()) != null) reply.append(line);

        String result = reply.toString();

        // Basic output extraction (simple)
        int start = result.indexOf("content") + 11;
        int end = result.indexOf("\"", start);

        return result.substring(start, end);
    }
}
// ====================== DRAWING PANEL =========================
class DrawArea extends JPanel {

    Color brushColor = Color.BLACK;
    int brushSize = 5;
    boolean eraseMode = false;

    public DrawArea() {
        setBackground(Color.WHITE);
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { draw(e); }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) { draw(e); }
        });
    }

    void draw(MouseEvent e) {
        Graphics2D g = (Graphics2D) getGraphics();
        g.setColor(eraseMode ? getBackground() : brushColor);
        g.fillOval(e.getX(), e.getY(), brushSize, brushSize);
    }

    public void saveImage(File file) throws Exception {
        BufferedImage bi = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics g = bi.getGraphics();
        paint(g);
        g.dispose();
        ImageIO.write(bi, "png", file);
    }

    public void clear() { repaint(); }
}


// ====================== MAIN NOTEPAD =========================
class MyNotePad extends JFrame implements ActionListener {

    JTextArea jta;
    MenuBar mb;
    Menu fileMenu, editMenu, viewMenu, fontMenu, colorMenu, drawMenu, aiMenu;
    UndoManager undo = new UndoManager();

    MenuItem newFile, openFile, saveFile, saveAsFile, exit;
    MenuItem cut, copy, paste, selectAll, clearText, undoBtn, redoBtn;
    MenuItem wrap, incFont, decFont, darkMode, lightMode;
    MenuItem clearDraw, saveDraw, summarize;
    MenuItem arial, courier, timesNR;
    MenuItem red, blue, green, black;
    MenuItem smallBrush, mediumBrush, largeBrush, eraserOn, eraserOff;

    JScrollPane jsp;
    JSplitPane split;
    DrawArea drawPanel;

    boolean isWrapped = false;
    int fontSize = 20;

    String currentFile = null;

    MyNotePad(String title) {
        super(title);

        mb = new MenuBar();

        fileMenu = new Menu("File");
        editMenu = new Menu("Edit");
        viewMenu = new Menu("View");
        fontMenu = new Menu("Font Style");
        colorMenu = new Menu("Text Color");
        drawMenu = new Menu("Drawing Tools");
        aiMenu = new Menu("AI");

        newFile = new MenuItem("New");
        openFile = new MenuItem("Open");
        saveFile = new MenuItem("Save");
        saveAsFile = new MenuItem("Save As");
        exit = new MenuItem("Exit");

        cut = new MenuItem("Cut");
        copy = new MenuItem("Copy");
        paste = new MenuItem("Paste");
        selectAll = new MenuItem("Select All");
        clearText = new MenuItem("Clear Text");
        undoBtn = new MenuItem("Undo");
        redoBtn = new MenuItem("Redo");

        wrap = new MenuItem("Word Wrap");
        incFont = new MenuItem("Increase Font");
        decFont = new MenuItem("Decrease Font");
        darkMode = new MenuItem("Dark Mode");
        lightMode = new MenuItem("Light Mode");

        clearDraw = new MenuItem("Clear Drawing");
        saveDraw = new MenuItem("Save Drawing");

        summarize = new MenuItem("Summarize Text");

        arial = new MenuItem("Arial");
        courier = new MenuItem("Courier New");
        timesNR = new MenuItem("Times New Roman");

        red = new MenuItem("Red");
        blue = new MenuItem("Blue");
        green = new MenuItem("Green");
        black = new MenuItem("Black");

        smallBrush = new MenuItem("Small Brush");
        mediumBrush = new MenuItem("Medium Brush");
        largeBrush = new MenuItem("Large Brush");
        eraserOn = new MenuItem("Eraser ON");
        eraserOff = new MenuItem("Eraser OFF");

        // add AI listener
        summarize.addActionListener(this);

        MenuItem[] allItems = { newFile, openFile, saveFile, saveAsFile, exit, cut, copy, paste,
                selectAll, clearText, undoBtn, redoBtn, wrap, incFont, decFont,
                darkMode, lightMode, clearDraw, saveDraw, arial, courier, timesNR,
                red, blue, green, black, smallBrush, mediumBrush, largeBrush, eraserOn, eraserOff };

        for (MenuItem m : allItems) m.addActionListener(this);

        // add menu items
        fileMenu.add(newFile); fileMenu.add(openFile); fileMenu.add(saveFile); fileMenu.add(saveAsFile); fileMenu.add(exit);
        editMenu.add(cut); editMenu.add(copy); editMenu.add(paste); editMenu.add(selectAll);
        editMenu.add(clearText); editMenu.add(undoBtn); editMenu.add(redoBtn);
        viewMenu.add(wrap); viewMenu.add(incFont); viewMenu.add(decFont);
        viewMenu.add(darkMode); viewMenu.add(lightMode); viewMenu.add(clearDraw); viewMenu.add(saveDraw);
        fontMenu.add(arial); fontMenu.add(courier); fontMenu.add(timesNR);
        colorMenu.add(red); colorMenu.add(blue); colorMenu.add(green); colorMenu.add(black);
        drawMenu.add(smallBrush); drawMenu.add(mediumBrush); drawMenu.add(largeBrush);
        drawMenu.add(eraserOn); drawMenu.add(eraserOff);
        aiMenu.add(summarize);

        mb.add(fileMenu); mb.add(editMenu); mb.add(viewMenu);
        mb.add(fontMenu); mb.add(colorMenu); mb.add(drawMenu); mb.add(aiMenu);

        setMenuBar(mb);

        jta = new JTextArea();
        jta.setFont(new Font("Arial", Font.PLAIN, fontSize));
        jta.getDocument().addUndoableEditListener(undo);

        jsp = new JScrollPane(jta);
        drawPanel = new DrawArea();

        split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, jsp, drawPanel);
        split.setDividerLocation(900);
        add(split);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }


    void saveToFile(String path) {
        try {
            FileWriter fw = new FileWriter(path);
            fw.write(jta.getText());
            fw.close();
            currentFile = path;
            setTitle("Notepad - " + path);
        } catch (Exception e) {}
    }


    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();

        try {
            switch (cmd) {

                case "Summarize Text":
                    String selected = jta.getSelectedText();
                    if (selected == null || selected.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Select text first!");
                        break;
                    }
                    JOptionPane.showMessageDialog(this, "AI Working...");
                    String summary = SimpleAI.summarize(selected);
                    JOptionPane.showMessageDialog(this, "Summary:\n" + summary);
                    break;

                case "New": jta.setText(""); currentFile = null; break;

                case "Open":
                    FileDialog open = new FileDialog(this, "Open", FileDialog.LOAD);
                    open.setVisible(true);
                    if (open.getFile() != null)
                        jta.read(new FileReader(open.getDirectory() + open.getFile()), null);
                    break;

                case "Save":
                    if (currentFile == null) {
                        FileDialog save = new FileDialog(this, "Save", FileDialog.SAVE);
                        save.setVisible(true);
                        if (save.getFile() != null)
                            saveToFile(save.getDirectory() + save.getFile() + ".txt");
                    } else saveToFile(currentFile);
                    break;

                case "Save As":
                    FileDialog saveAs = new FileDialog(this, "Save As", FileDialog.SAVE);
                    saveAs.setVisible(true);
                    if (saveAs.getFile() != null)
                        saveToFile(saveAs.getDirectory() + saveAs.getFile() + ".txt");
                    break;

                case "Cut": jta.cut(); break;
                case "Copy": jta.copy(); break;
                case "Paste": jta.paste(); break;
                case "Select All": jta.selectAll(); break;
                case "Clear Text": jta.setText(""); break;

                case "Undo": if (undo.canUndo()) undo.undo(); break;
                case "Redo": if (undo.canRedo()) undo.redo(); break;

                case "Word Wrap": isWrapped = !isWrapped; jta.setLineWrap(isWrapped); break;

                case "Increase Font": jta.setFont(new Font(jta.getFont().getName(), Font.PLAIN, ++fontSize)); break;
                case "Decrease Font": if (fontSize > 6) jta.setFont(new Font(jta.getFont().getName(), Font.PLAIN, --fontSize)); break;

                case "Dark Mode":
                    jta.setBackground(Color.BLACK); jta.setForeground(Color.WHITE);
                    drawPanel.setBackground(Color.DARK_GRAY);
                    break;

                case "Light Mode":
                    jta.setBackground(Color.WHITE); jta.setForeground(Color.BLACK);
                    drawPanel.setBackground(Color.WHITE);
                    break;

                case "Arial": jta.setFont(new Font("Arial", Font.PLAIN, fontSize)); break;
                case "Courier New": jta.setFont(new Font("Courier New", Font.PLAIN, fontSize)); break;
                case "Times New Roman": jta.setFont(new Font("Times New Roman", Font.PLAIN, fontSize)); break;

                case "Red": drawPanel.brushColor = Color.RED; break;
                case "Blue": drawPanel.brushColor = Color.BLUE; break;
                case "Green": drawPanel.brushColor = Color.GREEN; break;
                case "Black": drawPanel.brushColor = Color.BLACK; break;

                case "Small Brush": drawPanel.brushSize = 5; break;
                case "Medium Brush": drawPanel.brushSize = 12; break;
                case "Large Brush": drawPanel.brushSize = 25; break;

                case "Eraser ON": drawPanel.eraseMode = true; break;
                case "Eraser OFF": drawPanel.eraseMode = false; break;

                case "Clear Drawing": drawPanel.clear(); break;

                case "Save Drawing":
                    FileDialog drawSave = new FileDialog(this, "Save Drawing", FileDialog.SAVE);
                    drawSave.setVisible(true);
                    if (drawSave.getFile() != null)
                        drawPanel.saveImage(new File(drawSave.getDirectory() + drawSave.getFile() + ".png"));
                    break;

                case "Exit": System.exit(0); break;
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}


// ====================== MAIN =========================
public class NotePad {
    public static void main(String[] args) {
        MyNotePad np = new MyNotePad("AI Notepad + Drawing");
        np.setVisible(true);
        np.setSize(1400, 700);
        np.setLocation(50, 50);
    }
}
