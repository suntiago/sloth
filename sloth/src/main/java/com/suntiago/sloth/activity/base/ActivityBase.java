package com.suntiago.sloth.activity.base;

import android.os.Bundle;
import android.text.TextUtils;

import com.hwangjr.rxbus.RxBus;
import com.suntiago.sloth.activity.ActivityStackManager;
import com.suntiago.sloth.activity.base.theMvp.DataBinderBase;
import com.suntiago.sloth.activity.base.theMvp.databind.DataBinder;
import com.suntiago.sloth.activity.base.theMvp.model.BaseModel;
import com.suntiago.sloth.activity.base.theMvp.model.IModel;
import com.suntiago.sloth.activity.base.theMvp.presenter.ActivityPresenter;
import com.suntiago.sloth.utils.log.Slog;

import java.util.ArrayList;
import java.util.List;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by zy on 2018/12/4.
 */

public abstract class ActivityBase<T extends AppDelegateBase, D extends IModel> extends ActivityPresenter<T, D> {

  protected final String TAG = getClass().getSimpleName();
  //Rxbus注册管理
  private List<Object> mRxBusList;
  /**
   * 用来管理Subscribe的生命周期
   * 在Activity中用到的subscriptin一定要加到这个里面，在onDestroy的时候会
   * 释放掉。
   */
  private CompositeSubscription mCompositeSubscription;
  protected DataBinder binder;

  public DataBinder getDataBinder() {
    return null;
  }

  public final void notifyModelChanged() {
    if (binder != null) {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          binder.viewBindModel(viewDelegate, iModel);
        }
      });
    }
  }

  public final void notifyModelChanged(final Object o) {
    notifyModelChanged(o, null);
  }

  public final void notifyModelChanged(final Object o, final String tag) {
    if (binder != null) {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          if (!viewDelegate.dataBinding(o, tag)) {
            if (TextUtils.isEmpty(tag)) {
              binder.viewBindModel(viewDelegate, o);
            } else {
              binder.viewBindModel(viewDelegate, o, tag);
            }
          }
        }
      });
    }
  }

  public ActivityBase() {
    if (iModel != null && iModel instanceof BaseModel) {
      ((BaseModel)iModel).setActivityBase(this);
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    viewDelegate.onACreate();
  }

  @Override
  protected void initView(Bundle savedInstanceState) {
    super.initView(savedInstanceState);
    binder = getDataBinder();
    if (binder == null) {
      binder = dataBinderBase();
    }
  }

  @Override
  protected void bindEvenListener() {
    super.bindEvenListener();
    //Activity管理
    ActivityStackManager.getInstance().addActivity(this);
    //请求管理
    mCompositeSubscription = new CompositeSubscription();
    //rxbus事件
    registerRxBus(this);
  }

  @Override
  protected void onDestroy() {
    Slog.state(TAG, "---------onDestroy ");
    viewDelegate.onADestory();
    unregisterRxBus();
    ActivityStackManager.getInstance().removeActivity(this);
    mCompositeSubscription.unsubscribe();
    super.onDestroy();
  }

  @Override
  protected void onResume() {
    super.onResume();
    viewDelegate.activityResume(true);
    viewDelegate.onAResume();
  }

  @Override
  protected void onPause() {
    viewDelegate.activityResume(false);
    viewDelegate.onAPause();
    super.onPause();
  }

  @Override
  protected void onStop() {
    viewDelegate.onASTop();
    super.onStop();
  }

  @Override
  protected void onStart() {
    super.onStart();
    viewDelegate.onAStart();
  }

  public void addRxSubscription(Subscription sub) {
    if (sub != null) {
      mCompositeSubscription.add(sub);
    }
  }

  public void removeRxSubscription(Subscription sub) {
    mCompositeSubscription.remove(sub);
  }

  public void registerRxBus(Object o) {
    if (mRxBusList == null) {
      mRxBusList = new ArrayList<>();
    }
    if (mRxBusList.contains(o)) {
      return;
    }
    RxBus.get().register(o);
    mRxBusList.add(o);
  }

  public void unregisterRxBus(Object o) {
    if (mRxBusList != null && mRxBusList.contains(o)) {
      RxBus.get().unregister(o);
      mRxBusList.remove(o);
    }
  }

  public void unregisterRxBus() {
    if (mRxBusList != null && mRxBusList.size() > 0) {
      for (Object o : mRxBusList) {
        RxBus.get().unregister(o);
      }
      mRxBusList.clear();
    }
  }

  private final DataBinderBase dataBinderBase() {
    return new DataBinderBase();
  }
}