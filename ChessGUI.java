// Michael Perrotta
// ChessGUI.java

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ChessGUI {

    JFrame frame;
    JButton[][] buttons = new JButton[8][8];
    JTextArea whiteCapturedArea = new JTextArea();
    JTextArea blackCapturedArea = new JTextArea();

    Game game;

    int selectedRow = -1, selectedCol = -1;

    public ChessGUI() {
        game = new Game();

        frame = new JFrame("Chess");
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        JPanel boardPanel = new JPanel(new GridLayout(8, 8));
        JPanel sidePanel = new JPanel(new GridLayout(2, 1));

        whiteCapturedArea.setBorder(BorderFactory.createTitledBorder("White Captured"));
        blackCapturedArea.setBorder(BorderFactory.createTitledBorder("Black Captured"));

        sidePanel.add(new JScrollPane(whiteCapturedArea));
        sidePanel.add(new JScrollPane(blackCapturedArea));

        initializeBoard(boardPanel);

        frame.add(boardPanel, BorderLayout.CENTER);
        frame.add(sidePanel, BorderLayout.EAST);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        refreshBoard();
    }

    private void initializeBoard(JPanel panel) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                JButton btn = new JButton();
                buttons[r][c] = btn;

                int row = r;
                int col = c;

                btn.addActionListener(e -> handleClick(row, col));

                if ((r + c) % 2 == 0)
                    btn.setBackground(Color.WHITE);
                else
                    btn.setBackground(Color.GRAY);

                panel.add(btn);
            }
        }
    }

    // CLICK LOGIC
    private void handleClick(int r, int c) {

        // FIRST CLICK (select piece)
        if (selectedRow == -1) {
            Piece p = game.board.get(r, c);

            if (p == null) return; // can't select empty
            if (p.isWhite != game.whiteTurn) return; // must be your turn

            selectedRow = r;
            selectedCol = c;
            buttons[r][c].setBackground(Color.YELLOW);
            return;
        }

        // SECOND CLICK (attempt move)
        Piece captured = game.board.move(
            selectedRow, selectedCol, r, c, game.whiteTurn
        );

        if (captured != null || game.board.lastMoveValid) {

            if (captured != null) {
                if (captured.isWhite)
                    game.capturedWhite.add(captured);
                else
                    game.capturedBlack.add(captured);
            }

            game.whiteTurn = !game.whiteTurn;

        } else {
            JOptionPane.showMessageDialog(frame, "Illegal move!");
        }

        selectedRow = -1;
        selectedCol = -1;

        resetColors();
        refreshBoard();
    }

    private void resetColors() {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if ((r + c) % 2 == 0)
                    buttons[r][c].setBackground(Color.WHITE);
                else
                    buttons[r][c].setBackground(Color.GRAY);
            }
        }
    }

    private void refreshBoard() {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = game.board.get(r, c);
                buttons[r][c].setText(p == null ? "" : p.toString());
            }
        }

        whiteCapturedArea.setText(listToString(game.capturedWhite));
        blackCapturedArea.setText(listToString(game.capturedBlack));
    }

    private String listToString(List<Piece> list) {
        StringBuilder sb = new StringBuilder();
        for (Piece p : list) sb.append(p.toString()).append(" ");
        return sb.toString();
    }

    public static void main(String[] args) {
        new ChessGUI();
    }
}

// GAME

class Game {
    Board board = new Board();
    boolean whiteTurn = true;

    List<Piece> capturedWhite = new ArrayList<>();
    List<Piece> capturedBlack = new ArrayList<>();

    public Game() {
        board.initialize();
    }
}

// BOARD

class Board {
    private Piece[][] grid = new Piece[8][8];
    boolean lastMoveValid = false;

    public void initialize() {
        for (int i = 0; i < 8; i++) {
            grid[1][i] = new Pawn(false);
            grid[6][i] = new Pawn(true);
        }

        grid[0][0] = new Rook(false);
        grid[0][7] = new Rook(false);
        grid[7][0] = new Rook(true);
        grid[7][7] = new Rook(true);

        grid[0][1] = new Knight(false);
        grid[0][6] = new Knight(false);
        grid[7][1] = new Knight(true);
        grid[7][6] = new Knight(true);

        grid[0][2] = new Bishop(false);
        grid[0][5] = new Bishop(false);
        grid[7][2] = new Bishop(true);
        grid[7][5] = new Bishop(true);

        grid[0][3] = new Queen(false);
        grid[7][3] = new Queen(true);

        grid[0][4] = new King(false);
        grid[7][4] = new King(true);
    }

    public Piece move(int fr, int fc, int tr, int tc, boolean whiteTurn) {
        lastMoveValid = false;

        Piece p = grid[fr][fc];
        if (p == null || p.isWhite != whiteTurn) return null;

        Piece target = grid[tr][tc];
        if (target != null && target.isWhite == whiteTurn) return null;

        if (p.isValidMove(fr, fc, tr, tc, this)) {
            grid[tr][tc] = p;
            grid[fr][fc] = null;
            lastMoveValid = true;
            return target;
        }

        return null;
    }

    public Piece get(int r, int c) {
        return grid[r][c];
    }
}

// PIECES

abstract class Piece {
    boolean isWhite;

    public Piece(boolean isWhite) {
        this.isWhite = isWhite;
    }

    public abstract boolean isValidMove(int fr, int fc, int tr, int tc, Board b);
}

class Pawn extends Piece {
    public Pawn(boolean isWhite) { super(isWhite); }

    public boolean isValidMove(int fr, int fc, int tr, int tc, Board b) {
        int dir = isWhite ? -1 : 1;

        if (fc == tc && b.get(tr, tc) == null)
            return tr == fr + dir;

        if (Math.abs(fc - tc) == 1 && tr == fr + dir) {
            Piece target = b.get(tr, tc);
            return target != null && target.isWhite != isWhite;
        }

        return false;
    }

    public String toString() { return isWhite ? "P" : "p"; }
}

class Rook extends Piece {
    public Rook(boolean isWhite) { super(isWhite); }

    public boolean isValidMove(int fr, int fc, int tr, int tc, Board b) {
        if (fr != tr && fc != tc) return false;

        int dr = Integer.compare(tr, fr);
        int dc = Integer.compare(tc, fc);

        int r = fr + dr, c = fc + dc;
        while (r != tr || c != tc) {
            if (b.get(r, c) != null) return false;
            r += dr;
            c += dc;
        }
        return true;
    }

    public String toString() { return isWhite ? "R" : "r"; }
}

class Knight extends Piece {
    public Knight(boolean isWhite) { super(isWhite); }

    public boolean isValidMove(int fr, int fc, int tr, int tc, Board b) {
        int dr = Math.abs(fr - tr);
        int dc = Math.abs(fc - tc);
        return (dr == 2 && dc == 1) || (dr == 1 && dc == 2);
    }

    public String toString() { return isWhite ? "N" : "n"; }
}

class Bishop extends Piece {
    public Bishop(boolean isWhite) { super(isWhite); }

    public boolean isValidMove(int fr, int fc, int tr, int tc, Board b) {
        if (Math.abs(fr - tr) != Math.abs(fc - tc)) return false;

        int dr = Integer.compare(tr, fr);
        int dc = Integer.compare(tc, fc);

        int r = fr + dr, c = fc + dc;
        while (r != tr) {
            if (b.get(r, c) != null) return false;
            r += dr;
            c += dc;
        }
        return true;
    }

    public String toString() { return isWhite ? "B" : "b"; }
}

class Queen extends Piece {
    public Queen(boolean isWhite) { super(isWhite); }

    public boolean isValidMove(int fr, int fc, int tr, int tc, Board b) {
        if (fr == tr || fc == tc)
            return new Rook(isWhite).isValidMove(fr, fc, tr, tc, b);

        if (Math.abs(fr - tr) == Math.abs(fc - tc))
            return new Bishop(isWhite).isValidMove(fr, fc, tr, tc, b);

        return false;
    }

    public String toString() { return isWhite ? "Q" : "q"; }
}

class King extends Piece {
    public King(boolean isWhite) { super(isWhite); }

    public boolean isValidMove(int fr, int fc, int tr, int tc, Board b) {
        return Math.abs(fr - tr) <= 1 && Math.abs(fc - tc) <= 1;
    }

    public String toString() { return isWhite ? "K" : "k"; }
}
