package com.suntiago.dblibDemo.themvpTest;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.util.Log;

import com.suntiago.sloth.activity.base.theMvp.model.BaseModel;

/**
 * Created by zy on 2018/12/27.
 */

@SuppressLint("ParcelCreator")
public class JavaBeanT extends BaseModel {
  private final String TAG = getClass().getSimpleName();
  String name;


  public void setName(String name) {
    this.name = name;
    notifyModelChanged(name, "name");
  }


  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int i) {

  }

  @Override
  public void loadDataFromCache() {
    Log.d(TAG, "loadDataFromCache: ");
  }
}
