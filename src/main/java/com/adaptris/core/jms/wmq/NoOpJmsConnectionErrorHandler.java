package com.adaptris.core.jms.wmq;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.ConnectionErrorHandler;
import com.adaptris.core.ConnectionErrorHandlerImp;
import com.adaptris.core.CoreException;

public class NoOpJmsConnectionErrorHandler extends ConnectionErrorHandlerImp {

  @Override
  public void handleConnectionException() {
    super.restartAffectedComponents();
  }

  @Override
  public void init() throws CoreException {
  }

  @Override
  public void start() throws CoreException {
  }

  @Override
  public void stop() {
  }

  @Override
  public void close() {
  }

  @Override
  public boolean allowedInConjunctionWith(ConnectionErrorHandler ceh) {
    return !equals(ceh);
  }

  @Override
  public boolean equals(Object obj) {
    boolean result = false;
    return result;
  }

  @Override
  public int hashCode() {
    int hashCode = 0;
    if (retrieveConnection(AdaptrisConnection.class) != null) {
      hashCode = new HashCodeBuilder(11, 17).append(retrieveConnection(AdaptrisConnection.class)).toHashCode();
    }
    return hashCode == 0 ? super.hashCode() : hashCode;
  }

}
