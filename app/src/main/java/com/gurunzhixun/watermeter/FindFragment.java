package com.gurunzhixun.watermeter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.gurunzhixun.watermeter.base.BaseFragment;
import com.gurunzhixun.watermeter.modules.fx.activity.JFFragment;
import com.gurunzhixun.watermeter.modules.fx.activity.TJFragment;
import com.gurunzhixun.watermeter.modules.sbgl.model.entity.MyMeterVo;
import com.gurunzhixun.watermeter.util.PreferenceUtils;


public class FindFragment extends BaseFragment {
    private View mView;
    private LinearLayout tongji, jiaofei;
    private JFFragment second ;
    private TJFragment first ;
    private Fragment currentFragment ;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView=inflater.inflate(R.layout.fragment_2, container, false);
        tongji = mView.findViewById(R.id.tongji);
        jiaofei = mView.findViewById(R.id.jiaofei);
        second = new JFFragment();
        first = new TJFragment();
        currentFragment = new Fragment();
        tongji.setOnClickListener(view -> switchFragment(first).commit());
        jiaofei.setOnClickListener(view -> switchFragment(second).commit());
        switchFragment(first).commit();

        return mView;
    }
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }
    public void refushDataView() {
        String myMeterJson = PreferenceUtils.getInstance(getActivity()).getString("myMeterJson");
        String myMeterVoNumber_str = PreferenceUtils.getInstance(getActivity()).getString("myMeterVoNumber");
        String myMeterVoName = PreferenceUtils.getInstance(getActivity()).getString("myMeterVoName");
        if(!myMeterVoNumber_str.equals("")){
            Gson sg = new Gson();
            MyMeterVo vv = sg.fromJson(myMeterJson,MyMeterVo.class);
            MainApplicaton.meterVo = vv;
        }
        if(first!=null){
            first.updata();
        }
        if(second!=null){
            second.updata();
        }
    }

    private FragmentTransaction switchFragment(Fragment targetFragment) {
        FragmentTransaction transaction = null;
        if(getActivity()!=null&&currentFragment!=null){
         transaction = getActivity().getSupportFragmentManager().beginTransaction();
            if(currentFragment.getClass().equals(targetFragment.getClass())){
                return transaction;
            }
        }
        if(transaction!=null){
            if (!targetFragment.isAdded()) {
                if (currentFragment != null) {
                    transaction.remove(currentFragment);
                }
                transaction.add(R.id.fragment1, targetFragment, targetFragment.getClass().getName());

            } else {
                transaction.remove(currentFragment).show(targetFragment);
            }
            currentFragment = targetFragment;
        }
        return transaction;

    }
}
