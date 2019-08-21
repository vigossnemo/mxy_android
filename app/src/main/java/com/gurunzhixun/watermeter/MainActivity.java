package com.gurunzhixun.watermeter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gurunzhixun.watermeter.base.BaseActivity;
import com.gurunzhixun.watermeter.modules.yhdl.activity.LoginActivity;
import com.gurunzhixun.watermeter.util.utils.T;


public class MainActivity extends BaseActivity {
    private Fragment currentFragment = new Fragment();
   // private FindFragment first = new FindFragment();
    private Fragment1 first = new Fragment1();
    //private Fragment2 second = new Fragment2();
    private Fragment4 four = new Fragment4();
    private Fragment3 third = new Fragment3();
    private FindFragment second = new FindFragment();
    private LinearLayout home, fenxi,guanli, persion;
    private TextView home_icon, home_text, fenxi_icon, fenxi_text, guanli_icon, guanli_text, persion_icon, persion_text;
    //双击back退出间隔
    public static final int DOUBLE_CLICK_EXIT_INTERVAL = 2 * 1000;
    //记录上次点击back键时间
    private double mLastBackPressedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        home = findViewById(R.id.home);
        fenxi = findViewById(R.id.fenxi);
        guanli = findViewById(R.id.guanli);
        persion = findViewById(R.id.persion);
        home_icon = findViewById(R.id.home_icon);
        home_text = findViewById(R.id.home_text);
        fenxi_icon = findViewById(R.id.fenxi_icon);
        fenxi_text = findViewById(R.id.fenxi_text);
        guanli_icon = findViewById(R.id.guanli_icon);
        guanli_text = findViewById(R.id.guanli_text);
        persion_icon = findViewById(R.id.persion_icon);
        persion_text = findViewById(R.id.persion_text);
        home.setOnClickListener(view -> switchFragment(first).commit());
        fenxi.setOnClickListener(view -> switchFragment(second).commit());
        guanli.setOnClickListener(view -> switchFragment(four).commit());
        persion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!MainApplicaton.sIsLogin) {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                } else {
                    switchFragment(third).commit();
                }
            }
        });
        switchFragment(first).commit();
//        Intent intentOne = new Intent(MainActivity.this, UPMeterDataService.class);
//        MainActivity.this.startService(intentOne);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!MainApplicaton.sIsLogin && third.isVisible()) {
            home.callOnClick();
            getSupportFragmentManager().beginTransaction().remove(third).commit();
        }
        first.refushDataView();
        if(MainApplicaton.loginResultVBack.getUser().getMgrId()==null){
            guanli_text.setText("资讯");
        }else{
            guanli_text.setText("管理");
        }
    }

    @Override
    protected String getPageTitle() {
        return "首页";
    }




    private FragmentTransaction switchFragment(Fragment targetFragment) {

        if (targetFragment.getClass().equals(Fragment1.class)) {
            home_icon.setBackgroundResource(R.drawable.home_icon_watch_pressed);
            fenxi_icon.setBackgroundResource(R.drawable.home_icon_analyze_normal);
            guanli_icon.setBackgroundResource(R.drawable.home_icon_manage);
            persion_icon.setBackgroundResource(R.drawable.home_icon_my_normal);
            home_text.setTextColor(this.getResources().getColor(R.color.colorPrimary));
            fenxi_text.setTextColor(this.getResources().getColor(R.color.main_title));
            guanli_text.setTextColor(this.getResources().getColor(R.color.main_title));
            persion_text.setTextColor(this.getResources().getColor(R.color.main_title));
        } else if (targetFragment.getClass().equals(FindFragment.class)) {
            home_icon.setBackgroundResource(R.drawable.home_icon_watch_normal);
            fenxi_icon.setBackgroundResource(R.drawable.home_icon_analyze_pressed);
            guanli_icon.setBackgroundResource(R.drawable.home_icon_manage);
            persion_icon.setBackgroundResource(R.drawable.home_icon_my_normal);
            home_text.setTextColor(this.getResources().getColor(R.color.main_title));
            fenxi_text.setTextColor(this.getResources().getColor(R.color.colorPrimary));
            guanli_text.setTextColor(this.getResources().getColor(R.color.main_title));
            persion_text.setTextColor(this.getResources().getColor(R.color.main_title));
            second.refushDataView();
        } else if (targetFragment.getClass().equals(Fragment4.class)) {
            home_icon.setBackgroundResource(R.drawable.home_icon_watch_normal);
            fenxi_icon.setBackgroundResource(R.drawable.home_icon_analyze_normal);
            guanli_icon.setBackgroundResource(R.drawable.home_icon_manage_sel);
            persion_icon.setBackgroundResource(R.drawable.home_icon_my_normal);
            home_text.setTextColor(this.getResources().getColor(R.color.main_title));
            fenxi_text.setTextColor(this.getResources().getColor(R.color.main_title));
            guanli_text.setTextColor(this.getResources().getColor(R.color.colorPrimary));
            persion_text.setTextColor(this.getResources().getColor(R.color.main_title));
        } else if (targetFragment.getClass().equals(Fragment3.class)) {
            home_icon.setBackgroundResource(R.drawable.home_icon_watch_normal);
            fenxi_icon.setBackgroundResource(R.drawable.home_icon_analyze_normal);
            guanli_icon.setBackgroundResource(R.drawable.home_icon_manage);
            persion_icon.setBackgroundResource(R.drawable.home_icon_my_pressed);
            home_text.setTextColor(this.getResources().getColor(R.color.main_title));
            fenxi_text.setTextColor(this.getResources().getColor(R.color.main_title));
            guanli_text.setTextColor(this.getResources().getColor(R.color.main_title));
            persion_text.setTextColor(this.getResources().getColor(R.color.colorPrimary));
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (!targetFragment.isAdded()) {
            if (currentFragment != null) {
                transaction.hide(currentFragment);
            }
            transaction.add(R.id.fragment, targetFragment, targetFragment.getClass().getName());
//            transaction.commitAllowingStateLoss();
//            getSupportFragmentManager().executePendingTransactions();
        } else {
            transaction.hide(currentFragment).show(targetFragment);
        }
        currentFragment = targetFragment;
        return transaction;
    }




    public void JumpToFind() {
        switchFragment(second).commit();
    }


    @Override
    public void onBackPressed() {
//        exitApp();
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            exitApp();
            return false;
        }
        return super.dispatchKeyEvent(event);
    }

    /**
     * 退出App, 清空activity
     */
    public void exitApp() {
        if (System.currentTimeMillis() - mLastBackPressedTime > DOUBLE_CLICK_EXIT_INTERVAL) {
            T.showToastSafe("再次点击退出应用");
            mLastBackPressedTime = System.currentTimeMillis();
        } else {
            MainApplicaton.getInstance().removeAllActivities();
        }
    }
}
