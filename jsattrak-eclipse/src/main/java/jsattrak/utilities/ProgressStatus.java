/*
 * ProgressStatus.java
 *
 * An Object to hold the current progress and status for a JProgressBar
 *
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
 *
 * Created on 22 March 2009
 */
package jsattrak.utilities;

/**
 *
 * @author Shawn E. Gano
 */
public class ProgressStatus
{
    private int percentComplete = 0;
    private String statusText = "";

    /**
     * Default ProgressStatus object, intialized to 0 percent and no statusText
     */
    public ProgressStatus()
    {
    }

    /**
     * Creates a Progress Status initialized to the input parameters
     * @param percentComplete
     * @param statusText
     */
    public ProgressStatus(int percentComplete, String statusText)
    {
        this.percentComplete = percentComplete;
        this.statusText = statusText;
    }

    /**
     * @return the percentComplete
     */
    public int getPercentComplete()
    {
        return percentComplete;
    }

    /**
     * @param percentComplete the percentComplete to set
     */
    public void setPercentComplete(int percentComplete)
    {
        this.percentComplete = percentComplete;
    }

    /**
     * @return the statusText
     */
    public String getStatusText()
    {
        return statusText;
    }

    /**
     * @param statusText the statusText to set
     */
    public void setStatusText(String statusText)
    {
        this.statusText = statusText;
    }

}
