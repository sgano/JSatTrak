/*
 * Copyright 2007 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Sun Microsystems nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.java.joglutils.msg.test;

import java.awt.image.*;

/** 
 * Defines how elements in the ListModel associated with the
 * ImageBrowser are converted into images that can be rendered
 * on-screen. <P>
 * 
 * The IDENT type is the client's identifier for a particular image
 * which is used in operations like progress callbacks. For example,
 * this might be the index into the ListModel of the image we're
 * talking about. <P>
 *
 * @author Jasper Potts
 * @author Kenneth Russell
 * @author Richard Bair
 */

public interface Fetcher<IDENT> {
    /** Requests the particular image associated with the given
        element (of arbitrary type) from the ListModel of the
        ImageBrowser. The <CODE>requestedImageSize</CODE> parameter
        indicates the desired maximum dimension (width or height) of
        the returned image. Passing a negative number for this
        parameter results in always fetching the full-size image.
        Fetchers that perform their work asynchronously will return
        null if the image is not available immediately. In this case,
        progress callbacks will be fired promptly to allow the caller
        to display an indication of download progress. */
    public BufferedImage getImage(Object imageDescriptor,
                                  IDENT clientIdentifier,
                                  int requestedImageSize);

    /** Cancels a previously-registered download request from this
        Fetcher -- one which resulted in null being returned from
        getImage(). */
    public void cancelDownload(Object imageDescriptor,
                               IDENT clientIdentifier,
                               int requestedImageSize);

    /** Adds a progress listener to this Fetcher. */
    public void addProgressListener(ProgressListener listener);

    /** Removes a progress listener from this Fetcher. */
    public void removeProgressListener(ProgressListener listener);
}
