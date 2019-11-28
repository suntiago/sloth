package com.suntiago.dblibDemo.themvpTest;

import com.suntiago.sloth.activity.base.theMvp.databind.DataBinder;

/**
 * Created by zy on 2018/12/27.
 */

public class DatabindT implements DataBinder<MvpTestDelegate, JavaBeanT> {

    @Override
    public void viewBindModel(MvpTestDelegate viewDelegate, JavaBeanT data) {
        //调用viewDelegate跟新data里面的数据
        viewDelegate.viewBindModel(data);
    }

    @Override
    public void viewBindModel(MvpTestDelegate appDelegate, Object data) {

    }

    @Override
    public void viewBindModel(MvpTestDelegate appDelegate, Object data, String tag) {

    }
}
