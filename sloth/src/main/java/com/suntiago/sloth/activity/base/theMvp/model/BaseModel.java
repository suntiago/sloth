package com.suntiago.sloth.activity.base.theMvp.model;

import com.suntiago.sloth.activity.base.ActivityBase;

/**
 * Created by Zaiyu on 2019/4/26.
 */
public abstract class BaseModel implements IModel {
  ActivityBase mActivityBase;

  public void setActivityBase(ActivityBase activityBase) {
    mActivityBase = activityBase;
  }

  public BaseModel(ActivityBase activityBase) {
    mActivityBase = activityBase;
  }

  public BaseModel() {
  }

  public final void notifyModelChanged() {
    if (mActivityBase != null) {
      mActivityBase.notifyModelChanged();
    }
  }

  public final void notifyModelChanged(final Object o) {
    if (mActivityBase != null) {
      mActivityBase.notifyModelChanged(o);
    }
  }

  public final void notifyModelChanged(final Object o, final String tag) {
    if (mActivityBase != null) {
      mActivityBase.notifyModelChanged(o, tag);
    }
  }
}

