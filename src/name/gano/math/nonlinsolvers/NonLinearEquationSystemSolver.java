/*
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

/**
 *
 * @author sgano
 */
public abstract class NonLinearEquationSystemSolver {

    public abstract double[] getDx();

    public abstract double[] getFGoals();

    public abstract double getFinalError();

    public abstract int getFuncEval();

    public abstract int getMaxIter();

    public abstract String getOutputMessage();

    public abstract double getTol();

    public abstract double[] getX();

    public abstract boolean isSolverConverged();

    public abstract boolean isVerbose();

    public abstract void setDx(double[] dx);

    public abstract void setFGoals(double[] fGoals);

    public abstract void setMaxIter(int maxIter);

    public abstract void setTol(double tol);

    public abstract void setVerbose(boolean verbose);

    public abstract void setX(double[] X);

    public abstract boolean solve();

}
