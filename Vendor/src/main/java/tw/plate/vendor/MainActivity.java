package tw.plate.vendor;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.Timer;
import java.util.TimerTask;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends ActionBarActivity implements ActionBar.TabListener, PlateOrderManager.PlateOrderManagerCallback {

    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link android.support.v4.view.ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    public static PlateOrderManager plateOrderManager;

    //================================================================================
    // Layout Setup
    //================================================================================
    private void layout_setup() {
        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayShowHomeEnabled(false);     // turn off display
        actionBar.setDisplayShowTitleEnabled(false);    // turn off display

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            //int[] tabsResources= new int[]{R.drawable.tab_location_trans,R.drawable.tab_bill_trans};

            String text = "";
            if (i==0)   text = getString(R.string.tab_cooking);
            else if (i==1)   text = getString(R.string.tab_finish);

            actionBar.addTab(
                    actionBar.newTab()
                            .setTabListener(this)
                            .setText(text)
                    //.setText(mSectionsPagerAdapter.getPageTitle(i))
                    //.setIcon(getResources().getDrawable(tabsResources[i]))
            );
        }
    }


    // ======= UI Stuff =======
    private void vendorList() {
        // FIXME: should download available list from server
        CharSequence vendorList[] = new CharSequence[] {"v1", "v2", "v3", "v4"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("選擇登入商家");
        builder.setItems(vendorList, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // which
            }
        });
        builder.show();
    }

    private void popupMessage(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setTitle(title);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    // ======= UI Stuff ======= END

    private void setupCookie() {
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);
    }

    //================================================================================
    // Runtime Override Events
    //================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // setup system-wide cookie
        setupCookie();

        // login, and first update
        plateOrderManager = new PlateOrderManager(this);
        plateOrderManager.login(this);

        //
        layout_setup();

        // regular refresh
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                refreshData();
            }
        }, 0, Constants.REFRESH_INT);
    }

    private void refreshData() {
        Log.d(Constants.LOG_TAG, "Main: refresh");
        plateOrderManager.update(this);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private final FragmentManager fm;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            this.fm = fm;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).

            //    return PlaceholderFragment.newInstance(position + 1);
            switch(position){
                case 0: return CookingFragment.newInstance();
                case 1: return FinishFragment.newInstance();
                default: return CookingFragment.newInstance();
            }

        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle
                (int position) {
            //Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.tab_cooking);
                case 1:
                    return getString(R.string.tab_finish);
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            //@SuppressWarnings("ConstantConditions") TextView textView = (TextView) rootView.findViewById(R.id.section_label);
//            textView.setText("This is page: "+Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }


    // Plate Order Manager Callbacks
    @Override
    public void orderUpdated() {
        Log.d(Constants.LOG_TAG, "order updated");

        FragmentManager fm = getSupportFragmentManager();
        String tag;

        tag = makeFragmentName(R.id.pager, 0);
        CookingFragment cookingFragment = (CookingFragment)fm.findFragmentByTag(tag);
        cookingFragment.cookingListUpdate();

        tag = makeFragmentName(R.id.pager, 1);
        FinishFragment finishFragment = (FinishFragment)fm.findFragmentByTag(tag);
        finishFragment.finishListUpdate();
    }

    @Override
    public void loginCompleted() {
        Log.d(Constants.LOG_TAG, "login completed");
        plateOrderManager.update(this);
    }

    //
    private static String makeFragmentName(int viewId, int index) {
        return "android:switcher:" + viewId + ":" + index;
    }


    //================================================================================
    // End
    //================================================================================
}
