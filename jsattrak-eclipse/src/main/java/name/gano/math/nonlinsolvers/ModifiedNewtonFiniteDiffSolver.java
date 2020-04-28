/*
 *  Newton-Raphson method in n-D solve n equations m variables 
 *  Uses finite differencing to calculate derivatives (Jacobian)
 *  - matrix inverse uses psudeo inverse (SVD)
 *  - if set - uses a check after the newton step to insure the error has improved otherwise, step lenght is reduced until progress is made (insures better step every iteration)
 *  - Shawn E. Gano, 17 Jan 2007
 * =====================================================================
 *   This file is part of JSatTrak.
 *
 *   Copyright 2007-2013 Shawn E. Gano
 *   
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *   
 *       http://www.apache.org/licenses/LICENSE-2.0
 *   
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * =====================================================================
 */

package name.gano.math.nonlinsolvers;

import Jama.Matrix;
import name.gano.astro.MathUtils;

/**
 *
 * @author sgano
 */
public class ModifiedNewtonFiniteDiffSolver extends NonLinearEquationSystemSolver
{
    // options
    int maxIter = 25; 
    
    double[] fGoals; 
    double[] X;
    
    double[] dx; // perturbing values
    double defaultDx = 0.0001;
    double tol = 1e-8; // convergence tolerance
    
    // step size reduction of newton step
    private static final double PHI = 10.0;//(1.0+Math.sqrt(5.0))/2.0; // golden ratio, use in reducing Newton Step Size
    
     // problem function
    NonLinearEquationSystemProblem func;
    
    int funcEval = 0;
    
    boolean verbose = false;
    
    boolean useModifiedNewtonStep = true; // if a step results in a worse error, reduce the newton step size until progress is made, quit after size of tol reached no better solution
    boolean solverConverged = false;
    
    String outputMessage = "";
    private double finalError = 0;
    
    public ModifiedNewtonFiniteDiffSolver(NonLinearEquationSystemProblem func, double[] fGoals, double[] X0)
    {
        this.func = func;
        this.X = X0.clone(); // clone this value as the internal value will change
        this.fGoals = fGoals;
        
        // since not speificied, defualt dx
        dx = new double[X.length];
        for(int i=0;i<X.length;i++)
        {
            dx[i] = defaultDx;
        }
        
        
    }
    
    //  returns if converged -- might want to eval F at the end.. to see if it is a better point (and half step to see if that is better etc.)
    public boolean solve()
    {
        solverConverged = false; // reset 
        
        if (verbose)
        {
            System.out.println("Iter)  x1, x2, ... | RMS error | func evals");
            System.out.println("=======================================");
        }
        
        funcEval = 0;
        
        // step 1: eval f at current point and goal array
        double[] fCurrent = func.evaluateSystemOfEquations(X);
        funcEval++;
        
        // step 1b: create goal measure array
        double[] goalArray = new double[fCurrent.length];
        for(int i=0;i<fCurrent.length;i++)
        {
            goalArray[i] = fGoals[i] - fCurrent[i];
        }
        double errorOld = MathUtils.norm(goalArray);
        double error = 0;  // used to hold current error
        
        if (verbose)
        {
            printIterationDateLine(0, errorOld, false);
        }
        
        
        int iterCount = 1;
        boolean modStepUsed = false; // a flag to see if a modified step was used this iteration
        main_loop: while (iterCount <= maxIter && errorOld > tol) // labled for breaking purposes
        {
            modStepUsed = false;
        
            // setp 2: calculate approximate Jacobian using finite differencing
            double[][] jac = new double[fCurrent.length][X.length];

            for (int i = 0; i < X.length; i++)
            {
                // create new perturbed design vector
                double[] Xpert = X.clone();
                Xpert[i] += dx[i];

                // eval at this new point
                double[] ftemp = func.evaluateSystemOfEquations(Xpert);
                funcEval++;

                // calculate part of the Jacobian
                for (int j = 0; j < fCurrent.length; j++)
                {
                    jac[j][i] = (ftemp[j] - fCurrent[j]) / dx[i];
                }
            } // calc jacobian

            // step 3: calculate next iteration point (Newton Step)

            Matrix jacMatrix = new Matrix(jac);
            Matrix jacInvMatrix = jacMatrix.inverse(); // psudeu inverse (unless square)

            Matrix goalMat = new Matrix(goalArray, goalArray.length); // vertical matrix
            Matrix xOld = new Matrix(X, X.length); // verticle matrix 
            
            // save in case we need it in modified step
            double prevError = error;

            // next step = x + jac^-1 * goal
            Matrix step = jacInvMatrix.times(goalMat);
            Matrix newX = step.plus(xOld);

            // copy result back to X
            
            for (int i = 0; i < X.length; i++)
            {
                X[i] = newX.get(i, 0);
            }

            // step 1: eval f at current point
            fCurrent = func.evaluateSystemOfEquations(X);
            funcEval++;
            
            // step 1b: create goal measure array
            for (int i = 0; i < fCurrent.length; i++)
            {
                goalArray[i] = fGoals[i] - fCurrent[i];
            }
            
            error = MathUtils.norm(goalArray);
            
            
            // Modified Newton Step Checking ==============
            if(useModifiedNewtonStep)
            {
                double newtonStepScale = 1.0;
                
                if(error > errorOld)
                {
                    modStepUsed = true; // mod step used
                }
                
                while(error > errorOld  && newtonStepScale > tol)
                {
                    //System.out.println("Modified Step Used; error was:" + error);
                    
                    newtonStepScale = newtonStepScale / PHI; // reduce step size
                    
                    // take new step
                     newX = step.times(newtonStepScale).plus(xOld);

                    // copy result back to X
                    for (int i = 0; i < X.length; i++)
                    {
                        X[i] = newX.get(i, 0);
                    }

                    // step 1: eval f at current point
                    fCurrent = func.evaluateSystemOfEquations(X);
                    funcEval++;

                    // step 1b: create goal measure array
                    for (int i = 0; i < fCurrent.length; i++)
                    {
                        goalArray[i] = fGoals[i] - fCurrent[i];
                    }

                    error = MathUtils.norm(goalArray);
                    
                } // modified step iteration loop
                
                if(newtonStepScale <= tol || error == errorOld)
                {
                    // point can't be improved -- end now (somehow and set message to "solution couldn't be improved";
                    outputMessage = "Solution could not be improved.";
                    // copy old X back to current point
                    for (int i = 0; i < X.length; i++)
                    {
                        X[i] = xOld.get(i, 0);
                    }
                    error = prevError;
                    printIterationDateLine(iterCount, error, modStepUsed);
                    break main_loop;
                }
                
            } // modified newton step
            // ==========================================
            errorOld = error; // save current error to old one

            
            // print out iteration results
            if (verbose)
            {
                printIterationDateLine(iterCount, error, modStepUsed);
            }
            
            
            iterCount++;
        } // iteration loop
        
       
        if(iterCount > maxIter && errorOld > tol)
        {
            outputMessage = "Did not converge to tolerance in " + maxIter + " iterations.";
        }
        else if(errorOld <= tol)
        {
            outputMessage = "Solution converged successfully.";
            solverConverged = true;
        }
        
        finalError = error;
        
        return solverConverged;
    }
    
    private void printIterationDateLine(int iterCount, double error, boolean modStepUsed)
    {
        System.out.print(iterCount + ")  ");
        for (int i = 0; i < X.length; i++)
        {
            System.out.print(X[i] + "     ");
        }
        System.out.print("  |  " + error + " | " + funcEval);
        
        if(modStepUsed)
        {
            System.out.print(" (Modified Step(s) Used)");
        }
        
        System.out.print("\n");
    }

    
  // ----- get and set methods ---------------
    
    public double[] getX()
    {
        return X;
    }

    public void setX(double[] X)
    {
        this.X = X;
    }

    public double[] getDx()
    {
        return dx;
    }

    public void setDx(double[] dx)
    {
        this.dx = dx;
    }

    public double[] getFGoals()
    {
        return fGoals;
    }

    public void setFGoals(double[] fGoals)
    {
        this.fGoals = fGoals;
    }

    public int getFuncEval()
    {
        return funcEval;
    }

    public int getMaxIter()
    {
        return maxIter;
    }

    public void setMaxIter(int maxIter)
    {
        this.maxIter = maxIter;
    }

    public String getOutputMessage()
    {
        return outputMessage;
    }

    public boolean isSolverConverged()
    {
        return solverConverged;
    }

    
    public double getTol()
    {
        return tol;
    }

    public void setTol(double tol)
    {
        this.tol = tol;
    }

    public boolean isUseModifiedNewtonStep()
    {
        return useModifiedNewtonStep;
    }

    public void setUseModifiedNewtonStep(boolean useModifiedNewtonStep)
    {
        this.useModifiedNewtonStep = useModifiedNewtonStep;
    }

    public boolean isVerbose()
    {
        return verbose;
    }

    public void setVerbose(boolean verbose)
    {
        this.verbose = verbose;
    }
    
    public double getFinalError()
    {
        return finalError;
    }
    
}
