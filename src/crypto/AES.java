package crypto;

import java.util.Arrays;

public class AES {

    private int[][][] state;

    private int[][][] expandedKey;

    private int round = 0;

    public static void main(String args[]) {
        AES sys = new AES(new int[][]{{1, 2, 3, 4}, {5, 6, 7, 8}, {9, 10, 11, 12}, {13, 14, 15, 16}}, "yellow submarine");
        System.out.println(sys.digest());
    }

    public AES(int[][] key, String message) {
        int blocks = message.length() / 16 + 1;
        state = new int[blocks][4][4];
        int index = 0;
        for (int i = 0; i < blocks; i++)
            for (int j = 0; j < 4; j++)
                for (int k = 0; k < 4; k++) {
                    state[i][j][k] = (index < message.length()) ? (int) message.charAt(index) : 0;
                    index++;
                }
        for (int k = 0; k < state.length; k++) {
            int[][] tState = new int[4][4];
            for (int i = 0; i < 4; i++) {
                int[] col = new int[4];
                for (int j = 0; j < 4; j++)
                    tState[i][j] = state[k][j][i];
            }
            state[k] = tState;
        }

        //Transpose key matrix
        int[][] subkey = new int[4][4];
        for (int i = 0; i < 4; i++) {
            int[] col = new int[4];
            for (int j = 0; j < 4; j++)
                subkey[i][j] = key[j][i];
        }
        keySchedule(key);
    }

    private void keySchedule(int[][] key) {

        expandedKey = new int[12][4][4];

        expandedKey[0] = key;

        for (int iter = 0; iter < 11; iter++) {
            int[] t = new int[4];
            for (int i = 0; i < 4; i++)
                t[i] = Tables.sBox[key[3][(i + 1) % 4]];
            t[0] = t[0] ^ Tables.rCon[iter];

            for (int j = 0; j < 4; j++) {
                for (int i = 0; i < 4; i++) {
                    t[i] = t[i] ^ expandedKey[iter][j][i];
                }
                expandedKey[iter + 1][j] = t;
            }
        }
    }

    public void addRoundKey() {
        for (int[][] block : state) {
            for (int i = 0; i < 4; i++)
                for (int j = 0; j < 4; j++)
                    block[i][j] = block[i][j] ^ expandedKey[round][i][j];
        }
    }

    private void substitute() {
        //substitution
        for (int[][] block : state)
            for (int[] row : block)
                for (int i = 0; i < 4; i++) {
                    row[i] = Tables.sBox[row[i]];
                }
    }

    private void invSub() {
        //substitution
        for (int[][] block : state)
            for (int[] row : block)
                for (int i = 0; i < 4; i++) {
                    row[i] = Tables.invSBox[row[i]];
                }
    }

    private void shift() {
        for (int k = 0; k < state.length; k++) {
            int[][] tState = new int[4][4];
            for (int i = 0; i < 4; i++) {
                int[] col = new int[4];
                for (int j = 0; j < 4; j++)
                    tState[i][j] = state[k][j][i];
            }
            state[k] = tState;
        }

        for (int[][] block : state) {
            for (int row = 1; row < 4; row++) {
                int[] newRow = new int[4];
                for (int i = 0; i < 4; i++) {
                    newRow[i] = block[row][(row + i) % 4];
                }
                block[row] = newRow;
            }
        }

        for (int k = 0; k < state.length; k++) {
            int[][] tState = new int[4][4];
            for (int i = 0; i < 4; i++) {
                int[] col = new int[4];
                for (int j = 0; j < 4; j++)
                    tState[i][j] = state[k][j][i];
            }
            state[k] = tState;
        }
    }

    private void round() {
        substitute();
        shift();
        mixColumns();
        addRoundKey();
    }

    public void mixColumns() {
        for (int n = 0; n < state.length; n++) {
            int[][] arr = state[n];
            int[][] temp = new int[4][4];
            for (int i = 0; i < 4; i++) {
                System.arraycopy(arr[i], 0, temp[i], 0, 4);
            }
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    arr[i][j] = colHelper(temp, Tables.galois, i, j);
                }
            }
        }
    }

    private int colHelper(int[][] arr, int[][] g, int i, int j) {
        int colsum = 0;
        for (int k = 0; k < 4; k++) {
            int a = g[i][k];
            int b = arr[k][j];
            colsum ^= colCalc(a, b);
        }
        return colsum;
    }

    private int colCalc(int a, int b) //Helper method for mcHelper
    {
        if (a == 1) {
            return b;
        } else if (a == 2) {
            return Tables.mc2[b / 16][b % 16];
        } else if (a == 3) {
            return Tables.mc3[b / 16][b % 16];
        }
        return 0;
    }

    public String digest() {
        for (round = 0; round < 11; round++) {
            round();
        }
        StringBuilder sb = new StringBuilder();
        for (int[][] block : state)
            for (int[] row : block)
                for (int c : row)
                    sb.append((char) c);

        return sb.toString();
    }


}
