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

import java.awt.EventQueue;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import javax.imageio.*;

/**
 * Basic implementation of Fetcher using ImageIO and a single-threaded
 * ExecutorService.
 *
 * @author Kenneth Russell
 */

public class BasicFetcher<IDENT> implements Fetcher<IDENT> {
  private volatile ArrayList<ProgressListener> listeners = new ArrayList<ProgressListener>();
  private Map<FetchKey, Future<BufferedImage>> activeTasks = new HashMap<FetchKey, Future<BufferedImage>>();

  private ExecutorService threadPool;

  public BasicFetcher() {
    threadPool = Executors.newSingleThreadExecutor(new ThreadFactory() {
        public Thread newThread(Runnable r) {
          Thread t = Executors.defaultThreadFactory().newThread(r);
          t.setPriority(Thread.NORM_PRIORITY - 2);
          return t;
        }
      });
  }

  public synchronized BufferedImage getImage(final Object imageDescriptor,
                                             final IDENT clientIdentifier,
                                             final int requestedImageSize) {
    FetchKey key = new FetchKey(imageDescriptor, clientIdentifier, requestedImageSize);
    Future<BufferedImage> result = activeTasks.get(key);
    if (result != null) {
      try {
        return result.get(1, TimeUnit.MILLISECONDS);
      } catch (Exception e) {
        return null;
      }
    }

    FutureTask<BufferedImage> task = new FutureTask<BufferedImage>(new Callable<BufferedImage>() {
      public BufferedImage call() {
        try {
          if (imageDescriptor instanceof File) {
            return ImageIO.read((File) imageDescriptor);
          } else if (imageDescriptor instanceof URL) {
            return ImageIO.read((URL) imageDescriptor);
          } else {
            throw new RuntimeException("Unsupported ImageDescriptorType " + imageDescriptor.getClass().getName());
          }
        } catch (Exception e) {
          System.out.println("Exception loading " + imageDescriptor + ":");
          e.printStackTrace();
          return null;
        } finally {
          fireProgressEnd(new ProgressEvent(BasicFetcher.this,
                                            imageDescriptor,
                                            clientIdentifier,
                                            1.0f,
                                            true));
        }
      }
    });
    activeTasks.put(key, task);
    threadPool.execute(task);
    return null;
  }

  public void cancelDownload(Object imageDescriptor,
                             IDENT clientIdentifier,
                             int requestedImageSize) {
  }

  public synchronized void addProgressListener(ProgressListener listener) {
    ArrayList<ProgressListener> newListeners = (ArrayList<ProgressListener>) listeners.clone();
    newListeners.add(listener);
    listeners = newListeners;
  }

  public synchronized void removeProgressListener(ProgressListener listener) {
    ArrayList<ProgressListener> newListeners = (ArrayList<ProgressListener>) listeners.clone();
    newListeners.remove(listener);
    listeners = newListeners;
  }

  //----------------------------------------------------------------------
  // Internals only below this point
  //

  class FetchKey {
    private Object imageDescriptor;
    private IDENT  clientIdentifier;
    private int    requestedImageSize;

    public FetchKey(Object imageDescriptor,
                    IDENT clientIdentifier,
                    int requestedImageSize) {
      this.imageDescriptor = imageDescriptor;
      this.clientIdentifier = clientIdentifier;
      this.requestedImageSize = requestedImageSize;
    }

    public Object getImageDescriptor   () { return imageDescriptor;    }
    public IDENT  getClientIdentifier  () { return clientIdentifier;   }
    public int    getRequestedImageSize() { return requestedImageSize; }

    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null) return false;
      if (getClass() != o.getClass()) return false;

      FetchKey other = (FetchKey) o;
      return (requestedImageSize == other.requestedImageSize &&
              imageDescriptor.equals(other.imageDescriptor) &&
              clientIdentifier.equals(other.clientIdentifier));
    }

    public int hashCode() {
      int result;
      result = clientIdentifier.hashCode();
      result = 29 * result + imageDescriptor.hashCode();
      result = 29 * result + requestedImageSize;
      return result;
    }
  }

  private synchronized void fireProgressStart(final ProgressEvent e) {
    List<ProgressListener> curListeners = listeners;
    for (ProgressListener listener : curListeners) {
      final ProgressListener finalListener = listener;
      invokeLaterOnEDT(new Runnable() {
          public void run() {
            finalListener.progressStart(e);
          }
        });
    }
  }

  private synchronized void fireProgressUpdate(final ProgressEvent e) {
    List<ProgressListener> curListeners = listeners;
    for (ProgressListener listener : curListeners) {
      final ProgressListener finalListener = listener;
      invokeLaterOnEDT(new Runnable() {
          public void run() {
            finalListener.progressUpdate(e);
          }
        });
    }
  }

  private synchronized void fireProgressEnd(final ProgressEvent e) {
    List<ProgressListener> curListeners = listeners;
    for (ProgressListener listener : curListeners) {
      final ProgressListener finalListener = listener;
      invokeLaterOnEDT(new Runnable() {
          public void run() {
            finalListener.progressEnd(e);
          }
        });
    }
  }

  private void invokeLaterOnEDT(Runnable runnable) {
    if (EventQueue.isDispatchThread()) {
      runnable.run();
    } else {
      EventQueue.invokeLater(runnable);
    }
  }
}
