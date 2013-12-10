/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

// Jacobi iteration on a mesh. Based loosely on a Filaments demo

import java.util.concurrent.*;

public class jacobi2 {

    //    static final int DEFAULT_GRANULARITY = 4096;
    static final int DEFAULT_GRANULARITY = 9;

    /**
     * The maximum number of matrix cells
     * at which to stop recursing down and instead directly update.
     */
    static final double EPSILON = 0.0001;  // convergence criterion

    public static void main(String[] args) throws Exception {
        int n = 2048;
        int steps = 1000;
        int granularity = DEFAULT_GRANULARITY;

        try {
            if (args.length > 0)
                n = Integer.parseInt(args[0]);
            if (args.length > 1)
                steps = Integer.parseInt(args[1]);
            if (args.length > 2)
                granularity = Integer.parseInt(args[2]);
        }

        catch (Exception e) {
            System.out.println("Usage: java FJJacobi <matrix size> <max steps> [<leafcells>]");
            return;
        }
//read from a 
//write to b
        ForkJoinPool fjp = new ForkJoinPool();

        // allocate enough space for edges
        //int dim = n+2; //2048 + 2
        int row = 400; 
        int col = 1600;
        //int ncells = dim * dim;
        AlloyObj[][] a = new AlloyObj[row][col];
        AlloyObj[][] b = new AlloyObj[row][col];
        // Initialize interiors to small value
       // AlloyObj smallVal = EPSILON; // 1.0/dim;
       AlloyObj alloy = new AlloyObj();
       alloy.temp =0;
        for (int i = 1; i < row -1; ++i){
            for (int j = 1; j < col -1; ++j){
                a[i][j] = alloy;
             }
        }
        //set the top right and the bottom left 
        alloy.temp = 300;
        a[1][1] = alloy; 
        a[row -1][col-1] = alloy;

        // Fill all edges with -1's.
        alloy.temp = -1;
        for (int k = 0; k < row; ++k) {
            a[k][0] = alloy; //down 1st col
            a[k][col-1] = alloy;//down last col
            b[k][0] = alloy;
            b[k][col-1] = alloy;
        }
        for(int m =0; m < col;m++){
            a[0][m] = alloy; // across 1st row
            a[row-1][m] = alloy;//across last row
            b[0][m] = alloy;
            b[row-1][m] = alloy;
        }
        int nreps = 10;
        for (int rep = 0; rep < nreps; ++rep) {
            //row anc col may need to be -1
            Driver driver = new Driver(a, b, 1, row-1, 1, col-1, steps, granularity);

            long startTime = System.currentTimeMillis();
            fjp.invoke(driver);//what up here? 

            long time = System.currentTimeMillis() - startTime;
            double secs = ((double)time) / 1000.0;

            System.out.println("Compute Time: " + secs);
            System.out.println(fjp);
        }
    }


    abstract static class MatrixTree extends RecursiveAction {
        // maximum difference between old and new values
        AlloyObj maxDiff;
        public final AlloyObj directCompute() {
            compute();
            return maxDiff;
        }
        public final void joinAndReinitialize() {
            if (tryUnfork())
                compute();
            else {
                quietlyJoin();
                reinitialize();
            }
        }

    }


    static final class LeafNode extends MatrixTree {
        final AlloyObj[][] A; // matrix to get old values from
        final AlloyObj[][] B; // matrix to put new values into
        double TC1,TC2,TC3;
        double sum1,sum2,sum3;
        double tempAvg1,tempAvg2,tempAvg3;
        long metalTemp;

        // indices of current submatrix
        final int loRow;    final int hiRow;
        final int loCol;    final int hiCol;

        int steps = 0; // track even/odd steps

        LeafNode(AlloyObj[][] A, AlloyObj[][] B,
                 int loRow, int hiRow,
                 int loCol, int hiCol) {
            this.A = A;   this.B = B;
            this.loRow = loRow; this.hiRow = hiRow;
            this.loCol = loCol; this.hiCol = hiCol;
        }

        public void compute() {
            boolean AtoB = (steps++ & 1) == 0;
            AlloyObj[][] a = AtoB ? A : B;
            AlloyObj[][] b = AtoB ? B : A;


            for (int i = loRow; i <= hiRow; ++i) {
                for (int j = loCol; j <= hiCol; ++j) {
                    //middle value is this floating in space? 
                       AlloyObj mid = a[i][j];
                        //top 
                        AlloyObj north = a[i+1][j];    

                        //left
                        AlloyObj west = a[i][j -1];    

                        //right
                        AlloyObj east = a[i][j+1]; 

                        //bottom
                        AlloyObj south = a[i -1][j];   

                       AlloyObj[] neighbors = new AlloyObj[4];
                       neighbors[0] = north; 
                       neighbors[1] = south; 
                       neighbors[2] = east; 
                       neighbors[3] = west; 

                    //do math on that leaf -- place to do forumal
                     //for each metels heat constant 
                     for(int k =0; k <=2;k++){
                         //get the heat constant 
                       //only for one metal at a time
                       //going to hard code it but why is it not getting TC?
                       //TC1 = mid.m1.getMetalTC();
                      TC1 = .75;
                     if(k == 1){
                     // TC2 = mid.m2.getMetalTC(); 
                     TC2 = 1.00; 
                     }else if(k ==2){
                      //TC3 = mid.m3.getMetalTC();
                      TC3 = 1.25;
                     }  
                            //for each neighbor around the metal
                            for(int l=0; l < neighbors.length;l++){
                                //if the neighbor is not a plate 
                                if(neighbors[l].temp > -1){

                                //get the neighboring regions metal temprature
                                metalTemp = neighbors[l].temp;
                                    
                                //get the percent of metal in neighbor
                                sum1 += neighbors[l].m1P * metalTemp;
                                sum2 += neighbors[l].m2P * metalTemp;
                                sum3 += neighbors[l].m3P * metalTemp;

                                 }//end if 
                              }//end for
                     }//end for 
                            //divide by the number of neighbors absolute N
                                tempAvg1 = sum1/neighbors.length;
                                tempAvg2 = sum2/neighbors.length;
                                tempAvg3 = sum3/neighbors.length;

                                //averge temp plue 
                                //not sure if this is supposed to be added or multiplied
                         double theTemp = (tempAvg1*TC1)+(tempAvg2 * TC2)+(tempAvg3 * TC3);
          
                }
            }

        }
    }

    static final class FourNode extends MatrixTree {
        final MatrixTree q1;
        final MatrixTree q2;
        final MatrixTree q3;
        final MatrixTree q4;
        FourNode(MatrixTree q1, MatrixTree q2,
                 MatrixTree q3, MatrixTree q4) {
            this.q1 = q1; this.q2 = q2; this.q3 = q3; this.q4 = q4;
        }

        public void compute() {
            q4.fork();
            q3.fork();
            q2.fork();
            AlloyObj md = q1.directCompute();
            q2.joinAndReinitialize();
            q3.joinAndReinitialize();
            q4.joinAndReinitialize();
        }
    }


    static final class TwoNode extends MatrixTree {
        final MatrixTree q1;
        final MatrixTree q2;

        TwoNode(MatrixTree q1, MatrixTree q2) {
            this.q1 = q1; this.q2 = q2;
        }

        public void compute() {
            q2.fork();
            q1.directCompute();
            q2.joinAndReinitialize();
        }

    }


    static final class Driver extends RecursiveAction {
        MatrixTree mat;
        AlloyObj[][] A; AlloyObj[][] B;
        int firstRow; int lastRow;
        int firstCol; int lastCol;
        final int steps;
        final int leafs;
        int nleaf;

        Driver(AlloyObj[][] A, AlloyObj[][] B,
               int firstRow, int lastRow,
               int firstCol, int lastCol,
               int steps, int leafs) {
            this.A = A;
            this.B = B;
            this.firstRow = firstRow;
            this.firstCol = firstCol;
            this.lastRow = lastRow;
            this.lastCol = lastCol;
            this.steps = steps;
            this.leafs = leafs;
            mat = build(A, B, firstRow, lastRow, firstCol, lastCol, leafs);
            System.out.println("Using " + nleaf + " segments");

        }

        MatrixTree build(AlloyObj[][] a, AlloyObj[][] b,
                         int lr, int hr, int lc, int hc, int leafs) {
            int rows = (hr - lr + 1);
            int cols = (hc - lc + 1);

            int mr = (lr + hr) >>> 1; // midpoints
            int mc = (lc + hc) >>> 1;

            int hrows = (mr - lr + 1);
            int hcols = (mc - lc + 1);

            if (rows * cols <= leafs) {
                ++nleaf;
                return new LeafNode(a, b, lr, hr, lc, hc);
            }
            else if (hrows * hcols >= leafs) {
                return new FourNode(build(a, b, lr,   mr, lc,   mc, leafs),
                                    build(a, b, lr,   mr, mc+1, hc, leafs),
                                    build(a, b, mr+1, hr, lc,   mc, leafs),
                                    build(a, b, mr+1, hr, mc+1, hc, leafs));
            }
            else if (cols >= rows) {
                return new TwoNode(build(a, b, lr, hr, lc,   mc, leafs),
                                   build(a, b, lr, hr, mc+1, hc, leafs));
            }
            else {
                return new TwoNode(build(a, b, lr,   mr, lc, hc, leafs),
                                   build(a, b, mr+1, hr, lc, hc, leafs));

            }
        }

        static void doCompute(MatrixTree m, int s) {
            for (int i = 0; i < s; ++i) {
                m.invoke();
                m.reinitialize();
            }
        }
            //compute for driver may be broken
        public void compute() {
            doCompute(mat, steps);
            AlloyObj md = mat.maxDiff;
            System.out.println("max diff after " + steps + " steps = " + md);
        }
    }//end driver
}
