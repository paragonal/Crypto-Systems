package crypto;

import java.util.Arrays;

public class AES {

    private int[][][] state;

    private int[][] key;

    public static void main(String args[]) {
        AES sys = new AES(new int[][]{{1, 2, 3, 4}, {5, 6, 7, 8}, {9, 10, 11, 12}, {13, 14, 15, 16}}, "Hello there this is a poem, maybe it isn't who really knows?");
        System.out.println(sys.digest());
    }

    public AES(int[][] key, String message) {
        this.key = key;
        int blocks = message.length() / 16 + 1;
        state = new int[blocks][4][4];
        int index = 0;
        for (int i = 0; i < blocks; i++)
            for (int j = 0; j < 4; j++)
                for (int k = 0; k < 4; k++) {
                    state[i][j][k] = (index < message.length()) ? (int) message.charAt(index) : 0;
                    index++;
                }
        keySchedule();
    }

    public void keySchedule() {
        int[][] subkey = new int[4][4];
        for (int i = 0; i < 4; i++) {
            int[] col = new int[4];
            for (int j = 0; j < 4; j++)
                subkey[i][j] = key[j][i];
        }
        System.out.println(Arrays.deepToString(key));
        System.out.println(Arrays.deepToString(subkey));

        int ind = 0;
        int[][] temp = new int[4][4];
        for(int[] col : subkey){
            for(int i = 0; i < 4; i++)
                temp[ind][i] = col[(i+2)%4];
            ind++;
        }
        subkey = temp;
        System.out.println(Arrays.deepToString(subkey));


    }

    public void addRoundKey() {
        for (int[][] block : state) {
            for (int i = 0; i < 4; i++)
                for (int j = 0; j < 4; j++)
                    block[i][j] = block[i][j] ^ key[i][j];
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

    private void shift() {
        for (int[][] block : state) {
            for (int row = 1; row < 4; row++) {
                int[] newRow = new int[4];
                for (int i = 0; i < 4; i++) {
                    newRow[i] = block[row][(row + i) % 4];
                }
                block[row] = newRow;

            }
        }
    }

    private void round() {
        substitute();
        shift();
        mixColumns();


        //TODO some weird block chain key shit


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

        for (int i = 0; i < 10; i++) {
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
