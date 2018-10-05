package com.adaptris.core.wmq;

import com.adaptris.core.CoreException;
import com.adaptris.core.PollerImp;

public class NoOpPoller extends PollerImp {

  @Override
  public void close() {
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
  public void prepare() throws CoreException {
  }

}
