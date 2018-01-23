package crypto;

import java.util.Arrays;

public class AES {
	
    private int[][][] state;

    private int[] key;

    public static void main(String args[]) {
        AES sys = new AES(new int[]{1, 2, 3, 2, 123, 1, 1, 23}, "Hello there this is a poem, maybe it isn't who really knows?");
        System.out.println(sys.digest());
    }

    public AES(int[] key, String message) {
        this.key = key;
        int blocks = message.length() / 16 + 1;
        state = new int[blocks][4][4];
        int index = 0;
        for (int i = 0; i < blocks; i++)
            for (int j = 0; j < 4; j++)
                for (int k = 0; k < 4; k++) {
                    state[i][j][k] = (index < message.length()) ? (int) message.charAt(index) : 255;
                    index++;
                }
    }
    private void substitute(){
        //substitution
        for (int[][] block : state)
            for (int[] row : block)
                for (int i = 0; i < 4; i++)
                    row[i] = Tables.sBox[row[i] - 1];
    }

    private void shift(){
        for(int[][] block : state){
            for(int row = 1; row < 4; row++){
                int[] newRow = new int[4];
                for(int i = 0; i < 4; i ++){
                    newRow[i] = block[row][(row + i) % 4];
                }
                block[row] = newRow;

            }
        }
    }

    private void round() {
        substitute();
        shift();
        //TODO mix columns

        //TODO diffuse

        //TODO some weird block chain key shit


    }

	public void mixColumns(){
		for (int n = 0; n < state.length; n++) {
			int[][] arr = state[n];
			int[][] temp = new int[4][4];
			for (int i = 0; i < 4; i++) {
				//Copies all 4-element arrays from arr to temp.
				System.arraycopy(arr[i], 0, temp[i], 0, 4);
			}
			for (int i = 0; i < 4; i++){
				for (int j = 0; j < 4; j++){
					//Runs colHelper on each element in array.
					arr[i][j] = colHelper(temp, Tables.galois, i, j);
				}
			}
		}
	}

	private int colHelper(int[][] arr, int[][] g, int i, int j) {
		int colsum = 0;
		for (int k = 0; k < 4; k++) {
			//Uses galois table to find specified numbers for each value in array.
			int a = g[i][k];
			int b = arr[k][j];
			//XNORs the value from colCalc to find resulting number.
			colsum ^= colCalc(a, b);
		}
		return colsum;
	}

	private int colCalc(int a, int b) {
    	//Calculates b based on a, using values from table.
		if (a == 1) {
			return b;
		} else if (a == 2) {
			return Tables.mc2[b / 16][b % 16];
		} else if (a == 3) {
			return Tables.mc3[b / 16][b % 16];
		}
		return 0;
	}

	public void invMixColumns() {
    	for (int n = 0; n < state.length; n++) {
    		int[][] arr = state[n];
		    int[][] temp = new int[4][4];
		    for (int i = 0; i < 4; i++) {
			    System.arraycopy(arr[i], 0, temp[i], 0, 4);
		    }
		    for (int i = 0; i < 4; i++) {
			    for (int j = 0; j < 4; j++) {
				    arr[i][j] = invColHelper(temp, Tables.invgalois, i, j);
			    }
		    }
	    }
	}

	private int invColHelper(int[][] arr, int[][] igalois, int i, int j){
		int colsum = 0;
		for (int k = 0; k < 4; k++) {
			int a = igalois[i][k];
			int b = arr[k][j];
			colsum ^= invColCalc(a, b);
		}
		return colsum;
	}

	private int invColCalc(int a, int b) //Helper method for invMcHelper
	{
		if (a == 9) {
			return Tables.mc9[b / 16][b % 16];
		} else if (a == 0xb) {
			return Tables.mc11[b / 16][b % 16];
		} else if (a == 0xd) {
			return Tables.mc13[b / 16][b % 16];
		} else if (a == 0xe) {
			return Tables.mc14[b / 16][b % 16];
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
